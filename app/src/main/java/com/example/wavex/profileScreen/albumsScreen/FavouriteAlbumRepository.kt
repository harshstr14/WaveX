package com.example.wavex.profileScreen.albumsScreen

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FavouriteAlbumRepository {
    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
        .child(userID!!).child("Favourites").child("Albums")

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

            favouriteReference.addValueEventListener(listener)

            awaitClose {
                favouriteReference.removeEventListener(listener)
            }
        }
}