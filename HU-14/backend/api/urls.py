from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView

from .views import (
    EmailTokenObtainPairView,
    PatientAntecedentesView,
    PatientAtencionesView,
    PatientDetailView,
    PatientSearchView,
)

urlpatterns = [
    path("auth/token/", EmailTokenObtainPairView.as_view(), name="token_obtain_pair"),
    path("auth/token/refresh/", TokenRefreshView.as_view(), name="token_refresh"),
    path("medico/pacientes/", PatientSearchView.as_view(), name="patient-search"),
    path("medico/pacientes/<int:pk>/", PatientDetailView.as_view(), name="patient-detail"),
    path(
        "medico/pacientes/<int:paciente_id>/antecedentes/",
        PatientAntecedentesView.as_view(),
        name="patient-antecedentes",
    ),
    path(
        "medico/pacientes/<int:paciente_id>/atenciones/",
        PatientAtencionesView.as_view(),
        name="patient-atenciones",
    ),
]
