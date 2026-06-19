package com.example.wavex.feature.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.auth.data.AuthRepository
import com.example.wavex.feature.auth.model.SignUpUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())

    val uiState = _uiState.asStateFlow()

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value =
                uiState.value.copy(
                    isLoading = true
                )

            repository.register(email, password)
                .onSuccess { user ->
                    user.sendEmailVerification().await()

                    _uiState.value =
                        uiState.value.copy(
                            isLoading = false,
                            navigateToVerifyEmail = true,
                            userName = name,
                            email = email
                        )
                }
                .onFailure {
                    _uiState.value =
                        uiState.value.copy(
                            isLoading = false,
                            errorMessage =
                                it.message
                        )
                }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            repository.loginAsGuest()
                .onSuccess {
                    _uiState.value =
                        uiState.value.copy(
                            navigateToHome = true
                        )
                }
                .onFailure {
                    _uiState.value =
                        uiState.value.copy(
                            errorMessage =
                                it.message
                        )
                }
        }
    }

    fun clearError() {
        _uiState.value =
            uiState.value.copy(
                errorMessage = null
            )
    }
}