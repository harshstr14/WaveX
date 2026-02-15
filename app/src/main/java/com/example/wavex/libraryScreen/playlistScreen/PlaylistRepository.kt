package com.example.wavex.libraryScreen.playlistScreen

import com.example.wavex.libraryScreen.PlaylistData
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

    fun observeSinglePlaylistByName(name: String): Flow<PlaylistData?> =
        callbackFlow {
            val query = favouriteReference
                .orderByChild("playlistName")
                .equalTo(name)

            val listener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val playlist = snapshot.children
                        .firstOrNull()
                        ?.getValue(PlaylistData::class.java)

                    trySend(playlist)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            query.addValueEventListener(listener)

            awaitClose {
                query.removeEventListener(listener)
            }
        }
}