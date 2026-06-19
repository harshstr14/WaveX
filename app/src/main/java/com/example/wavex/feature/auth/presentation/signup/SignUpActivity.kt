package com.example.wavex.feature.auth.presentation.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.lifecycleScope
import com.example.wavex.MainScreen
import com.example.wavex.R
import com.example.wavex.feature.auth.data.GoogleSignInManager
import com.example.wavex.feature.profile.presentation.ProfileViewModel
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

val fonts = FontFamily(
    Font(R.font.merriweathersans_bold, FontWeight.Bold),
    Font(R.font.merriweathersans_semibold, FontWeight.SemiBold),
    Font(R.font.merriweathersans_regular, FontWeight.Normal),
    Font(R.font.merriweathersans_medium, FontWeight.Medium)
)

class SignUpActivity : ComponentActivity() {
    private val viewModel: SignUpViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var googleSignInManager: GoogleSignInManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleSignInManager = GoogleSignInManager(this)

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
                SignUpScreen(
                    uiState = viewModel.uiState.collectAsState().value,
                    onSignUp = { name, email, password ->
                        viewModel.signUp(
                            name = name.trim(),
                            email = email.trim(),
                            password = password.trim()
                        )
                    },
                    onRefreshUser = {
                        profileViewModel.refreshUserData()
                    },
                    onSignInWithGoogle = {
                        lifecycleScope.launch {
                            googleSignInManager.signIn(
                                activity = this@SignUpActivity,
                                onSuccess = { auth ->

                                    FirebaseMessaging.getInstance()
                                        .token
                                        .addOnSuccessListener { token ->
                                            Log.d("FCM", token)

                                            // uploadTokenToBackend(token)
                                        }

                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "Welcome ${auth.currentUser?.displayName}",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(
                                        this@SignUpActivity,
                                        MainScreen::class.java
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }

                                    profileViewModel.refreshUserData()
                                    startActivity(intent)
                                },
                                onError = { message ->
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    },
                    onContinueWithGuest = {
                        viewModel.continueAsGuest()
                    },
                    onClearSnackbar = {
                        viewModel.clearError()
                    }
                )
            }
        }
    }
}