package com.essaid.user.presentation.registration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.essaid.user.domain.model.RegistrationField
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    demoMode: Boolean,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.status) {
        if (state.status == RegistrationStatus.Success) {
            delay(1200)
            val registeredEmail = viewModel.lastRegisteredEmail.ifBlank { state.form.email }
            onRegistrationSuccess(registeredEmail)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "EssAid",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    TextButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text("Iniciar Sesión")
                    }
                }
                Text(
                    text = "Crear cuenta de paciente",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Regístrate para acceder a tus funcionalidades de salud.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                StatusBanner(state.status, state.message)

                RegistrationTextField(
                    value = state.form.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = "Correo electrónico",
                    error = state.errorFor(RegistrationField.Email),
                    keyboardType = KeyboardType.Email,
                )
                RegistrationTextField(
                    value = state.form.password1,
                    onValueChange = viewModel::onPasswordChanged,
                    label = "Contraseña",
                    error = state.errorFor(RegistrationField.Password1),
                    keyboardType = KeyboardType.Password,
                    password = true,
                )
                RegistrationTextField(
                    value = state.form.password2,
                    onValueChange = viewModel::onPasswordConfirmationChanged,
                    label = "Confirmar contraseña",
                    error = state.errorFor(RegistrationField.Password2),
                    keyboardType = KeyboardType.Password,
                    password = true,
                )

                DocumentTypeField(
                    selected = state.form.tipoDocumento,
                    onSelected = viewModel::onDocumentTypeChanged,
                    error = state.errorFor(RegistrationField.TipoDocumento),
                )
                RegistrationTextField(
                    value = state.form.numeroDocumento,
                    onValueChange = viewModel::onDocumentNumberChanged,
                    label = "Número de documento",
                    error = state.errorFor(RegistrationField.NumeroDocumento),
                    keyboardType = KeyboardType.Ascii,
                )
                if (state.form.tipoDocumento == "DNI") {
                    RegistrationTextField(
                        value = state.form.caracterVerificador,
                        onValueChange = viewModel::onVerifierChanged,
                        label = "Carácter verificador del DNI",
                        error = state.errorFor(RegistrationField.CaracterVerificador),
                        keyboardType = KeyboardType.Ascii,
                    )
                }
                RegistrationTextField(
                    value = state.form.fechaNacimiento,
                    onValueChange = viewModel::onBirthDateChanged,
                    label = "Fecha de nacimiento (AAAA-MM-DD)",
                    error = state.errorFor(RegistrationField.FechaNacimiento),
                    keyboardType = KeyboardType.Ascii,
                )
                RegistrationTextField(
                    value = state.form.celular,
                    onValueChange = viewModel::onPhoneChanged,
                    label = "Celular",
                    error = state.errorFor(RegistrationField.Celular),
                    keyboardType = KeyboardType.Phone,
                )

                ConsentRow(
                    checked = state.form.terminosAceptados,
                    onCheckedChange = viewModel::onTermsChanged,
                    text = "Acepto los términos y condiciones de uso.",
                    error = state.errorFor(RegistrationField.TerminosAceptados),
                )
                ConsentRow(
                    checked = state.form.tratamientoDatosAutorizado,
                    onCheckedChange = viewModel::onDataProcessingChanged,
                    text = "Autorizo el tratamiento de mis datos personales.",
                    error = state.errorFor(RegistrationField.TratamientoDatosAutorizado),
                )

                state.errorFor(RegistrationField.General)?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                if (state.status == RegistrationStatus.Success) {
                    Button(
                        onClick = {
                            val registeredEmail = viewModel.lastRegisteredEmail.ifBlank { state.form.email }
                            onRegistrationSuccess(registeredEmail)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                    ) {
                        Text("Ir a Iniciar Sesión")
                    }
                } else {
                    Button(
                        onClick = viewModel::submit,
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Crear cuenta")
                        }
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}



@Composable
private fun StatusBanner(status: RegistrationStatus, message: String?) {
    if (message == null || status == RegistrationStatus.Loading) return

    val successful = status == RegistrationStatus.Success
    val containerColor = if (successful) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (successful) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType,
    password: Boolean = false,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (password) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        },
                    )
                }
            }
        } else {
            null
        },
        supportingText = error?.let { text ->
            { Text(text) }
        },
    )
}

@Composable
private fun DocumentTypeField(
    selected: String,
    onSelected: (String) -> Unit,
    error: String?,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected) {
        "DNI" -> "DNI"
        "CE" -> "Carné de extranjería"
        else -> "Selecciona un tipo de documento"
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, color = if (selected.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                })
                Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Seleccionar documento")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("DNI") },
                onClick = {
                    expanded = false
                    onSelected("DNI")
                },
            )
            DropdownMenuItem(
                text = { Text("Carné de extranjería") },
                onClick = {
                    expanded = false
                    onSelected("CE")
                },
            )
        }
    }
    FieldErrorText(error)
}

@Composable
private fun ConsentRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    error: String?,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) },
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
            Text(
                text = text,
                modifier = Modifier.padding(top = 12.dp, end = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        FieldErrorText(error)
    }
}

@Composable
private fun FieldErrorText(error: String?) {
    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
