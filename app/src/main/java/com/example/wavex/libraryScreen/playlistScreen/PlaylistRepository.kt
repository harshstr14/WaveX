package com.example.wavex.libraryScreen.playlistScreen

import com.example.wavex.libraryScreen.PlaylistData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PlaylistRepository {
    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
        .child(userID!!).child("Favourites").child("MyPlaylists")

    fun observePlaylistById(playlistId: String): Flow<PlaylistData?> =
        callbackFlow {
            val reference = favouriteReference.child(playlistId)

            val listener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val playlist = snapshot.getValue(PlaylistData::class.java)
                    trySend(playlist)
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

    fun removeSongFromPlaylist(
        playlistId: String,
        songId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val playlistRef = favouriteReference.child(playlistId)

        playlistRef.runTransaction(object : Transaction.Handler {

            override fun doTransaction(currentData: MutableData): Transaction.Result {

                val playlist =
                    currentData.getValue(PlaylistData::class.java)
                        ?: return Transaction.success(currentData)

                val currentSongs = playlist.songs.toMutableList()

                val updatedSongs = currentSongs.filter { it.id != songId }

                if (updatedSongs.size == currentSongs.size) {
                    return Transaction.abort()
                }

                playlist.songs = updatedSongs.toMutableList()
                playlist.totalSongs = updatedSongs.size
                playlist.totalDuration = updatedSongs.sumOf { it.duration }

                currentData.value = playlist

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                when {
                    error != null -> {
                        onResult(false, "Something went wrong")
                    }

                    committed -> {
                        onResult(true, "Removed from playlist")
                    }

                    else -> {
                        onResult(false, "Song not found")
                    }
                }
            }
        })
    }
}