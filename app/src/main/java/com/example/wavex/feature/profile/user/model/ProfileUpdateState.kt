package com.example.wavex.feature.profile.user.model

sealed interface ProfileUpdateState {
    data object Idle : ProfileUpdateState
    data object Loading : ProfileUpdateState
    data class Success(val message: String) : ProfileUpdateState
    data class Error(val message: String) : ProfileUpdateState
}