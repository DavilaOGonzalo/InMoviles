from datetime import timedelta

from django.contrib.auth import get_user_model
from django.urls import reverse
from django.utils import timezone
from rest_framework.test import APITestCase

from api.models import Antecedente, Atencion
from usuarios.models import Medico, Paciente, Usuario


class MedicalApiTests(APITestCase):
    def setUp(self):
        user_model = get_user_model()
        self.auth_user = user_model.objects.create_user(
            username="doctor@example.test",
            email="doctor@example.test",
            password="test-only-secret",
        )
        self.doctor_profile = Usuario.objects.create(
            nombre="Doctora Ficticia",
            apellido="Prueba",
            correo="doctor@example.test",
            telefono="000000000",
            cuenta=self.auth_user,
            dni="00000001",
            rol="medico",
            permisos=[
                "paciente.leer",
                "paciente.editar",
            ],
        )
        self.doctor = Medico.objects.create(
            usuario=self.doctor_profile,
            cmp="TEST-001",
            direccion="Dirección ficticia",
            especialidad="Especialidad de prueba",
            identificacion_complementaria="",
            sexo="no especificado",
        )
        self.patient_user = Usuario.objects.create(
            nombre="Paciente Ficticio",
            apellido="Ejemplo",
            correo="patient@example.test",
            telefono="000000000",
            dni="12345678",
            rol="paciente",
            permisos=[],
        )
        self.patient = Paciente.objects.create(
            usuario=self.patient_user,
            contacto_emergencia="",
            direccion="",
            informacion_contacto="",
            sexo="no especificado",
            telefono_emergencia="",
            tipo_sangre="O+",
        )
        self.client.force_authenticate(user=self.auth_user)

    def test_search_by_exact_dni_and_name_partial(self):
        url = reverse("patient-search")

        dni_response = self.client.get(url, {"q": "12345678"})
        name_response = self.client.get(url, {"q": "Ficticio"})

        self.assertEqual(dni_response.status_code, 200)
        self.assertEqual(dni_response.data["results"][0]["id"], self.patient.pk)
        self.assertEqual(dni_response.data["results"][0]["dni"], "12345678")
        self.assertIn("alergias", dni_response.data["results"][0])
        self.assertIn("condiciones_cronicas", dni_response.data["results"][0])
        self.assertIn("medicamentos_actuales", dni_response.data["results"][0])
        self.assertIn("contacto_emergencia", dni_response.data["results"][0])
        self.assertIn("telefono_emergencia", dni_response.data["results"][0])
        self.assertIn("observaciones_medicas", dni_response.data["results"][0])
        self.assertIn("informacion_medica_actualizada", dni_response.data["results"][0])
        self.assertEqual(name_response.status_code, 200)
        self.assertEqual(name_response.data["count"], 1)

    def test_search_rejects_missing_or_partial_numeric_dni(self):
        url = reverse("patient-search")

        self.assertEqual(self.client.get(url).status_code, 400)
        self.assertEqual(self.client.get(url, {"q": "123"}).status_code, 400)

    def test_search_only_returns_rows_with_patient_profile(self):
        Usuario.objects.create(
            nombre="Paciente Sin Perfil",
            apellido="Prueba",
            correo="no-profile@example.test",
            telefono="000000000",
            dni="12345679",
            rol="paciente",
            permisos=[],
        )

        response = self.client.get(reverse("patient-search"), {"q": "Paciente"})

        self.assertEqual(response.data["count"], 1)
        self.assertEqual(response.data["results"][0]["id"], self.patient.pk)

    def test_patient_detail_returns_expected_contract(self):
        response = self.client.get(reverse("patient-detail", args=[self.patient.pk]))

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data, {
            "id": self.patient.pk,
            "nombre": "Paciente Ficticio",
            "apellido": "Ejemplo",
            "dni": "12345678",
            "fecha_nacimiento": None,
            "sexo": "no especificado",
            "tipo_sangre": "O+",
            "alergias": [],
            "condiciones_cronicas": [],
            "medicamentos_actuales": [],
            "contacto_emergencia": "",
            "telefono_emergencia": "",
            "observaciones_medicas": "",
            "informacion_medica_actualizada": None,
        })

    def test_antecedentes_are_grouped_by_type(self):
        Antecedente.objects.create(
            paciente=self.patient,
            tipo=Antecedente.Tipo.ALERGIA,
            descripcion="Dato ficticio de prueba",
            registrado_por=self.doctor,
        )

        response = self.client.get(
            reverse("patient-antecedentes", args=[self.patient.pk]),
        )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.data["alergia"]), 1)
        self.assertEqual(response.data["patologia"], [])

    def test_atencion_creation_uses_authenticated_doctor_not_request_body(self):
        url = reverse("patient-atenciones", args=[self.patient.pk])
        payload = {
            "motivo_consulta": "Motivo ficticio de prueba",
            "diagnostico": "Diagnóstico ficticio de prueba",
            "tratamiento": "Tratamiento ficticio de prueba",
            "medico": 999999,
            "presion_arterial": "120/80",
            "frecuencia_cardiaca": 70,
            "temperatura": "36.5",
            "saturacion_o2": 98,
            "peso_kg": "60.00",
            "talla_cm": "165.00",
        }

        response = self.client.post(url, payload, format="json")

        self.assertEqual(response.status_code, 201, response.data)
        created = Atencion.objects.get(pk=response.data["id"])
        self.assertEqual(created.medico_id, self.doctor.pk)
        self.assertEqual(created.paciente_id, self.patient.pk)

    def test_atencion_creation_validates_required_text_and_vital_ranges(self):
        url = reverse("patient-atenciones", args=[self.patient.pk])
        missing_fields = self.client.post(url, {}, format="json")
        invalid_vital = self.client.post(
            url,
            {
                "motivo_consulta": "Motivo de prueba",
                "diagnostico": "Diagnóstico de prueba",
                "tratamiento": "Tratamiento de prueba",
                "frecuencia_cardiaca": 300,
            },
            format="json",
        )

        self.assertEqual(missing_fields.status_code, 400)
        self.assertEqual(invalid_vital.status_code, 400)
        self.assertIn("frecuencia_cardiaca", invalid_vital.data)

    def test_atenciones_are_paginated_ordered_and_filterable_by_dates(self):
        old_date = timezone.now() - timedelta(days=10)
        recent_date = timezone.now() - timedelta(days=1)
        old_record = Atencion.objects.create(
            paciente=self.patient,
            medico=self.doctor,
            fecha_atencion=old_date,
            motivo_consulta="Prueba antigua",
            diagnostico="Diagnóstico ficticio",
            tratamiento="Tratamiento ficticio",
        )
        recent_record = Atencion.objects.create(
            paciente=self.patient,
            medico=self.doctor,
            fecha_atencion=recent_date,
            motivo_consulta="Prueba reciente",
            diagnostico="Diagnóstico ficticio",
            tratamiento="Tratamiento ficticio",
        )

        response = self.client.get(
            reverse("patient-atenciones", args=[self.patient.pk]),
            {"desde": recent_date.date().isoformat(), "hasta": recent_date.date().isoformat()},
        )
        all_records = self.client.get(reverse("patient-atenciones", args=[self.patient.pk]))

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["count"], 1)
        self.assertEqual(response.data["results"][0]["id"], recent_record.pk)
        self.assertNotEqual(response.data["results"][0]["id"], old_record.pk)
        self.assertEqual(
            [record["id"] for record in all_records.data["results"]],
            [recent_record.pk, old_record.pk],
        )

    def test_atenciones_for_unknown_patient_return_not_found(self):
        response = self.client.get(reverse("patient-atenciones", args=[999999]))

        self.assertEqual(response.status_code, 404)

    def test_inactive_nonmedical_and_missing_permission_users_are_forbidden(self):
        self.auth_user.is_active = False
        self.auth_user.save(update_fields=["is_active"])
        inactive_response = self.client.get(reverse("patient-search"), {"q": "12345678"})
        self.assertEqual(inactive_response.status_code, 403)

        self.auth_user.is_active = True
        self.auth_user.save(update_fields=["is_active"])
        self.doctor_profile.permisos = []
        self.doctor_profile.save(update_fields=["permisos"])
        no_permission_response = self.client.get(reverse("patient-search"), {"q": "12345678"})
        self.assertEqual(no_permission_response.status_code, 403)

        self.doctor_profile.permisos = ["paciente.leer"]
        self.doctor_profile.rol = "paciente"
        self.doctor_profile.save(update_fields=["permisos", "rol"])
        nonmedical_response = self.client.get(reverse("patient-search"), {"q": "12345678"})
        self.assertEqual(nonmedical_response.status_code, 403)

    def test_attention_creation_and_read_use_distinct_permissions(self):
        self.doctor_profile.permisos = ["paciente.editar"]
        self.doctor_profile.save(update_fields=["permisos"])
        url = reverse("patient-atenciones", args=[self.patient.pk])
        payload = {
            "motivo_consulta": "Motivo ficticio",
            "diagnostico": "Diagnóstico ficticio",
            "tratamiento": "Tratamiento ficticio",
        }

        create_response = self.client.post(url, payload, format="json")
        list_response = self.client.get(url)

        self.assertEqual(create_response.status_code, 201)
        self.assertEqual(list_response.status_code, 403)

    def test_token_endpoint_accepts_email_and_password(self):
        response = self.client.post(
            reverse("token_obtain_pair"),
            {"email": "doctor@example.test", "password": "test-only-secret"},
            format="json",
        )

        self.assertEqual(response.status_code, 200)
        self.assertIn("access", response.data)
        self.assertIn("refresh", response.data)
        self.client.force_authenticate(user=None)
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {response.data['access']}")
        protected_response = self.client.get(
            reverse("patient-search"),
            {"q": "12345678"},
        )
        self.assertEqual(protected_response.status_code, 200)

    def test_token_endpoint_accepts_prueba_alias_for_existing_demo_account(self):
        user_model = get_user_model()
        demo_account = user_model.objects.create_user(
            username="medico.demo@tecsup.edu.pe",
            email="medico.demo@tecsup.edu.pe",
            password="EssAidTest!2026",
        )
        Usuario.objects.create(
            nombre="Valeria",
            apellido="Rojas Medina",
            correo="medico.demo@tecsup.edu.pe",
            telefono="900000001",
            cuenta=demo_account,
            dni="99999991",
            rol="medico",
            permisos=["paciente.leer"],
        )

        response = self.client.post(
            reverse("token_obtain_pair"),
            {"email": "medico.prueba@tecsup.edu.pe", "password": "EssAidTest!2026"},
            format="json",
        )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["user"]["rol"], "medico")

    def test_session_authentication_remains_enabled(self):
        self.client.force_authenticate(user=None)
        logged_in = self.client.login(
            username="doctor@example.test",
            password="test-only-secret",
        )

        response = self.client.get(reverse("patient-search"), {"q": "12345678"})

        self.assertTrue(logged_in)
        self.assertEqual(response.status_code, 200)
