package com.example.wavex.feature.profile.presentation.songs.data

import com.example.wavex.core.model.SongItem
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

class FavouriteSongsRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private fun getFavouriteSongRef(): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        return firebaseDatabase
            .getReference("Users")
            .child(userId)
            .child("Favourites")
            .child("Songs")
    }

    fun observeFavouriteSongs(): Flow<List<SongItem>> =
        callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<SongItem>()

                    for (child in snapshot.children) {
                        val album = child.getValue(SongItem::class.java)

                        album?.let { list.add(it) }
                    }

                    trySend(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            getFavouriteSongRef().addValueEventListener(listener)

            awaitClose {
                getFavouriteSongRef().removeEventListener(listener)
            }
        }

    suspend fun deleteAllSongs() {
        getFavouriteSongRef().removeValue().await()
    }
}