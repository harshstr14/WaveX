package com.example.wavex.feature.auth.presentation.verifyemail

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import com.example.wavex.feature.profile.presentation.ProfileViewModel
import com.example.wavex.ui.theme.WaveXTheme

class VerifyEmailActivity : ComponentActivity() {
    private val viewModel: VerifyEmailViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var name: String
    private lateinit var email: String

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

        name = intent.getStringExtra("name") ?: ""
        email = intent.getStringExtra("email") ?: ""

        Log.d("User Details", "$name, $email")

        setContent {
            WaveXTheme {
                VerifyEmailScreen(
                    email = email,
                    uiState = viewModel.uiState.collectAsState().value,
                    onResendEmail = {
                        viewModel.resendEmail()
                    },
                    onRefreshUser = {
                        profileViewModel.refreshUserData()
                    },
                    onVerifyEmail = {
                        viewModel.verifyEmail(name, email)
                    },
                    onClearSnackbar = {
                        viewModel.clearSnackbar()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.checkVerificationStatus(
            name = name,
            email = email
        )
    }
}