package com.example.wavex.feature.auth.model

data class VerifyEmailUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val canResend: Boolean = false,
    val secondsLeft: Int = 60,
    val snackBarMessage: String? = null,
    val navigateToHome: Boolean = false
)