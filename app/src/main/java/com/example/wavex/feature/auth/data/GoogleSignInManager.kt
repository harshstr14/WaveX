package com.example.wavex.feature.auth.data

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.wavex.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GoogleSignInManager(context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val credentialManager = CredentialManager.create(context)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(
            context.getString(R.string.web_client_id)
        )
        .setFilterByAuthorizedAccounts(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    suspend fun signIn(
        activity: Activity,
        onSuccess: (FirebaseAuth) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )

            handleCredential(
                result,
                onSuccess,
                onError
            )

        } catch (e: Exception) {
            onError(e.message ?: "Sign in failed")
        }
    }

    private fun handleCredential(
        result: GetCredentialResponse,
        onSuccess: (FirebaseAuth) -> Unit,
        onError: (String) -> Unit
    ) {

        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            val googleCredential =
                GoogleIdTokenCredential.createFrom(
                    credential.data
                )

            firebaseAuthWithGoogle(
                googleCredential.idToken,
                onSuccess,
                onError
            )
        } else {
            onError("Invalid credential")
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: (FirebaseAuth) -> Unit,
        onError: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                createUserIfNeeded()
                onSuccess(auth)
            }
            .addOnFailureListener {
                onError("Authentication failed")
            }
    }

    private fun createUserIfNeeded() {
        val user = auth.currentUser ?: return
        val userID = user.uid

        val userRef = database.child("Users").child(userID)
        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val data = mapOf(
                    "name" to user.displayName,
                    "mail" to user.email,
                    "photoUrl" to user.photoUrl?.toString()?.replace("s96-c", "s400-c")
                )
                userRef.setValue(data)
            }
        }
    }

    suspend fun signOut() {
        auth.signOut()

        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
    }
}