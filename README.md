# EssAid — Aplicación Android EssAid (HU-01 a MED-HU-06)

Aplicación Android desarrollada en Kotlin y Jetpack Compose para registro,
autenticación, perfil de paciente y flujos del profesional médico. El apartado
que se muestra depende del rol devuelto por el backend: médicos acceden a
búsqueda de pacientes y consulta de antecedentes; pacientes, a su propio perfil.

## Estado de integración

El modo mock está activo por defecto para probar los flujos sin conexión al
backend. El backend Django/DRF y el contrato están en `backend\`; consulta
[`docs/API_INTEGRATION.md`](docs/API_INTEGRATION.md) para rutas, flujos,
autorización y endpoints pendientes. HU-03/04 del perfil aún no están
implementadas en ese backend.

En modo mock, selecciona el apartado **Médico** e inicia sesión con
`medico.prueba@tecsup.edu.pe`, o selecciona **Paciente** e inicia sesión con
`paciente.prueba@tecsup.edu.pe`. En ambos casos la contraseña es
`EssAidTest!2026`. Para autenticar contra las cuentas cargadas desde
`mihospital_db.sql`, desactiva el mock y apunta la app al backend con
`-Pessaid.useMockApi=false -Pessaid.apiBaseUrl=http://10.0.2.2:8000`.

La búsqueda mock y el dump incluyen cinco pacientes completamente ficticios,
con DNI `88000001` a `88000005`. Para que la búsqueda API devuelva
esos mismos perfiles, importa el dump en una base local y conecta la app al
backend con el modo mock desactivado.

El dump `mihospital_db.sql` ya incluía roles, permisos y perfiles de médicos y
pacientes. Se actualizó con las tablas de antecedentes y atenciones. No se
activaron cuentas médicas ni se añadieron datos personales ficticios.

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
