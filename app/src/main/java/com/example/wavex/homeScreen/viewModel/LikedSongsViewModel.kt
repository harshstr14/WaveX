package com.example.wavex.homeScreen.viewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class LikedSongsViewModel : ViewModel() {

    private val _likedSongs = MutableStateFlow<Set<String>>(emptySet())
    val likedSongs: StateFlow<Set<String>> = _likedSongs

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val favouriteRef = userId?.let {
        FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(it)
            .child("Favourites")
            .child("Songs")
    }

    init {
        observeFavourites()
    }

    private fun observeFavourites() {
        favouriteRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ids = snapshot.children.mapNotNull { it.key }.toSet()
                _likedSongs.value = ids
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun toggleLike(
        songId: String,
        songName: String,
        imageUrl: String?
    ) {
        val ref = favouriteRef?.child(songId) ?: return

        if (_likedSongs.value.contains(songId)) {
            ref.removeValue()
        } else {
            val data = mapOf(
                "songId" to songId,
                "songName" to songName,
                "songImageUrl" to imageUrl,
                "isFavourite" to true
            )
            ref.setValue(data)
        }
    }

    fun isLiked(songId: String): Boolean {
        return _likedSongs.value.contains(songId)
    }
}