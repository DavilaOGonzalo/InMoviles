# Implementation Plan - USER-HU-02: User Authentication & Login

This plan details the implementation of **USER-HU-02 (Autenticación y Inicio de Sesión de Pacientes)** for EssAid, following the exact same MVVM architecture, clean domain/data layers, validation rules, secure memory handling, and HTTP client patterns established in USER-HU-01.

## User Review Required

- **Authentication Mechanism**: Session token management (Bearer token / session) securely stored in memory via `UserLoginRepository`, without storing passwords in plain text in preferences or local databases.
- **Error Handling**: Explicit handling of valid response/success, incorrect credentials (`401`), inactive account (`403`), network errors, and server errors.
- **Protected Screens**: Implementation of an authenticated home/dashboard screen (`HomeScreen`) that requires an active session, allowing navigation between Login, Register, and Protected Home.

## Proposed Changes

### Domain & Validation Layer
- [NEW] `app/src/main/java/com/essaid/user/domain/model/LoginModels.kt`
  - Defines `LoginForm`, `UserLoginRequest`, `AuthenticatedUser`, `AuthSession`, `LoginField`, and `LoginOutcome` sealed interface (Success, InvalidCredentials, InactiveAccount, ValidationError, NetworkError, ServerError).
- [NEW] `app/src/main/java/com/essaid/user/domain/validation/LoginValidator.kt`
  - Validates email format (`@gmail.com` or `@tecsup.edu.pe`) and non-empty password.

### Data Layer
- [NEW] `app/src/main/java/com/essaid/user/data/remote/UserLoginDataSource.kt`
  - Interface for login remote data source.
- [NEW] `app/src/main/java/com/essaid/user/data/remote/MockUserLoginDataSource.kt`
  - In-memory mock data source simulating success, invalid credentials, and inactive account states for local development and testing.
- [NEW] `app/src/main/java/com/essaid/user/data/remote/DjangoLoginHttpClient.kt`
  - HTTP client adapter for Django login endpoint (`POST /api/login/`) using `HttpURLConnection`.
- [NEW] `app/src/main/java/com/essaid/user/data/repository/UserLoginRepository.kt`
  - Repository managing login calls, exception handling, and in-memory session state (`StateFlow<AuthSession?>`).

### Presentation & UI Layer
- [NEW] `app/src/main/java/com/essaid/user/presentation/login/LoginViewModel.kt`
  - ViewModel managing UI state, validation, submitting requests, clearing password from state on submission, and outcome updates.
- [NEW] `app/src/main/java/com/essaid/user/presentation/login/LoginScreen.kt`
  - Jetpack Compose screen for login with email and password fields, validation errors, loading state, and navigation to registration.
- [NEW] `app/src/main/java/com/essaid/user/presentation/home/HomeScreen.kt`
  - Protected screen showing active user session details and logout button.
- [MODIFY] `app/src/main/java/com/essaid/user/MainActivity.kt`
  - Coordinates navigation between Register, Login, and Protected Home screens based on authentication session state.

### Dependency Injection
- [MODIFY] `app/src/main/java/com/essaid/user/di/AppContainer.kt`
  - Provides `userLoginRepository` instance (Mock vs real HTTP client based on `USE_MOCK_API`).

### Testing & Documentation
- [NEW] `app/src/test/java/com/essaid/user/domain/validation/LoginValidatorTest.kt`
- [NEW] `app/src/test/java/com/essaid/user/data/repository/UserLoginRepositoryTest.kt`
- [NEW] `app/src/test/java/com/essaid/user/presentation/login/LoginViewModelTest.kt`
- [MODIFY] `docs/API_INTEGRATION.md`
  - Document login endpoint (`POST /api/login/`), session handling, error mapping, and security guarantees.

## Verification Plan

### Automated Tests
- Run unit tests: `.\gradlew.bat testDebugUnitTest`
- Build app: `.\gradlew.bat assembleDebug`

### Manual Verification
- Deploy app to emulator/device and test login flow with valid credentials, invalid credentials, inactive account, and navigation between login, register, and protected home screen.
