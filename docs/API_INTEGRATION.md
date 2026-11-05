# EssAid — Integración de API (HU-01 a MED-HU-06)

La aplicación separa las pantallas de los servicios HTTP mediante repositorios
y fuentes de datos. `USE_MOCK_API=true` está habilitado por defecto, por lo que
las historias pueden probarse sin un backend disponible.

## Endpoints y estado de integración

| Historia | Método y ruta | Estado / contrato |
| --- | --- | --- |
| HU-01 — Registro | `GET /registro/`, `POST /registro/` | Formulario Django HTML existente; requiere cookie y token CSRF. El POST exitoso redirige a `/registro/completado/`. |
| HU-02 — Inicio de sesión | `POST /api/auth/token/` | JWT implementado en `backend/`; recibe JSON `email`/`password` y devuelve `access`/`refresh`. |
| HU-03 — Consulta de perfil | `GET /api/patient/profile/` | Ruta configurada en el cliente; confirmar contrato y forma de respuesta JSON con el backend. |
| HU-04 — Edición de perfil | `PUT /api/patient/profile/` | Ruta configurada en el cliente; confirmar contrato, campos y respuesta con el backend. |
| MED-HU-05 — Búsqueda de pacientes | `GET /api/medico/pacientes/?q=<consulta>` | Implementado en `backend/`; contrato documentado en `backend/API_CONTRACT.md`. |
| MED-HU-06 — Consulta de antecedentes | `GET /api/medico/pacientes/{paciente_id}/antecedentes/` | Integrado en Android; agrupado por tipo y protegido por `paciente.leer`. Contrato en `backend/API_CONTRACT.md`. |

El backend nuevo implementa autenticación JWT y MED-HU-05. Los endpoints de
perfil HU-03–04 aún no están implementados. El adaptador HTTP de perfil todavía
no deserializa una respuesta JSON
real; esos datos son provisionales.

MED-HU-06 consulta el paciente seleccionado por el ID del perfil de paciente
devuelto por la búsqueda. La respuesta incluye los grupos `alergia`,
`patologia`, `cirugia`, `familiar` y `medicamento`. El encabezado usa tipo de
sangre, sexo y fecha de nacimiento ya devueltos en la búsqueda; la edad se
calcula localmente cuando la fecha es válida. `401` indica sesión no autorizada,
`403` falta de permiso y `404` un perfil inexistente. Los errores de conexión
se pueden reintentar. En modo mock se muestran categorías vacías y se informa
que no se están consultando antecedentes reales.

## Flujos

### HU-01 — Registro

`RegisterScreen` valida los campos y envía la solicitud mediante
`UserRegistrationRepository`. El modo mock permite probar el flujo local. El
adaptador Django solicita primero el formulario, conserva la cookie, extrae el
token CSRF y envía los campos URL-encoded; interpreta la redirección de éxito y
los errores devueltos por el formulario HTML.

### HU-02 — Inicio de sesión y autenticación

`LoginScreen` valida el correo y la contraseña antes de que
`LoginViewModel` llame a `UserLoginRepository`. Un inicio exitoso crea una
sesión en memoria y abre la pantalla protegida; cerrar sesión elimina esa
sesión. La contraseña no se guarda en preferencias ni bases locales. El
contrato de éxito del adaptador HTTP debe conectarse a la respuesta real del
backend antes de usarlo contra un servicio desplegado.

Resultados reconocidos:

- `200` o `201`: inicio exitoso.
- `401`: credenciales incorrectas.
- `403`: cuenta inactiva.
- `400`: errores de validación.
- Error de red o de servidor: se informa en el estado de la pantalla.

### HU-03 — Consulta personal y médica

Con una sesión activa, `PatientProfileViewModel` solicita el perfil usando el
token de sesión. El servidor debe identificar al paciente a partir de esa
sesión y no de un identificador enviado por la app. El cliente no debe acceder
directamente a la base de datos ni permitir consultar perfiles de otros
pacientes.

El adaptador reconoce `200` (éxito), `404` (sin perfil), `400` (solicitud
inválida), `401`/`403` (sesión no autorizada) y errores de servidor o red.

### HU-04 — Actualización de perfil autorizado

