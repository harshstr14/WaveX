package com.example.wavex.feature.library.presentation.favourite.data

import com.example.wavex.core.model.SongItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavouriteSongRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private fun getFavouriteReference(): DatabaseReference {
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
            val reference = getFavouriteReference()

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val songs = mutableListOf<SongItem>()

                    snapshot.children.forEach { child ->
                        child.getValue(SongItem::class.java)?.let {
                            songs.add(it)
                        }
                    }

                    trySend(songs)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            reference.addValueEventListener(listener)

            awaitClose {
                reference.removeEventListener(listener)
            }
        }

    suspend fun removeSong(songId: String) {
        getFavouriteReference()
            .child(songId)
            .removeValue()
            .await()
    }
}