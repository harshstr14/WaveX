package com.example.wavex.feature.player.model

data class PlayerUiState(
    val amplitudes: List<Float> = emptyList(),
    val currentSongId: String? = null
)