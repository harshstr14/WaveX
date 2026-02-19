package com.example.wavex.libraryScreen

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PlaylistRepository {
    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
        .child(userID!!).child("Favourites").child("MyPlaylists")

    fun observePlaylists(): Flow<List<PlaylistData>> =
        callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<PlaylistData>()

                    for (child in snapshot.children) {
                        val playlist = child.getValue(PlaylistData::class.java)

                        playlist?.let { list.add(it) }
                    }

                    trySend(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }

            }

            favouriteReference.addValueEventListener(listener)

            awaitClose {
                favouriteReference.removeEventListener(listener)
            }
        }

    fun deletePlaylist(
        playlistId: String,
        onResult: (Boolean) -> Unit
    ) {
        favouriteReference
            .child(playlistId)
            .removeValue()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}