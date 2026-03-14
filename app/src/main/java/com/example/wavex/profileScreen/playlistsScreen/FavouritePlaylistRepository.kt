package com.example.wavex.profileScreen.playlistsScreen

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FavouritePlaylistRepository {
    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
        .child(userID!!).child("Favourites").child("Playlists")

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

            favouriteReference.addValueEventListener(listener)

            awaitClose {
                favouriteReference.removeEventListener(listener)
            }
        }

    suspend fun deleteAllPlaylists() {
        favouriteReference.removeValue().await()
    }
}