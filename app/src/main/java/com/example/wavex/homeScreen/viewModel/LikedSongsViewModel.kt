package com.example.wavex.homeScreen.viewModel

import androidx.lifecycle.ViewModel
import com.example.wavex.homeScreen.SongItem
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

    fun toggleLike(song: SongItem) {
        val ref = favouriteRef?.child(song.id) ?: return

        if (_likedSongs.value.contains(song.id)) {
            ref.removeValue()
        } else {
            ref.setValue(song)
        }
    }

    fun isLiked(songId: String): Boolean {
        return _likedSongs.value.contains(songId)
    }
}