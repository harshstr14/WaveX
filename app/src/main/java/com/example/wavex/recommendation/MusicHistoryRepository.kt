package com.example.wavex.recommendation

import com.example.wavex.recommendation.dataClass.PlayedSong
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import jakarta.inject.Inject

class MusicHistoryRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    firebaseDatabase: FirebaseDatabase
) {
    private val database = firebaseDatabase.reference

    fun getUserHistory(
        onResult: (List<PlayedSong>) -> Unit
    ) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            onResult(emptyList())
            return
        }

        database
            .child("Users")
            .child(userId)
            .child("history")
            .get()
            .addOnSuccessListener { snapshot ->
                val history =
                    mutableListOf<PlayedSong>()

                for (child in snapshot.children) {
                    val song =
                        child.getValue(
                            PlayedSong::class.java
                        )

                    song?.let {
                        history.add(it)
                    }
                }

                history.sortByDescending {
                    it.lastPlayed
                }

                onResult(history.take(20))
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun savePlayedSong(song: PlayedSong) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        val songRef = database
            .child("Users")
            .child(userId)
            .child("history")
            .child(song.id)

        songRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val existingSong = snapshot.getValue(PlayedSong::class.java)

                val updatedSong = existingSong?.copy(
                    playCount = (existingSong.playCount) + 1,
                    lastPlayed = System.currentTimeMillis()
                )

                songRef.setValue(updatedSong)
            }
            else {
                val newSong = song.copy(
                    playCount = 1,
                    lastPlayed = System.currentTimeMillis()
                )

                songRef.setValue(newSong)
            }
        }
    }
}