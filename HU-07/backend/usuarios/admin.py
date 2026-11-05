from django.contrib import admin

from .models import Medico, Paciente, Usuario


@admin.register(Usuario)
class UsuarioAdmin(admin.ModelAdmin):
    list_display = ("id", "nombre", "apellido", "dni", "rol", "correo")
    list_filter = ("rol",)
    search_fields = ("nombre", "apellido", "dni", "correo")


@admin.register(Paciente)
class PacienteAdmin(admin.ModelAdmin):
    list_display = ("id", "usuario", "fecha_nacimiento", "tipo_sangre")
    search_fields = ("usuario__nombre", "usuario__apellido", "usuario__dni")


@admin.register(Medico)
class MedicoAdmin(admin.ModelAdmin):
    list_display = ("id", "usuario", "cmp", "especialidad")
    search_fields = ("usuario__nombre", "usuario__apellido", "cmp")
