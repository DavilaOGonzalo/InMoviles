package com.essaid.user.presentation.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.CitaRepository
import com.essaid.user.domain.model.BloqueHorario
import com.essaid.user.domain.model.DoctorInfo
import com.essaid.user.domain.model.EspecialidadMedica
import com.essaid.user.domain.model.Hospital
import com.essaid.user.domain.model.MisCitasOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CitasUiState(
    val selectedStep: Int = 1, // 1: Hospital, 2: Especialidad, 3: Doctor, 4: Perfil y Horarios Doctor
    val hospitales: List<Hospital> = listOf(
        Hospital("H-001", "Hospital Norte EssAid", "Av. Industrial 123", "Norte", "01-555-0001", true),
        Hospital("H-002", "Hospital Sur EssAid", "Calle Los Pinos 456", "Sur", "01-555-0002", true),
        Hospital("H-003", "Hospital Este EssAid", "Av. Universitaria 789", "Este", "01-555-0003", true),
    ),
    val selectedHospital: Hospital? = null,
    val especialidades: List<EspecialidadMedica> = listOf(
        EspecialidadMedica("ESP-01", "Medicina General", "Atención primaria y prevención general"),
        EspecialidadMedica("ESP-02", "Cardiología", "Evaluación cardiovascular avanzada"),
        EspecialidadMedica("ESP-03", "Pediatría", "Atención integral infantil"),
        EspecialidadMedica("ESP-04", "Odontología", "Salud bucal y dental"),
    ),
    val selectedSpecialty: EspecialidadMedica? = null,
    val doctores: List<DoctorInfo> = emptyList(),
    val selectedDoctor: DoctorInfo? = null,
    val doctorDiasHorarios: List<Pair<LocalDate, List<BloqueHorario>>> = emptyList(),
    val selectedFecha: LocalDate = LocalDate.now(),
    val selectedHora: String? = null,
    val misCitas: MisCitasOutcome = MisCitasOutcome.Empty,
    val isLoading: Boolean = false,
    val message: String? = null,
    val successMessage: String? = null,
)

class CitasViewModel(
    private val repository: CitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitasUiState())
    val uiState: StateFlow<CitasUiState> = _uiState.asStateFlow()

    fun selectHospital(hospital: Hospital) {
        _uiState.value = _uiState.value.copy(
            selectedHospital = hospital,
            selectedStep = 2,
            selectedSpecialty = null,
            selectedDoctor = null,
        )
    }

    fun selectSpecialty(specialty: EspecialidadMedica) {
        val hospital = _uiState.value.selectedHospital
        val sampleDoctores = listOf(
            DoctorInfo(
                id = "DOC-101",
                nombre = "Dr. Carlos Mendoza",
                especialidad = specialty.nombre,
                hospitalId = hospital?.id ?: "H-001",
                hospitalNombre = hospital?.nombre ?: "Hospital Norte",
                colegiatura = "CMP 45892",
                biografia = "Especialista con más de 12 años de experiencia en el sistema EssAid, enfocado en atención preventiva y pacientes crónicos.",
                imageUrl = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=300",
            ),
            DoctorInfo(
                id = "DOC-102",
                nombre = "Dra. Ana Lucía Torres",
                especialidad = specialty.nombre,
                hospitalId = hospital?.id ?: "H-001",
                hospitalNombre = hospital?.nombre ?: "Hospital Norte",
                colegiatura = "CMP 51234",
                biografia = "Egresada de la Universidad Nacional Mayor de San Marcos con especialización en cardiología clínica.",
                imageUrl = "https://images.unsplash.com/photo-1594824813581-2292025c81d3?w=300",
            ),
        )
        _uiState.value = _uiState.value.copy(
            selectedSpecialty = specialty,
            doctores = sampleDoctores,
            selectedStep = 3,
            selectedDoctor = null,
        )
    }

    fun selectDoctor(doctor: DoctorInfo) {
        val today = LocalDate.now()
        val dias = (0..4).map { offset ->
            val fecha = today.plusDays(offset.toLong())
            val bloques = listOf(
                BloqueHorario("08:00", true),
                BloqueHorario("09:00", offset % 2 == 0),
                BloqueHorario("10:00", true),
                BloqueHorario("11:00", true),
                BloqueHorario("14:00", true),
                BloqueHorario("15:00", false),
                BloqueHorario("16:00", true),
            )
            fecha to bloques
        }
        _uiState.value = _uiState.value.copy(
            selectedDoctor = doctor,
            doctorDiasHorarios = dias,
            selectedFecha = today,
            selectedHora = null,
            selectedStep = 4,
        )
    }

    fun selectFecha(fecha: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedFecha = fecha, selectedHora = null)
    }

    fun selectHora(hora: String) {
        _uiState.value = _uiState.value.copy(selectedHora = hora)
    }

    fun goToStep(step: Int) {
        _uiState.value = _uiState.value.copy(selectedStep = step)
    }

    fun reservarCita(token: String, pacienteId: String, hospitalId: String, fecha: String, hora: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.reservarCita(token, pacienteId, hospitalId, fecha, hora)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "¡Cita reservada con éxito para el $fecha a las $hora!",
                    selectedStep = 1,
                    selectedHospital = null,
                    selectedSpecialty = null,
                    selectedDoctor = null,
                    selectedHora = null,
                )
                cargarMisCitas(token, pacienteId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = result.exceptionOrNull()?.message ?: "Error al reservar la cita",
                )
            }
        }
    }

    fun cargarMisCitas(token: String, pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val outcome = repository.obtenerCitasPaciente(token, pacienteId)
            _uiState.value = _uiState.value.copy(misCitas = outcome, isLoading = false)
        }
    }

    fun cancelarCita(token: String, citaId: String, motivo: String, pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val success = repository.cancelarCita(token, citaId, motivo)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    successMessage = "Cita cancelada correctamente.",
                )
                cargarMisCitas(token, pacienteId)
            } else {
                _uiState.value = _uiState.value.copy(
                    message = "No se pudo cancelar la cita.",
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(message = null, successMessage = null)
    }
}
