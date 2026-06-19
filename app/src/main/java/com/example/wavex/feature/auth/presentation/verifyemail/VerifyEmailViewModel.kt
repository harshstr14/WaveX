package com.example.wavex.feature.auth.presentation.verifyemail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.auth.data.AuthRepository
import com.example.wavex.feature.auth.model.VerifyEmailUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class VerifyEmailViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyEmailUiState())

    val uiState = _uiState.asStateFlow()

    fun resendEmail() {
        viewModelScope.launch {
            repository
                .resendVerificationEmail()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            snackBarMessage =
                                "Verification email sent",
                            canResend = false,
                            secondsLeft = 60
                        )
                    }

                    startTimer()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            snackBarMessage =
                                "Failed to send verification email"
                        )
                    }
                }
        }
    }

    fun verifyEmail(name: String, email: String) {
        viewModelScope.launch {
            val verified = repository.isEmailVerified()

            if (!verified) {
                _uiState.update {
                    it.copy(
                        snackBarMessage =
                            "Email not verified yet"
                    )
                }

                return@launch
            }

            repository
                .saveVerifiedUser(name, email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            navigateToHome = true,
                            snackBarMessage =
                                "Email verified successfully"
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            snackBarMessage =
                                "Could not save user information"
                        )
                    }
                }
        }
    }

    fun clearSnackbar() {
        _uiState.update {
            it.copy(
                snackBarMessage = null
            )
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            for (i in 60 downTo 1) {
                _uiState.update {
                    it.copy(
                        secondsLeft = i,
                        canResend = false
                    )
                }

                delay(1000.milliseconds)
            }

            _uiState.update {
                it.copy(
                    canResend = true,
                    secondsLeft = 0
                )
            }
        }
    }

    fun checkVerificationStatus(name: String, email: String) {
        viewModelScope.launch {
            try {
                val verified = repository.isEmailVerified()

                if (!verified) return@launch

                repository
                    .saveVerifiedUser(
                        name = name,
                        email = email
                    )
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                navigateToHome = true
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update {
                            it.copy(
                                snackBarMessage =
                                    "Could not save user information"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        snackBarMessage =
                            e.message ?: "Something went wrong"
                    )
                }
            }
        }
    }
}