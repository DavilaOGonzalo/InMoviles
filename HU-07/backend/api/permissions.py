from rest_framework.permissions import BasePermission

from usuarios.models import Usuario


class IsActiveMedicalUserWithPermission(BasePermission):
    message = "Se requiere una cuenta médica activa con el permiso solicitado."

    def has_permission(self, request, view):
        user = request.user
        if not user or not user.is_authenticated or not user.is_active:
            return False
        try:
            profile = Usuario.objects.select_related("cuenta").get(cuenta_id=user.pk)
        except Usuario.DoesNotExist:
            return False
        if profile.rol != "medico" or not hasattr(profile, "medico"):
            return False

        required = getattr(view, "permission_by_method", {}).get(
            request.method,
            getattr(view, "required_medical_permission", "paciente.leer"),
        )
        return isinstance(profile.permisos, list) and required in profile.permisos
