package com.example.wavex.feature.auth.model

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)