package com.example.wavex.profileScreen.favouriteSongsScreen

import com.example.wavex.homeScreen.SongItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FavouriteSongRepository {
    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
        .child(userID!!).child("Favourites").child("Songs")

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

            favouriteReference.addValueEventListener(listener)

            awaitClose {
                favouriteReference.removeEventListener(listener)
            }
        }

    suspend fun deleteAllSongs() {
        favouriteReference.removeValue().await()
    }
}