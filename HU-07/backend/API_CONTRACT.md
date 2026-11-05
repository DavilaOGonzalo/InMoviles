# EssAid medical API contract

Base path: `/api/`. JSON is UTF-8. All listed routes except token issue/refresh
require an active authenticated user. Mobile requests use JWT:

```http
Authorization: Bearer <access>
Accept: application/json
```

JWT access tokens expire after 10 minutes; refresh tokens expire after one
day. Django session authentication is also enabled. The API does not expose a
mobile registration endpoint.
The current Android session stores the access token in memory; until automatic
refresh is wired into the client, the user must sign in again after access
token expiration.

## Obtain and refresh mobile token

### `POST /api/auth/token/`

Request:

```json
{"email":"doctor@example.test","password":"<account password>"}
```

Successful response (`200`):

```json
{
  "refresh": "<refresh-jwt>",
  "access": "<access-jwt>",
  "user": {"id": 12, "nombre": "Nombre", "apellido": "Apellido", "rol": "medico"}
}
```

Invalid credentials or unlinked accounts return `401`; an inactive account
returns `403`.

### `POST /api/auth/token/refresh/`

Request: `{"refresh":"<refresh-jwt>"}`. Success (`200`) returns a new `access`
token; invalid or expired refresh tokens return `401`.

## Search patients

### `GET /api/medico/pacientes/?q=<query>&page=<number>`

Requires active medical role and `paciente.leer`. `q` is either an exact
8-digit DNI or a case-insensitive partial match of first name or surname
(minimum two characters). Results are limited to rows that have a
`usuarios_paciente` profile. The page size is 20 and follows DRF pagination.

Successful response (`200`):

```json
{
  "count": 1,
  "next": null,
  "previous": null,
  "results": [{
    "id": 42,
    "nombre": "Nombre",
    "apellido": "Apellido",
    "dni": "12345678",
    "fecha_nacimiento": "2000-01-01",
    "sexo": "no especificado",
    "tipo_sangre": "O+",
    "alergias": ["Penicilina"],
    "condiciones_cronicas": [],
    "medicamentos_actuales": [],
    "contacto_emergencia": "Contacto ficticio",
    "telefono_emergencia": "900000000",
    "observaciones_medicas": "Datos sintéticos; verificar con el paciente.",
    "informacion_medica_actualizada": "2026-03-01"
  }]
}
```

Values above illustrate the schema and are fictitious. Optional/missing DNI and
birth date and medical text/date fields are serialized as `null`; medical list
fields are empty arrays when not recorded. The result also includes the
patient's allergy, chronic-condition, medication, emergency-contact,
observation, and medical-information update fields. No results returns `200` with an empty
`results` array. Empty, overly long, too-short, or partial numeric DNI queries
return `400`.

## Patient detail

### `GET /api/medico/pacientes/{paciente_id}/`

Requires `paciente.leer`. Returns the same patient object as an item in the
search response; an unknown patient profile returns `404`.

## Patient antecedentes

### `GET /api/medico/pacientes/{paciente_id}/antecedentes/`

Requires `paciente.leer`. Returns all five type groups, including empty arrays:

```json
{
  "alergia": [],
  "patologia": [],
  "cirugia": [],
  "familiar": [],
  "medicamento": []
}
```

Each item contains `id`, `tipo`, `descripcion`, `fecha_aproximada`, `activo`,
`registrado_por` (doctor id/name or `null`), and `creado_en`. An unknown
patient profile returns `404`.

## Patient atenciones

### `GET /api/medico/pacientes/{paciente_id}/atenciones/?desde=YYYY-MM-DD&hasta=YYYY-MM-DD&page=1`

Requires `paciente.leer`. Returns newest `fecha_atencion` first, paginated at
20 items per page. `desde` and `hasta` are optional and inclusive. Invalid
dates or `desde > hasta` return `400`; an unknown patient profile returns
`404`.

### `POST /api/medico/pacientes/{paciente_id}/atenciones/`

Requires active medical role and `paciente.editar`. Required non-empty text
fields are `motivo_consulta`, `diagnostico`, and `tratamiento`. Accepted
optional fields: `fecha_atencion`, `presion_arterial`, `frecuencia_cardiaca`,
`temperatura`, `saturacion_o2`, `peso_kg`, `talla_cm`, `codigo_cie10`, and
`notas`.

The server ignores caller-supplied patient/doctor identifiers and gets the
patient from the URL and doctor from the authenticated account. Success returns
`201` with the created record; validation failures return `400`.

Input bounds are defensive API validation limits, not clinical advice:

| Field | Accepted format/range |
| --- | --- |
| `presion_arterial` | `systolic/diastolic`; systolic 50–260, diastolic 30–160, systolic greater than diastolic |
| `frecuencia_cardiaca` | 20–250 bpm |
| `temperatura` | 25–45 °C |
| `saturacion_o2` | 0–100% |
| `peso_kg` | 0.1–500 kg |
| `talla_cm` | 20–250 cm |

## Common errors and access controls

- `401`: missing, invalid, expired credentials.
- `403`: account is inactive, not a medical user, has no medical profile, or
  lacks the permission required for that operation.
- `404`: requested patient detail/antecedentes resource does not exist.
- `400`: invalid query/date/attention payload; response body gives field errors.

All authorization is enforced server-side from the linked Django account,
`usuarios_usuario.rol`, `usuarios_usuario.permisos`, and presence of a
`usuarios_medico` row. Do not rely on mobile-side permission checks.

## Endpoints still pending

The following legacy mobile profile endpoints are not implemented by this
backend and remain pending integration:

- `GET /api/patient/profile/` — HU-03, current patient's personal/medical
  profile.
- `PUT /api/patient/profile/` — HU-04, updates to the current patient's
  permitted profile fields.

They are deliberately not aliases for the medical patient-detail route; their
authorization and response contracts are different.
