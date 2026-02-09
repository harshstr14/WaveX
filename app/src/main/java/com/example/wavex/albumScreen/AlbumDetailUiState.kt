package com.example.wavex.albumScreen

import com.example.wavex.songData.Artists
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.SongItem

data class AlbumDetailUiState(
    val albumId: String = "",
    val albumName: String = "",
    val description: String = "",
    val songCount: String = "",
    val albumImages: List<Image> = emptyList(),
    val primaryArtists: List<Artists> = emptyList(),
    val songs: List<SongItem> = emptyList(),
    val totalDuration: Int = 0,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

