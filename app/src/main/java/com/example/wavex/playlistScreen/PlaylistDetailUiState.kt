package com.example.wavex.playlistScreen

import com.example.wavex.songData.Artists
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.SongItem

data class PlaylistDetailUiState(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val songCount: String = "",
    val images: List<Image> = emptyList(),
    val artists: List<Artists> = emptyList(),
    val songs: List<SongItem> = emptyList(),
    val totalDuration: Int = 0,
    val isError: Boolean = false,
    val errorMessage: String = ""
)
