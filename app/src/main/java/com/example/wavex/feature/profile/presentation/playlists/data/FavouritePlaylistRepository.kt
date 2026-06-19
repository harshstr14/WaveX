package com.example.wavex.feature.profile.presentation.playlists.data

import com.example.wavex.feature.profile.presentation.playlists.model.FavouritePlaylist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FavouritePlaylistRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private fun getFavouritePlaylistRef(): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        return firebaseDatabase
            .getReference("Users")
            .child(userId)
            .child("Favourites")
            .child("Playlists")
    }

    fun observeFavouritePlaylists(): Flow<List<FavouritePlaylist>> =
        callbackFlow {

            val listener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<FavouritePlaylist>()

                    for (child in snapshot.children) {
                        val playlist = child.getValue(FavouritePlaylist::class.java)

                        playlist?.let { list.add(it) }
                    }

                    trySend(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            getFavouritePlaylistRef().addValueEventListener(listener)

            awaitClose {
                getFavouritePlaylistRef().removeEventListener(listener)
            }
        }

    suspend fun deleteAllPlaylists() {
        getFavouritePlaylistRef().removeValue().await()
    }
}