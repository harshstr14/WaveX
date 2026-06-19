package com.example.wavex.core.shared

import androidx.lifecycle.ViewModel
import com.example.wavex.core.model.SongItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : ViewModel() {
    private val _likedSongs = MutableStateFlow<Set<String>>(emptySet())
    val likedSongs: StateFlow<Set<String>> = _likedSongs

    private val favouriteRef: DatabaseReference
        get() {
            val userId = firebaseAuth.currentUser?.uid
                ?: throw Exception("User not logged in")

            return firebaseDatabase
                .getReference("Users")
                .child(userId)
                .child("Favourites")
                .child("Songs")
        }

    init {
        observeFavourites()
    }

    private fun observeFavourites() {
        favouriteRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ids = snapshot.children.mapNotNull { it.key }.toSet()
                _likedSongs.value = ids
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun toggleLike(song: SongItem) {
        val ref = favouriteRef.child(song.id)

        if (_likedSongs.value.contains(song.id)) {
            ref.removeValue()
        } else {
            ref.setValue(song)
        }
    }

    fun isLiked(songId: String): Boolean {
        return _likedSongs.value.contains(songId)
    }

    fun removeFromFavourites(
        songId: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        val ref = favouriteRef.child(songId)

        ref.removeValue()
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}