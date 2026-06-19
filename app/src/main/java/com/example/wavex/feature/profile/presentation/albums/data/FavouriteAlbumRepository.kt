package com.example.wavex.feature.profile.presentation.albums.data

import com.example.wavex.feature.profile.presentation.albums.model.FavouriteAlbum
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

class FavouriteAlbumRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private fun getFavouriteAlbumRef(): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        return firebaseDatabase
            .getReference("Users")
            .child(userId)
            .child("Favourites")
            .child("Albums")
    }

    fun observeFavouriteAlbums(): Flow<List<FavouriteAlbum>> =
        callbackFlow {
            val listener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<FavouriteAlbum>()

                    for (child in snapshot.children) {
                        val album = child.getValue(FavouriteAlbum::class.java)

                        album?.let { list.add(it) }
                    }

                    trySend(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            getFavouriteAlbumRef().addValueEventListener(listener)

            awaitClose {
                getFavouriteAlbumRef().removeEventListener(listener)
            }
        }

    suspend fun deleteAllAlbums() {
        getFavouriteAlbumRef().removeValue().await()
    }
}