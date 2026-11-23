from django.contrib import admin

from .models import Antecedente, Atencion


@admin.register(Antecedente)
class AntecedenteAdmin(admin.ModelAdmin):
    list_display = ("id", "paciente", "tipo", "activo", "creado_en", "registrado_por")
    list_filter = ("tipo", "activo")
    search_fields = ("paciente__usuario__nombre", "paciente__usuario__apellido", "descripcion")


@admin.register(Atencion)
class AtencionAdmin(admin.ModelAdmin):
    list_display = ("id", "paciente", "medico", "fecha_atencion", "diagnostico")
    list_filter = ("fecha_atencion",)
    search_fields = ("paciente__usuario__nombre", "paciente__usuario__apellido", "diagnostico")
