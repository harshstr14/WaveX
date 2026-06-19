package com.example.wavex.feature.player.data

import jakarta.inject.Inject
import kotlin.random.Random

class WaveformRepository @Inject constructor() {
    fun generateWaveform(songId: String): List<Float> {
        val seed = songId.hashCode()
        val random = Random(seed)

        return List(120) {
            random.nextFloat().coerceIn(0.1f, 1f)
        }
    }
}