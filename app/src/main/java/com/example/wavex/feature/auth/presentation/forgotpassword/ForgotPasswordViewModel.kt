package com.example.wavex.feature.auth.presentation.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.auth.data.AuthRepository
import com.example.wavex.feature.auth.model.ForgotPasswordUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())

    val uiState = _uiState.asStateFlow()

    fun sendResetLink(email: String) {
        viewModelScope.launch {
            _uiState.value =
                ForgotPasswordUiState(
                    isLoading = true
                )

            repository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.value =
                        ForgotPasswordUiState(
                            successMessage =
                                "Password reset email sent"
                        )
                }
                .onFailure {
                    _uiState.value =
                        ForgotPasswordUiState(
                            errorMessage =
                                it.localizedMessage
                                    ?: "Something went wrong"
                        )
                }
        }
    }

    fun clearMessages() {
        _uiState.value =
            _uiState.value.copy(
                successMessage = null,
                errorMessage = null
            )
    }
}