package com.example.wavex.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.profile.data.ProfileRepository
import com.example.wavex.feature.profile.user.model.ProfileUpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {
    val profileImageUrl = repository.profileImageUrlFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val userName = repository.userNameFlow
        .map { it ?: "Your Name" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Your Name"
        )

    val userEmail = repository.userEmailFLow
        .map { it ?: "Your Email" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Your Email"
        )

    val userGender = repository.userGenderFlow
        .map { it ?: "Your Gender" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Your Gender"
        )

    val userPhoneNo = repository.userPhoneNoFlow
        .map { it ?: "Your Phone No" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Your Phone No"
        )

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress = _uploadProgress.asStateFlow()

    private val _updateState = MutableStateFlow<ProfileUpdateState>(
        ProfileUpdateState.Idle
    )
    val updateState = _updateState.asStateFlow()

    fun updateProfile(
        name: String,
        phone: String, gender: String
    ) {
        viewModelScope.launch {
            _updateState.value = ProfileUpdateState.Loading

            repository.updateProfile(
                name = name,
                phone = phone,
                gender = gender
            ).fold(
                onSuccess = {
                    _updateState.value =
                        ProfileUpdateState.Success("Profile updated successfully")

                    refreshUserData()
                },
                onFailure = {
                    _updateState.value =
                        ProfileUpdateState.Error(
                            it.message ?: "Failed to update profile"
                        )
                }
            )
        }
    }

    fun updateProgress(progress: Float) {
        _uploadProgress.value = progress
    }

    fun resetProgress() {
        _uploadProgress.value = 0f
    }

    fun setUploading(value: Boolean) {
        _isUploading.value = value
    }

    fun refreshUserData() {
        viewModelScope.launch {
            try {
                repository.refreshUserData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}