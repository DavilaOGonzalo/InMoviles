# EssAid — Android User / USER-HU-01

Aplicación Android Kotlin para el registro de pacientes de EssAid. La carpeta
implementa únicamente la tabla **USER** del Excel de backlog; no contiene el
flujo `ADMIN-HU-01`.

## Estado de integración

El backend revisado expone `POST /registro/` como formulario Django con CSRF y
respuesta HTML. Aún no existe un contrato JSON confirmado para Android. Por
eso la aplicación usa por defecto un `MockUserRegistrationDataSource` temporal,
claramente indicado en la pantalla, y conserva un adaptador HTTP opt-in para el
endpoint existente. La decisión pendiente y la configuración están en
[`docs/API_INTEGRATION.md`](docs/API_INTEGRATION.md).

## Ejecutar

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Para probar el adaptador HTTP contra el Django local, sin guardar secretos en
el proyecto:

```powershell
.\gradlew.bat assembleDebug -Pessaid.useMockApi=false -Pessaid.apiBaseUrl=https://servidor-de-desarrollo
```

La contraseña solo vive en memoria durante el envío, no se guarda en
preferencias/base local ni se escribe en logs. El hash de la contraseña debe
seguir siendo responsabilidad del backend.
