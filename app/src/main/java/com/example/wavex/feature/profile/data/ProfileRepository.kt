package com.example.wavex.feature.profile.data

import com.example.wavex.core.datastore.SettingsDataStore
import com.example.wavex.feature.profile.user.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class ProfileRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    firebaseDatabase: FirebaseDatabase,
    private val settingsDataStore: SettingsDataStore
) {
    private val database = firebaseDatabase.getReference("Users")

    private fun getCurrentUid(): String {
        return firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
    }

    suspend fun getUserProfile(): UserProfile {
        val snapshot = database
            .child(getCurrentUid())
            .get()
            .await()

        return UserProfile(
            name = snapshot.child("name")
                .getValue(String::class.java) ?: "",
            photoUrl = snapshot.child("photoUrl")
                .getValue(String::class.java) ?: "",
            mail = snapshot.child("mail")
                .getValue(String::class.java) ?: "",
            phoneNo = snapshot.child("phoneNo")
                .getValue(String::class.java) ?: "",
            gender = snapshot.child("gender")
                .getValue(String::class.java) ?: ""
        )
    }

    suspend fun updateProfile(
        name: String,
        phone: String,
        gender: String
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()

            name.takeIf { it.isNotBlank() }
                ?.let { updates["name"] = it }

            phone.takeIf { it.isNotBlank() }
                ?.let { updates["phoneNo"] = it }

            gender.takeIf { it.isNotBlank() }
                ?.let { updates["gender"] = it }

            if (updates.isNotEmpty()) {
                database
                    .child(getCurrentUid())
                    .updateChildren(updates)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshUserData() {
        val profile = getUserProfile()

        profile.photoUrl.takeIf { it.isNotBlank() }
            ?.let {
                settingsDataStore.saveProfileUrl(it)
            }

        profile.name.takeIf { it.isNotBlank() }
            ?.let {
                settingsDataStore.saveUserName(it)
            }

        profile.mail.takeIf { it.isNotBlank() }
            ?.let {
                settingsDataStore.saveUserEmail(it)
            }

        profile.gender.takeIf { it.isNotBlank() }
            ?.let {
                settingsDataStore.saveUserGender(it)
            }

        profile.phoneNo.takeIf { it.isNotBlank() }
            ?.let {
                settingsDataStore.saveUserPhoneNo(it)
            }
    }

    val profileImageUrlFlow = settingsDataStore.profileUrlFlow
    val userNameFlow = settingsDataStore.userNameFlow
    val userEmailFLow = settingsDataStore.userEmailFlow
    val userGenderFlow = settingsDataStore.userGenderFlow
    val userPhoneNoFlow = settingsDataStore.userPhoneNoFlow
}