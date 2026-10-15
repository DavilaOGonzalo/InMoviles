# EssAid — Aplicación Android de pacientes (HU-01 a HU-04)

Aplicación Android desarrollada en Kotlin y Jetpack Compose para el registro,
autenticación y consulta/actualización de información personal y médica de
pacientes. La implementación usa capas de presentación, dominio y datos, con
repositorios y adaptadores HTTP intercambiables por fuentes mock.

## Estado de integración

El modo mock está activo por defecto para probar los flujos sin conexión al
backend. El registro usa el formulario Django `POST /registro/`; las rutas de
login y perfil están configuradas en los clientes, pero sus contratos deben
confirmarse con el backend antes de habilitar la integración real. Consulta
[`docs/API_INTEGRATION.md`](docs/API_INTEGRATION.md) para rutas, flujos,
autorización y limitaciones conocidas.

## Ejecutar pruebas y app

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Para seleccionar los adaptadores HTTP y configurar la URL base, sin guardar
credenciales en el proyecto:

```powershell
.\gradlew.bat assembleDebug -Pessaid.useMockApi=false -Pessaid.apiBaseUrl=https://servidor-de-desarrollo
```

Las contraseñas no se guardan en preferencias ni bases de datos locales. La
sesión de autenticación permanece en memoria y el hash de contraseñas es
responsabilidad del backend.
