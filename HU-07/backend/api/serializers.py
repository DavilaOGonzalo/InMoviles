from collections import defaultdict
from decimal import Decimal

from django.core.exceptions import ValidationError as DjangoValidationError
from rest_framework import serializers
from django.utils import timezone

from .models import Antecedente, Atencion, validate_blood_pressure
from usuarios.models import Paciente


class PatientSearchSerializer(serializers.ModelSerializer):
    nombre = serializers.CharField(source="usuario.nombre", read_only=True)
    apellido = serializers.CharField(source="usuario.apellido", read_only=True)
    dni = serializers.CharField(source="usuario.dni", read_only=True, allow_null=True)

    class Meta:
        model = Paciente
        fields = (
            "id",
            "nombre",
            "apellido",
            "dni",
            "fecha_nacimiento",
            "sexo",
            "tipo_sangre",
            "alergias",
            "condiciones_cronicas",
            "medicamentos_actuales",
            "contacto_emergencia",
            "telefono_emergencia",
            "observaciones_medicas",
            "informacion_medica_actualizada",
        )


class AntecedenteSerializer(serializers.ModelSerializer):
    registrado_por = serializers.SerializerMethodField()

    class Meta:
        model = Antecedente
        fields = (
            "id",
            "tipo",
            "descripcion",
            "fecha_aproximada",
            "activo",
            "registrado_por",
            "creado_en",
        )

    def get_registrado_por(self, obj):
        if obj.registrado_por_id is None:
            return None
        usuario = obj.registrado_por.usuario
        return {"id": obj.registrado_por_id, "nombre": usuario.nombre, "apellido": usuario.apellido}


class GroupedAntecedentesSerializer(serializers.Serializer):
    def to_representation(self, instance):
        groups = defaultdict(list)
        for item in instance:
            groups[item.tipo].append(AntecedenteSerializer(item).data)
        for tipo, _label in Antecedente.Tipo.choices:
            groups.setdefault(tipo, [])
        return dict(groups)


class AtencionSerializer(serializers.ModelSerializer):
    medico = serializers.SerializerMethodField()
    paciente = serializers.IntegerField(source="paciente_id", read_only=True)

    class Meta:
        model = Atencion
        fields = (
            "id",
            "paciente",
            "medico",
            "fecha_atencion",
            "motivo_consulta",
            "presion_arterial",
            "frecuencia_cardiaca",
            "temperatura",
            "saturacion_o2",
            "peso_kg",
            "talla_cm",
            "diagnostico",
            "codigo_cie10",
            "tratamiento",
            "notas",
            "creado_en",
        )
        read_only_fields = ("id", "paciente", "medico", "creado_en")

    def get_medico(self, obj):
        usuario = obj.medico.usuario
        return {
            "id": obj.medico_id,
            "nombre": usuario.nombre,
            "apellido": usuario.apellido,
        }

    def validate_motivo_consulta(self, value):
        if not value.strip():
            raise serializers.ValidationError("Este campo es obligatorio.")
        return value.strip()

    def validate_diagnostico(self, value):
        if not value.strip():
            raise serializers.ValidationError("Este campo es obligatorio.")
        return value.strip()

    def validate_tratamiento(self, value):
        if not value.strip():
            raise serializers.ValidationError("Este campo es obligatorio.")
        return value.strip()

    def validate_presion_arterial(self, value):
        if value:
            try:
                validate_blood_pressure(value)
            except DjangoValidationError as error:
                raise serializers.ValidationError(error.messages) from error
        return value

    def validate_frecuencia_cardiaca(self, value):
        if value is not None and not 20 <= value <= 250:
            raise serializers.ValidationError("Debe estar entre 20 y 250 latidos por minuto.")
        return value

    def validate_temperatura(self, value):
        if value is not None and not Decimal("25") <= value <= Decimal("45"):
            raise serializers.ValidationError("Debe estar entre 25 y 45 °C.")
        return value

    def validate_saturacion_o2(self, value):
        if value is not None and not 0 <= value <= 100:
            raise serializers.ValidationError("Debe estar entre 0 y 100 %.")
        return value

    def validate_peso_kg(self, value):
        if value is not None and not Decimal("0.1") <= value <= Decimal("500"):
            raise serializers.ValidationError("Debe estar entre 0.1 y 500 kg.")
        return value

    def validate_talla_cm(self, value):
        if value is not None and not Decimal("20") <= value <= Decimal("250"):
            raise serializers.ValidationError("Debe estar entre 20 y 250 cm.")
        return value

    def validate_fecha_atencion(self, value):
        if value.date() > timezone.localdate():
            raise serializers.ValidationError("La fecha de atención no puede ser futura.")
        return value
