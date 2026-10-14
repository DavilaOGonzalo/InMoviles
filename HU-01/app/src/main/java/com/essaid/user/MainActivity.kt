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
import com.essaid.user.di.AppContainer
import com.essaid.user.presentation.registration.RegisterScreen
import com.essaid.user.presentation.registration.RegisterViewModel
import com.essaid.user.ui.theme.EssAidTheme

class MainActivity : ComponentActivity() {
    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModel.Factory(AppContainer.registrationRepository)
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
                    RegisterScreen(
                        viewModel = registerViewModel,
                        demoMode = BuildConfig.USE_MOCK_API,
                    )
                }
            }
        }
    }
}
