# EssAid — Integración de API (HU-01 a HU-04)

La aplicación separa las pantallas de los servicios HTTP mediante repositorios
y fuentes de datos. `USE_MOCK_API=true` está habilitado por defecto, por lo que
las historias pueden probarse sin un backend disponible.

## Endpoints y estado de integración

| Historia | Método y ruta | Estado / contrato |
| --- | --- | --- |
| HU-01 — Registro | `GET /registro/`, `POST /registro/` | Formulario Django HTML existente; requiere cookie y token CSRF. El POST exitoso redirige a `/registro/completado/`. |
| HU-02 — Inicio de sesión | `POST /api/login/` | Ruta y formato JSON/formulario por confirmar con el backend. El cliente actual envía `email` y `password` como formulario URL-encoded. |
| HU-03 — Consulta de perfil | `GET /api/patient/profile/` | Ruta configurada en el cliente; confirmar contrato y forma de respuesta JSON con el backend. |
| HU-04 — Edición de perfil | `PUT /api/patient/profile/` | Ruta configurada en el cliente; confirmar contrato, campos y respuesta con el backend. |

Los endpoints de HU-02 a HU-04 son las rutas esperadas por la aplicación, no
una confirmación de que estén desplegados. Antes de desactivar el modo mock,
el equipo debe verificar las rutas, los formatos y los códigos de respuesta
con el backend. En particular, el adaptador HTTP de perfil todavía no
deserializa una respuesta JSON real; los datos de perfil que construye son
provisionales.

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
