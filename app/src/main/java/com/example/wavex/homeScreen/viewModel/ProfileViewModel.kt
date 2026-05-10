package com.example.wavex.homeScreen.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.ProfilePrefs.getProfileUrl
import com.example.wavex.homeScreen.ProfilePrefs.getUserName
import com.example.wavex.homeScreen.ProfilePrefs.saveProfileUrl
import com.example.wavex.homeScreen.ProfilePrefs.saveUserName
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val appContext = application.applicationContext

    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Users")

    val profileImageUrl = getProfileUrl(appContext)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )

    val userName = getUserName(appContext)
        .map { it ?: "Your Name" }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            "Your Name"
        )

    fun silentRefresh(uid: String) {
        database.child(uid).get().addOnSuccessListener { snapshot ->

            val newUrl = snapshot.child("photoUrl").getValue(String::class.java)
            val newName = snapshot.child("name").getValue(String::class.java)

            viewModelScope.launch {

                if (!newUrl.isNullOrEmpty()) {
                    saveProfileUrl(appContext, newUrl)
                }

                if (!newName.isNullOrEmpty()) {
                    saveUserName(appContext, newName)
                }
            }
        }
    }

    fun refreshProfileImage(uid: String) {
        database.child(uid).get().addOnSuccessListener { snapshot ->
            val newUrl = snapshot.child("photoUrl").getValue(String::class.java)
            viewModelScope.launch {
                if (!newUrl.isNullOrEmpty()) {
                    saveProfileUrl(getApplication(), newUrl)
                }
            }
        }
    }

    fun reloadProfileFromFirebase(uid: String) {
        database.child(uid).get().addOnSuccessListener { snapshot ->
            val newUrl = snapshot.child("photoUrl").getValue(String::class.java)
            val newName = snapshot.child("name").getValue(String::class.java)

            viewModelScope.launch {
                if (!newUrl.isNullOrEmpty()) saveProfileUrl(getApplication(), newUrl)
                if (!newName.isNullOrEmpty()) saveUserName(getApplication(), newName)
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

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
}