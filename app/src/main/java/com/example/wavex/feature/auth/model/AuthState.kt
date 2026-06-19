package com.example.wavex.feature.auth.model

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(
        val message: String
    ) : AuthState
    data class Error(
        val message: String
    ) : AuthState
}