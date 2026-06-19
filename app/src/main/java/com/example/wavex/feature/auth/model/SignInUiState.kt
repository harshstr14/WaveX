package com.example.wavex.feature.auth.model

data class SignInUiState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val navigateToVerifyEmail: Boolean = false,
    val userName: String? = null,
    val email: String? = null,
    val errorMessage: String? = null
)