La pantalla permite editar únicamente los siguientes campos:

- `celular` (obligatorio; 9 dígitos y empieza con 9).
- `contacto_emergencia_nombre` y `contacto_emergencia_telefono`.
- `alergias`, `condiciones_cronicas` y `medicamentos_actuales`.
- `observaciones_medicas`.

El correo, documento y otros datos identificativos permanecen de solo lectura.
`ProfileUpdateValidator` valida el formulario en el cliente y el backend debe
repetir la validación y aplicar los cambios exclusivamente al paciente
autenticado. La actualización usa `PUT /api/patient/profile/`; se espera
`200`/`202` para éxito, `400` para errores de validación, `401`/`403` para
sesión no autorizada y errores de servidor o red para los demás fallos.

## Autorización y configuración

Las solicitudes de perfil requieren el encabezado
`Authorization: Bearer <token-de-sesion>`. El token se conserva únicamente en
memoria mediante `UserLoginRepository`; no se escribe en logs ni en
almacenamiento persistente.

Para usar el modo mock (predeterminado):

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Para seleccionar los adaptadores HTTP y configurar la URL base:

```powershell
.\gradlew.bat assembleDebug -Pessaid.useMockApi=false -Pessaid.apiBaseUrl=https://servidor-de-desarrollo
```

No incluir credenciales ni tokens reales en el repositorio. El backend es
responsable del almacenamiento seguro y del hash de contraseñas.

## MED-HU-05 — Búsqueda de pacientes

El endpoint se implementa en `backend/` y su contrato está en
[`../backend/API_CONTRACT.md`](../backend/API_CONTRACT.md). La búsqueda
del cliente consume:

```http
GET /api/medico/pacientes/?q=ana
Authorization: Bearer <access>
Accept: application/json
```

`q` contiene un DNI exacto de 8 dígitos o al menos 2 caracteres de nombre o
apellido. La respuesta `200` está paginada:

```json
{
  "count": 1,
  "next": null,
  "previous": null,
  "results": [
    {
      "id": 9001,
      "nombre": "Nombre",
      "apellido": "Apellido",
      "dni": "00000001",
      "fecha_nacimiento": "2000-01-01",
      "sexo": "No especificado",
      "tipo_sangre": "No especificado"
    }
  ]
}
```

Los valores de ejemplo son ficticios. El cliente admite los campos opcionales
`fecha_nacimiento`, `sexo` y `tipo_sangre` como `null`. Respuestas vacías se
representan con `results: []`; `401` indica sesión no autorizada y `403` falta
de rol/permisos.

`PatientSearchViewModel` aplica 350 ms de debounce y validación de consulta;
`PatientSearchRepository` y su fuente de datos separan UI y transporte. En modo
mock aparecen únicamente pacientes de prueba ficticios (`9001`/`9002`). La
selección abre una ficha resumida usando el resultado de búsqueda; el backend
también ofrece `GET /api/medico/pacientes/{paciente_id}/` para detalle. La API
autoriza en servidor el rol médico activo y el permiso `paciente.leer`; el
cliente no sustituye esa autorización. El flujo de login ya consume el endpoint
JWT `POST /api/auth/token/`.

Después del login, la app muestra un solo apartado según `user.rol`: `medico`
abre búsqueda de pacientes y `paciente` abre su perfil. Un rol vacío o
desconocido no recibe acceso por defecto a ninguno. El dump conserva los roles
y perfiles existentes; las cuentas médicas inactivas no se activan
automáticamente.

## MED-HU-06 — Consulta de antecedentes

Al seleccionar un paciente, la sección de antecedentes consulta:

```http
GET /api/medico/pacientes/{paciente_id}/antecedentes/
Authorization: Bearer <access-jwt>
Accept: application/json
```

La respuesta contiene los cinco grupos del contrato documentado en
[`../backend/API_CONTRACT.md`](../backend/API_CONTRACT.md), incluso cuando
están vacíos. Cada antecedente muestra descripción, fecha aproximada, estado y
profesional que lo registró cuando esos datos existen. El acceso requiere un
usuario médico activo con permiso `paciente.leer`; el backend sigue siendo la
autoridad. No se crean antecedentes de ejemplo y el modo mock no consulta datos
reales.
