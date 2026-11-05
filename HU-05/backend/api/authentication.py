from rest_framework import serializers
from rest_framework.exceptions import AuthenticationFailed, PermissionDenied
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
from rest_framework_simplejwt.tokens import RefreshToken

from usuarios.models import Usuario


DEMO_EMAIL_COMPATIBILITY = {
    "medico.prueba@tecsup.edu.pe": "medico.demo@tecsup.edu.pe",
    "paciente.prueba@tecsup.edu.pe": "paciente.demo@tecsup.edu.pe",
    "medico.demo@tecsup.edu.pe": "medico.prueba@tecsup.edu.pe",
    "paciente.demo@tecsup.edu.pe": "paciente.prueba@tecsup.edu.pe",
}


class EmailTokenObtainPairSerializer(TokenObtainPairSerializer):
    username_field = "email"
    email = serializers.EmailField()
    password = serializers.CharField(write_only=True, trim_whitespace=False)

    def validate(self, attrs):
        requested_email = attrs["email"].casefold()
        lookup_emails = (requested_email, DEMO_EMAIL_COMPATIBILITY.get(requested_email))
        try:
            profile = Usuario.objects.select_related("cuenta").get(
                correo__iexact=lookup_emails[0],
            )
        except Usuario.DoesNotExist:
            if lookup_emails[1] is None:
                raise AuthenticationFailed("Credenciales inválidas.")
            try:
                profile = Usuario.objects.select_related("cuenta").get(
                    correo__iexact=lookup_emails[1],
                )
            except Usuario.DoesNotExist as error:
                raise AuthenticationFailed("Credenciales inválidas.") from error
        except Usuario.MultipleObjectsReturned as error:
            raise AuthenticationFailed("Credenciales inválidas.") from error

        user = profile.cuenta
        if user is None or not user.check_password(attrs["password"]):
            raise AuthenticationFailed("Credenciales inválidas.")
        if not user.is_active:
            raise PermissionDenied("La cuenta está inactiva.")

        refresh = RefreshToken.for_user(user)
        return {
            "refresh": str(refresh),
            "access": str(refresh.access_token),
            "user": {
                "id": profile.pk,
                "nombre": profile.nombre,
                "apellido": profile.apellido,
                "rol": profile.rol,
            },
        }
