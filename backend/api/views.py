from datetime import datetime, time, timedelta

from django.db.models import Q
from django.utils import timezone
from rest_framework import generics
from rest_framework.exceptions import ValidationError
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.views import TokenObtainPairView

from .authentication import EmailTokenObtainPairSerializer
from .models import Antecedente, Atencion
from .permissions import IsActiveMedicalUserWithPermission
from .serializers import (
    AtencionSerializer,
    GroupedAntecedentesSerializer,
    PatientSearchSerializer,
)
from usuarios.models import Medico, Paciente


class EmailTokenObtainPairView(TokenObtainPairView):
    permission_classes = (AllowAny,)
    serializer_class = EmailTokenObtainPairSerializer


class PatientSearchView(generics.ListAPIView):
    serializer_class = PatientSearchSerializer
    permission_classes = (IsActiveMedicalUserWithPermission,)
    required_medical_permission = "paciente.leer"

    def get_queryset(self):
        query = self.request.query_params.get("q", "").strip()
        if not query:
            raise ValidationError({"q": "Este parámetro es obligatorio."})
        if len(query) > 100:
            raise ValidationError({"q": "La búsqueda no puede superar 100 caracteres."})
        if query.isdigit():
            if len(query) != 8:
                raise ValidationError({"q": "El DNI debe contener exactamente 8 dígitos."})
            match = Q(usuario__dni=query)
        else:
            if len(query) < 2:
                raise ValidationError({"q": "Ingresa al menos 2 caracteres."})
            match = Q(usuario__nombre__icontains=query) | Q(usuario__apellido__icontains=query)
        return (
            Paciente.objects.select_related("usuario")
            .filter(match)
            .order_by("usuario__apellido", "usuario__nombre", "pk")
        )


class PatientDetailView(generics.RetrieveAPIView):
    queryset = Paciente.objects.select_related("usuario")
    serializer_class = PatientSearchSerializer
    permission_classes = (IsActiveMedicalUserWithPermission,)
    required_medical_permission = "paciente.leer"


class PatientAntecedentesView(APIView):
    permission_classes = (IsActiveMedicalUserWithPermission,)
    required_medical_permission = "paciente.leer"

    def get(self, request, paciente_id):
        patient = generics.get_object_or_404(Paciente, pk=paciente_id)
        antecedentes = (
            Antecedente.objects.filter(paciente=patient)
            .select_related("registrado_por__usuario")
            .order_by("tipo", "-creado_en", "-pk")
        )
        return Response(GroupedAntecedentesSerializer(antecedentes).data)


class PatientAtencionesView(generics.ListCreateAPIView):
    serializer_class = AtencionSerializer
    permission_classes = (IsActiveMedicalUserWithPermission,)
    permission_by_method = {
        "GET": "paciente.leer",
        "POST": "paciente.editar",
    }

    def get_queryset(self):
        patient = generics.get_object_or_404(Paciente, pk=self.kwargs["paciente_id"])
        queryset = (
            Atencion.objects.filter(paciente=patient)
            .select_related("medico__usuario", "paciente__usuario")
            .order_by("-fecha_atencion", "-pk")
        )
        desde = self.request.query_params.get("desde")
        hasta = self.request.query_params.get("hasta")
        start = self._parse_date(desde, "desde") if desde else None
        end = self._parse_date(hasta, "hasta") if hasta else None
        if start and end and start > end:
            raise ValidationError({"hasta": "Debe ser igual o posterior a desde."})
        if start:
            queryset = queryset.filter(fecha_atencion__gte=timezone.make_aware(datetime.combine(start, time.min)))
        if end:
            end_exclusive = end + timedelta(days=1)
            queryset = queryset.filter(
                fecha_atencion__lt=timezone.make_aware(datetime.combine(end_exclusive, time.min))
            )
        return queryset

    @staticmethod
    def _parse_date(value, parameter):
        try:
            return datetime.strptime(value, "%Y-%m-%d").date()
        except ValueError as error:
            raise ValidationError({parameter: "Usa el formato YYYY-MM-DD."}) from error

    def perform_create(self, serializer):
        try:
            doctor = Medico.objects.select_related("usuario").get(usuario__cuenta=self.request.user)
        except Medico.DoesNotExist as error:
            raise ValidationError({"detail": "La cuenta no tiene un perfil médico válido."}) from error
        patient = generics.get_object_or_404(Paciente, pk=self.kwargs["paciente_id"])
        serializer.save(paciente=patient, medico=doctor)
