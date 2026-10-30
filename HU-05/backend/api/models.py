from django.core.exceptions import ValidationError
from django.db import models
from django.utils import timezone

from usuarios.models import Medico, Paciente


def validate_blood_pressure(value):
    try:
        systolic_text, diastolic_text = value.split("/", maxsplit=1)
        systolic = int(systolic_text)
        diastolic = int(diastolic_text)
    except (ValueError, AttributeError):
        raise ValidationError("Usa el formato sistólica/diastólica, por ejemplo 120/80.")
    if not (50 <= systolic <= 260 and 30 <= diastolic <= 160 and systolic > diastolic):
        raise ValidationError("La presión arterial está fuera de los límites admitidos.")


class Antecedente(models.Model):
    class Tipo(models.TextChoices):
        ALERGIA = "alergia", "Alergia"
        PATOLOGIA = "patologia", "Patología"
        CIRUGIA = "cirugia", "Cirugía"
        FAMILIAR = "familiar", "Antecedente familiar"
        MEDICAMENTO = "medicamento", "Medicamento"

    paciente = models.ForeignKey(
        Paciente,
        on_delete=models.CASCADE,
        related_name="antecedentes",
    )
    tipo = models.CharField(max_length=20, choices=Tipo.choices)
    descripcion = models.TextField()
    fecha_aproximada = models.DateField(null=True, blank=True)
    activo = models.BooleanField(default=True)
    registrado_por = models.ForeignKey(
        Medico,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="antecedentes_registrados",
    )
    creado_en = models.DateTimeField(default=timezone.now, editable=False)

    class Meta:
        db_table = "usuarios_antecedente"
        ordering = ("tipo", "-creado_en", "-id")
        indexes = [
            models.Index(fields=("paciente", "tipo"), name="ant_paciente_tipo_idx"),
        ]

    def __str__(self):
        return f"{self.get_tipo_display()} — paciente {self.paciente_id}"


class Atencion(models.Model):
    paciente = models.ForeignKey(
        Paciente,
        on_delete=models.CASCADE,
        related_name="atenciones",
    )
    medico = models.ForeignKey(
        Medico,
        on_delete=models.PROTECT,
        related_name="atenciones",
    )
    fecha_atencion = models.DateTimeField(default=timezone.now)
    motivo_consulta = models.TextField()
    presion_arterial = models.CharField(
        max_length=15,
        blank=True,
        validators=[validate_blood_pressure],
    )
    frecuencia_cardiaca = models.PositiveSmallIntegerField(null=True, blank=True)
    temperatura = models.DecimalField(max_digits=4, decimal_places=1, null=True, blank=True)
    saturacion_o2 = models.PositiveSmallIntegerField(null=True, blank=True)
    peso_kg = models.DecimalField(max_digits=6, decimal_places=2, null=True, blank=True)
    talla_cm = models.DecimalField(max_digits=5, decimal_places=2, null=True, blank=True)
    diagnostico = models.TextField()
    codigo_cie10 = models.CharField(max_length=10, blank=True)
    tratamiento = models.TextField()
    notas = models.TextField(blank=True)
    creado_en = models.DateTimeField(default=timezone.now, editable=False)

    class Meta:
        db_table = "usuarios_atencion"
        ordering = ("-fecha_atencion", "-id")
        indexes = [
            models.Index(fields=("paciente", "-fecha_atencion"), name="ate_paciente_fecha_idx"),
        ]

    def clean(self):
        super().clean()
        errors = {}
        numeric_ranges = {
            "frecuencia_cardiaca": (20, 250),
            "temperatura": (25, 45),
            "saturacion_o2": (0, 100),
            "peso_kg": (0.1, 500),
            "talla_cm": (20, 250),
        }
        for field, (minimum, maximum) in numeric_ranges.items():
            value = getattr(self, field)
            if value is not None and not minimum <= value <= maximum:
                errors[field] = f"El valor debe estar entre {minimum} y {maximum}."
        if self.presion_arterial:
            try:
                validate_blood_pressure(self.presion_arterial)
            except ValidationError as error:
                errors["presion_arterial"] = error.messages
        if errors:
            raise ValidationError(errors)

    def __str__(self):
        return f"Atención {self.pk or 'nueva'} — paciente {self.paciente_id}"
