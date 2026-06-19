package com.example.wavex.feature.library.data

import com.example.wavex.core.model.SongItem
import com.example.wavex.core.model.PlaylistData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LibraryRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private fun getPlaylistReference(): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        return firebaseDatabase
            .getReference("Users")
            .child(userId)
            .child("Favourites")
            .child("MyPlaylists")
    }

    fun observePlaylists(): Flow<List<PlaylistData>> =
        callbackFlow {
            val reference = getPlaylistReference()

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val playlists = mutableListOf<PlaylistData>()

                    snapshot.children.forEach { child ->
                        child.getValue(PlaylistData::class.java)?.let {
                            playlists.add(it)
                        }
                    }

                    trySend(playlists)
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

    fun deletePlaylist(
        playlistId: String,
        onResult: (Boolean) -> Unit
    ) {
        getPlaylistReference()
            .child(playlistId)
            .removeValue()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun addSongToPlaylist(
        playlistId: String,
        song: SongItem,
        onResult: (Boolean, String) -> Unit
    ) {
        val playlistRef = getPlaylistReference().child(playlistId)

        playlistRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val playlist =
                    currentData.getValue(PlaylistData::class.java)
                        ?: return Transaction.success(currentData)

                val currentSongs = playlist.songs

                if (currentSongs.any { it.id == song.id }) {
                    return Transaction.abort()
                }

                currentSongs.add(song)

                playlist.songs = currentSongs
                playlist.totalSongs = currentSongs.size
                playlist.totalDuration += song.duration

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
                        onResult(false, "Something went wrong. Please try again")
                    }

                    committed -> {
                        onResult(true, "Added to playlist")
                    }

                    else -> {
                        onResult(false, "Already in playlist")
                    }
                }
            }
        })
    }
}