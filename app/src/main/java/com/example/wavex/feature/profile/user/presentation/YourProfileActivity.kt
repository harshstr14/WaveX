package com.example.wavex.feature.profile.user.presentation

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.wavex.feature.profile.presentation.ProfileViewModel
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YourProfileActivity : ComponentActivity() {
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = 0xFF121212.toInt(),
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        setContent {
            WaveXTheme {
                YourProfileScreen(
                    userName = profileViewModel.userName.collectAsStateWithLifecycle().value,
                    imageUrl = profileViewModel.profileImageUrl.collectAsStateWithLifecycle().value,
                    userEmail = profileViewModel.userEmail.collectAsStateWithLifecycle().value,
                    userPhoneNo = profileViewModel.userPhoneNo.collectAsStateWithLifecycle().value,
                    userGender = profileViewModel.userGender.collectAsStateWithLifecycle().value,
                    onUpdateClick = { updatedName, updatedPhone, updatedGender->
                        profileViewModel.updateProfile(
                            name = updatedName,
                            phone = updatedPhone,
                            gender = updatedGender
                        )
                    },
                    isUploading = profileViewModel.isUploading.collectAsState().value,
                    updateState = profileViewModel.updateState.collectAsStateWithLifecycle().value,
                    onRefreshUser = {
                        profileViewModel.refreshUserData()
                    },
                    onResetProgress = {
                        profileViewModel.resetProgress()
                    },
                    onUpdateProgress = { float ->
                        profileViewModel.updateProgress(float)
                    },
                    onSetUploading = { boolean ->
                        profileViewModel.setUploading(boolean)
                    }
                )
            }
        }
    }
}

fun uploadToCloudinary(
    imageUri: Uri,
    onSetUploading: (Boolean) -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onRefreshUser: () -> Unit,
    onResetProgress: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("Users")

    onSetUploading(true)

    MediaManager.get().upload(imageUri)
        .option("folder", "profile_pics")
        .option("public_id", userId)
        .option("overwrite", true)
        .callback(object : UploadCallback {
            override fun onStart(requestId: String?) {
                Log.d("UPLOAD_DEBUG", "Upload started")
                onSetUploading(true)
                onUpdateProgress(0f)
            }

            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                val progress = bytes.toFloat() / totalBytes.toFloat()
                Log.d("UPLOAD_DEBUG", "Progress: $progress")
                onUpdateProgress(progress)
            }

            override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                onSetUploading(false)

                val secureUrl = resultData?.get("secure_url").toString()
                val version = resultData?.get("version").toString()

                val finalUrl = "$secureUrl?v=$version"

                database.child(userId).child("photoUrl").setValue(finalUrl)
                    .addOnSuccessListener {
                        onShowMessage("Profile photo updated")

                        onRefreshUser()
                        onResetProgress()
                    }
                    .addOnFailureListener { e ->
                        onResetProgress()
                        onShowMessage("Failed to update profile: ${e.message}")
                    }
            }

            override fun onError(requestId: String, p1: ErrorInfo) {
                onSetUploading(false)
                onResetProgress()

                onShowMessage("Upload failed: ${p1.description}")
            }

            override fun onReschedule(requestId: String, p1: ErrorInfo) {
                // You can leave this empty if you don’t need it
            }
        })
        .dispatch()
}