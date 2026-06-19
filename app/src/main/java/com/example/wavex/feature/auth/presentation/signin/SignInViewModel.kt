package com.example.wavex.feature.auth.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.auth.data.AuthRepository
import com.example.wavex.feature.auth.model.SignInUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())

    val uiState = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value =
                uiState.value.copy(
                    isLoading = true
                )

            repository.login(email, password)
                .onSuccess { user ->
                    user.reload().await()

                    if (user.isEmailVerified) {
                        _uiState.value =
                            uiState.value.copy(
                                isLoading = false,
                                isLoginSuccess = true
                            )
                    } else {
                        user.sendEmailVerification().await()

                        _uiState.value =
                            uiState.value.copy(
                                isLoading = false,
                                navigateToVerifyEmail = true,
                                userName = user.displayName,
                                email = user.email
                            )
                    }
                }
                .onFailure {
                    _uiState.value =
                        uiState.value.copy(
                            isLoading = false,
                            errorMessage =
                                it.localizedMessage
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