package com.example.wavex.feature.auth.presentation.signin

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
import com.example.wavex.MainScreen
import com.example.wavex.feature.auth.data.GoogleSignInManager
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.messaging.FirebaseMessaging
import androidx.lifecycle.lifecycleScope
import com.example.wavex.feature.profile.presentation.ProfileViewModel
import kotlinx.coroutines.launch

class SignInActivity : ComponentActivity() {
    private val viewModel: SignInViewModel by viewModels()
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
                SignInScreen(
                    uiState = viewModel.uiState.collectAsState().value,
                    onSignIn = { email, password ->
                        viewModel.signIn(
                            email.trim(),
                            password.trim()
                        )
                    },
                    onRefreshUser = {
                        profileViewModel.refreshUserData()
                    },
                    onSignInWithGoogle = {
                        lifecycleScope.launch {
                            googleSignInManager.signIn(
                                activity = this@SignInActivity,
                                onSuccess = { auth ->

                                    FirebaseMessaging.getInstance()
                                        .token
                                        .addOnSuccessListener { token ->
                                            Log.d("FCM", token)

                                            // uploadTokenToBackend(token)
                                        }

                                    Toast.makeText(
                                        this@SignInActivity,
                                        "Welcome ${auth.currentUser?.displayName}",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(
                                        this@SignInActivity,
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
                                        this@SignInActivity,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    },
                    onClearSnackbar = {
                        viewModel.clearError()
                    }
                )
            }
        }
    }
}