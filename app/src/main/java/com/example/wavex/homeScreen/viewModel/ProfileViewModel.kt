package com.example.wavex.homeScreen.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.profileScreen.settingScreen.SettingsDataStore
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
    application: Application,
    private val settingsDataStore: SettingsDataStore
) : AndroidViewModel(application) {

    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Users")

    val profileImageUrl = settingsDataStore.profileUrlFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val userName = settingsDataStore.userNameFlow
        .map { it ?: "Your Name" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Your Name"
        )

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress = _uploadProgress.asStateFlow()

    fun updateProgress(progress: Float) {
        _uploadProgress.value = progress
    }

    fun resetProgress() {
        _uploadProgress.value = 0f
    }

    fun setUploading(value: Boolean) {
        _isUploading.value = value
    }

    fun refreshUserData(uid: String) {
        database.child(uid).get()
            .addOnSuccessListener { snapshot ->
                val newUrl =
                    snapshot.child("photoUrl")
                        .getValue(String::class.java)

                val newName =
                    snapshot.child("name")
                        .getValue(String::class.java)

                viewModelScope.launch {
                    newUrl?.takeIf { it.isNotBlank() }?.let {
                        settingsDataStore.saveProfileUrl(it)
                    }

                    newName?.takeIf { it.isNotBlank() }?.let {
                        settingsDataStore.saveUserName(it)
                    }
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
}