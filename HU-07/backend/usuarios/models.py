from django.conf import settings
from django.db import models


class Usuario(models.Model):
    nombre = models.CharField(max_length=100)
    apellido = models.CharField(max_length=100)
    correo = models.EmailField(max_length=254, null=True, blank=True, unique=True)
    telefono = models.CharField(max_length=20)
    cuenta = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        db_column="cuenta_id",
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="perfil_essaid",
    )
    dni = models.CharField(max_length=20, null=True, blank=True, unique=True)
    rol = models.CharField(max_length=20, blank=True)
    permisos = models.JSONField(default=list)

    class Meta:
        db_table = "usuarios_usuario"
        verbose_name = "usuario EssAid"
        verbose_name_plural = "usuarios EssAid"

    def __str__(self):
        return f"{self.nombre} {self.apellido}"


class Medico(models.Model):
    usuario = models.OneToOneField(
        Usuario,
        on_delete=models.CASCADE,
        related_name="medico",
    )
    cmp = models.CharField(max_length=20, null=True, blank=True, unique=True)
    direccion = models.TextField()
    especialidad = models.CharField(max_length=100)
    fecha_nacimiento = models.DateField(null=True, blank=True)
    identificacion_complementaria = models.CharField(max_length=100)
    sexo = models.CharField(max_length=20)

    class Meta:
        db_table = "usuarios_medico"

    def __str__(self):
        return f"Dr(a). {self.usuario}"


class Paciente(models.Model):
    usuario = models.OneToOneField(
        Usuario,
        on_delete=models.CASCADE,
        related_name="paciente",
    )
    contacto_emergencia = models.CharField(max_length=120, blank=True)
    direccion = models.TextField(blank=True)
    fecha_nacimiento = models.DateField(null=True, blank=True)
    informacion_contacto = models.TextField(blank=True)
    sexo = models.CharField(max_length=20, blank=True)
    telefono_emergencia = models.CharField(max_length=20, blank=True)
    tipo_sangre = models.CharField(max_length=3, blank=True)
    alergias = models.JSONField(default=list)
    condiciones_cronicas = models.JSONField(default=list)
    medicamentos_actuales = models.JSONField(default=list)
    observaciones_medicas = models.TextField(blank=True)
    informacion_medica_actualizada = models.DateField(null=True, blank=True)

    class Meta:
        db_table = "usuarios_paciente"

    def __str__(self):
        return f"Paciente {self.usuario}"
