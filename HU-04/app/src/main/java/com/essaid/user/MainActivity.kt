package com.essaid.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.essaid.user.di.AppContainer
import com.essaid.user.presentation.home.HomeScreen
import com.essaid.user.presentation.login.LoginScreen
import com.essaid.user.presentation.login.LoginViewModel
import com.essaid.user.presentation.registration.RegisterScreen
import com.essaid.user.presentation.registration.RegisterViewModel
import com.essaid.user.ui.theme.EssAidTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModel.Factory(AppContainer.registrationRepository)
    }

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(AppContainer.loginRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.setBackgroundColor("#F8FAFB".toColorInt())
        setContent {
            EssAidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val session by AppContainer.loginRepository.currentSession.collectAsStateWithLifecycle()
                    var currentScreen by remember { mutableStateOf("login") }

                    when {
                        session != null -> {
                            HomeScreen(
                                session = session!!,
                                onLogout = {
                                    loginViewModel.logout()
                                    currentScreen = "login"
                                }
                            )
                        }
                        currentScreen == "register" -> {
                            RegisterScreen(
                                viewModel = registerViewModel,
                                demoMode = BuildConfig.USE_MOCK_API,
                                onNavigateBack = { currentScreen = "login" },
                                onRegistrationSuccess = { registeredEmail ->
                                    loginViewModel.prefillEmailAndNotice(
                                        email = registeredEmail,
                                        notice = "¡Registro exitoso! Inicia sesión con tu correo."
                                    )
                                    currentScreen = "login"
                                }
                            )
                        }
                        else -> {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onNavigateToRegister = { currentScreen = "register" },
                            )
                        }
                    }
                }
            }
        }
    }
}
