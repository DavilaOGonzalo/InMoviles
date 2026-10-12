# EssAid — Integración API (HU-01 y HU-02)

## Alcance Confirmado en el Backlog

- **HU-01 (Registro de Paciente)**: Permite registrarse mediante correo Tecsup/Gmail, documento de identidad y validaciones del backend.
- **HU-02 (Autenticación / Inicio de Sesión)**: Permite a los pacientes iniciar sesión con sus credenciales (`email` y `password`), gestionando tokens de sesión y estados de autenticación de forma segura.

---

## 1. USER-HU-01: Registro

- **Endpoint verificado**: `POST /registro/` (Formulario HTML con CSRF).
- **Mock predeterminado**: `MockUserRegistrationDataSource` (en memoria).
- **Cliente HTTP real**: `DjangoRegistrationHttpClient`.

---

## 2. USER-HU-02: Autenticación / Login

- **Endpoint acordado**: `POST /api/login/` (o endpoint JSON equivalente expuesto por el backend responsable de usuarios/roles).
- **Mecanismo de Autenticación**: Token de sesión / JWT Bearer. El token y los datos de usuario (`AuthSession`) se almacenan **únicamente en memoria** a través de `UserLoginRepository` mediante `StateFlow`.
- **Seguridad de Contraseñas**: Las contraseñas **nunca** se almacenan en texto plano en preferencias locales, bases de datos locales ni se escriben en logs. El campo de contraseña en el formulario se limpia inmediatamente después de enviar la solicitud.

### Mapeo de Resultados de Login (`LoginOutcome`)
1. **Success**: Retorna sesión válida (`AuthSession`) con token y datos de usuario. Navega a la pantalla protegida (`HomeScreen`).
2. **InvalidCredentials** (`401 Unauthorized`): Correo o contraseña incorrectos.
3. **InactiveAccount** (`403 Forbidden`): Cuenta inactiva. Requiere contacto con el administrador.
4. **ValidationError** (`400 Bad Request`): Errores de validación en los campos.
5. **NetworkError**: Fallo de conectividad (`IOException`).
6. **ServerError**: Error general del servidor.

---

## Configuración y Ejecución

- Por defecto: `USE_MOCK_API=true`.
- Cliente real: `-Pessaid.useMockApi=false -Pessaid.apiBaseUrl=https://servidor-de-desarrollo`.
