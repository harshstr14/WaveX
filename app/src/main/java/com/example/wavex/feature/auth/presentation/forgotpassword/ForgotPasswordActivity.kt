package com.example.wavex.feature.auth.presentation.forgotpassword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import com.example.wavex.ui.theme.WaveXTheme

class ForgotPasswordActivity : ComponentActivity() {
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = 0xFF121212.toInt(),
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        setContent {
            WaveXTheme {
                ForgotPasswordScreen(
                    uiState = viewModel.uiState.collectAsState().value,
                    onSendResetLink = { email ->
                        viewModel.sendResetLink(
                            email.trim()
                        )
                    },
                    onClearSnackbar = {
                        viewModel.clearMessages()
                    }
                )
            }
        }
    }
}