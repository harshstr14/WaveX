package com.example.wavex.feature.auth.model

data class SignUpUiState(
    val isLoading: Boolean = false,
    val navigateToVerifyEmail: Boolean = false,
    val navigateToHome: Boolean = false,
    val userName: String? = null,
    val email: String? = null,
    val errorMessage: String? = null
)