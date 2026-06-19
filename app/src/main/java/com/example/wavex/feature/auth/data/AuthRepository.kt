package com.example.wavex.feature.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email,password).await()

            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email,password).await()

            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginAsGuest(): Result<Unit> {
        return try {
            val result = auth.signInAnonymously().await()

            val user = result.user
                    ?: throw Exception("User not found")

            val guestName = "Guest${user.uid.takeLast(4)}"

            database
                .child("Guest")
                .child(user.uid)
                .setValue(
                    mapOf(
                        "name" to guestName
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(
        email: String
    ): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isEmailVerified(): Boolean {
        val user = auth.currentUser ?: return false

        user.reload().await()

        return user.isEmailVerified
    }

    suspend fun saveVerifiedUser(name: String, email: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: throw Exception("User not found")

            database
                .child("Users")
                .child(userId)
                .setValue(
                    mapOf(
                        "name" to name,
                        "mail" to email
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun currentUser() = auth.currentUser
}