package com.example.wavex.feature.library.sheets.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class PlaylistEditorRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private fun playlistRef(): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw Exception("User not logged in")

        return firebaseDatabase
            .getReference("Users")
            .child(userId)
            .child("Favourites")
            .child("MyPlaylists")
    }

    suspend fun createPlaylist(title: String, description: String): Result<Unit> {
        return try {
            val playlistRef = playlistRef()

            val snapshot = playlistRef
                .orderByChild("playlistName")
                .equalTo(title.trim())
                .get()
                .await()

            if (snapshot.exists()) {
                return Result.failure(
                    Exception("Playlist Already Exists")
                )
            }

            val playlistId = playlistRef.push().key
                ?: return Result.failure(
                    Exception("Failed to generate playlist id")
                )

            val playlistData = hashMapOf<String, Any>(
                "playlistId" to playlistId,
                "playlistName" to title.trim(),
                "description" to description.trim(),
                "imageUrl" to "https://res.cloudinary.com/dcdg3s1pf/image/upload/v1774857863/default_image_jypeb6.jpg",
                "totalSongs" to 0
            )

            playlistRef.child(playlistId)
                .setValue(playlistData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renamePlaylist(playlistId: String, title: String, description: String): Result<Unit> {
        return try {
            val playlistRef = playlistRef()

            val snapshot = playlistRef
                .orderByChild("playlistName")
                .equalTo(title.trim())
                .get()
                .await()

            val alreadyExists = snapshot.children.any {
                it.key != playlistId
            }

            if (alreadyExists) {
                return Result.failure(
                    Exception("Playlist Already Exists")
                )
            }

            val updates = mapOf<String, Any>(
                "playlistName" to title.trim(),
                "description" to description.trim()
            )

            playlistRef.child(playlistId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}