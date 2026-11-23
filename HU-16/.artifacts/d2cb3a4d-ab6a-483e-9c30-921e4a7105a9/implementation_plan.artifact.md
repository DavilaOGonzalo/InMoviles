# Implementation Plan - Missing Features (HU-10 to HU-16)

Complete the implementation of remaining user stories (HU-10 through HU-16) for Hospital location search, medical services, service availability updates by health staff, available appointment slots, appointment booking, viewing scheduled appointments, and appointment cancellation, using pure **Material 3** and **Experimental Material 3** components.

## User Review Required

> [!IMPORTANT]
> All new screens, components, and interactions will be built using pure Jetpack Compose Material 3 and Experimental Material 3 (`@OptIn(ExperimentalMaterial3Api::class)`), matching the established app architecture and design system.

## Proposed Changes

### Data & Repository Layer
#### [NEW] [HospitalServicesAndAppointmentsDataSource.kt](file:///C:/Users/Pedro/Downloads/InMoviles-suarez/InMoviles-suarez/HU-16/app/src/main/java/com/essaid/user/data/remote/HospitalServicesAndAppointmentsDataSource.kt)
- Data sources and repositories for medical services, hospital location/region filtering, available time slots (`BloqueHorario`), booking appointments, fetching scheduled appointments (`MisCitasOutcome`), and cancelling appointments.

### Presentation & UI Layer (Material 3 & Experimental M3)
#### [MODIFY/NEW] [HospitalDirectoryScreen.kt](file:///C:/Users/Pedro/Downloads/InMoviles-suarez/InMoviles-suarez/HU-16/app/src/main/java/com/essaid/user/presentation/hospital/HospitalDirectoryScreen.kt)
- Enhance with location/region search filter (HU-10) using Material 3 SearchBar / OutlinedTextField and FilterChips.
- Add navigation to view medical services available at each hospital (HU-11).
- Add health staff view to toggle service availability (HU-12).

#### [NEW] [CitasAndServicesScreens.kt](file:///C:/Users/Pedro/Downloads/InMoviles-suarez/InMoviles-suarez/HU-16/app/src/main/java/com/essaid/user/presentation/citas/CitasAndServicesScreens.kt)
- Screen for consulting available time slots (HU-13).
- Screen/Dialog for booking a medical appointment (HU-14).
- Screen for viewing scheduled appointments (HU-15).
- Dialog for cancelling an appointment with a reason (HU-16).

## Verification Plan

### Automated Tests
- Run unit tests for ViewModels and Repositories via gradle build.
### Manual Verification
- Deploy app to connected device and verify all 16 user stories through UI navigation.
