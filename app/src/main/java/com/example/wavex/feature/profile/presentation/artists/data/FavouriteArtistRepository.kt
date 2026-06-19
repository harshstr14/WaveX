package com.example.wavex.feature.profile.presentation.artists.data

import com.example.wavex.feature.profile.presentation.artists.model.FavouriteArtist
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

class FavouriteArtistRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private fun getFavouriteArtistsRef(): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        return firebaseDatabase
            .getReference("Users")
            .child(userId)
            .child("Favourites")
            .child("Artists")
    }

    fun observeFavouriteArtists(): Flow<List<FavouriteArtist>> =
        callbackFlow {
            val listener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<FavouriteArtist>()

                    for (child in snapshot.children) {
                        val artist = child.getValue(FavouriteArtist::class.java)

                        artist?.let { list.add(it) }
                    }

                    trySend(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            getFavouriteArtistsRef().addValueEventListener(listener)

            awaitClose {
                getFavouriteArtistsRef().removeEventListener(listener)
            }
        }

    suspend fun deleteAllArtists() {
        getFavouriteArtistsRef().removeValue().await()
    }
}