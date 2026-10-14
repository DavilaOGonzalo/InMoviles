# USER-HU-01 — Contrato e integración pendiente

## Alcance confirmado en el Excel

- Historia: `HU-01`, paciente: “Como paciente, quiero registrarme en EssAid
  para poder acceder a sus funcionalidades”.
- `CA-01`: permitir ingresar datos personales para crear la cuenta.
- `CA-02`: permitir un correo Gmail de Tecsup. La validación compatible con el
  backend acepta `@gmail.com` y `@tecsup.edu.pe`.
- Este proyecto no implementa los campos ni la operación de `ADMIN-HU-01`.

## Campos de USER-HU-01

Se tomaron del formulario público de registro del backend Django inspeccionado,
no del formulario administrativo:

| Campo API | Tipo | Regla cliente |
| --- | --- | --- |
| `email` | string | Obligatorio; termina en `gmail.com` o `tecsup.edu.pe`. |
| `password1` | string | Obligatorio; mínimo 8 caracteres y no solo números. |
| `password2` | string | Obligatorio; coincide con `password1`. |
| `tipo_documento` | `DNI`/`CE` | Obligatorio. |
| `numero_documento` | string | Obligatorio; máximo 20 caracteres. |
| `caracter_verificador` | string | Obligatorio para DNI; vacío para CE. |
| `fecha_nacimiento` | `YYYY-MM-DD` | Obligatoria y no futura. |
| `celular` | string | Obligatorio; máximo 20 caracteres. |
| `terminos_aceptados` | boolean | Debe ser `true`. |
| `tratamiento_datos_autorizado` | boolean | Debe ser `true`. |

No se envían `first_name`, `last_name` ni `rol`: pertenecen a la pantalla
administrativa y no a USER-HU-01.

## Endpoint verificado y limitación actual

El código Django vecino registra la ruta:

```text
POST /registro/
Content-Type: application/x-www-form-urlencoded
```

Es un formulario HTML protegido por CSRF: primero requiere `GET /registro/`,
obtener la cookie/token y luego enviar los campos. Un éxito redirige a
`/registro/completado/`; los errores vuelven a HTML. No se encontró un endpoint
JSON público acordado para Android ni códigos JSON de duplicado.

`DjangoRegistrationHttpClient` implementa únicamente ese contrato existente,
sin inventar una ruta JSON. El adaptador no se activa por defecto porque la
integración móvil aún debe acordar formato JSON, autenticación, códigos
`400/409` y respuesta de éxito con el backend responsable.

## Configuración

- Por defecto: `USE_MOCK_API=true`. El mock es temporal, vive en memoria y no
  guarda contraseñas.
- Cliente real: `-Pessaid.useMockApi=false
  -Pessaid.apiBaseUrl=https://servidor-de-desarrollo`.
- En cuanto exista el contrato JSON, se debe reemplazar solo la fuente remota
  por el cliente acordado y conservar `Repository`, `ViewModel` y la UI.

## Mapeo de estados

El `ViewModel` muestra carga, éxito, validación local/remota, duplicado y fallo
de red. Los duplicados del formulario actual se reconocen desde el error HTML
de correo/documento; cuando el backend publique JSON deben mapearse a la misma
salida `RegistrationOutcome.Duplicate`.
