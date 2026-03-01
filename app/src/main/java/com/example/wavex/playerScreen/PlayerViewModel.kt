package com.example.wavex.playerScreen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class PlayerViewModel : ViewModel() {

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    val amplitudes = _amplitudes.asStateFlow()

    private var lastLoadedSongId: String? = null

    fun loadWaveform(songId: String?) {
        if (songId == null) return

        if (lastLoadedSongId == songId) return

        lastLoadedSongId = songId
        _amplitudes.value = generateWaveformForSong(songId)
    }

    fun generateWaveformForSong(songId: String): List<Float> {
        val seed = songId.hashCode()
        val random = Random(seed)

        return List(120) {
            random.nextFloat().coerceIn(0.1f, 1f)
        }
    }
}