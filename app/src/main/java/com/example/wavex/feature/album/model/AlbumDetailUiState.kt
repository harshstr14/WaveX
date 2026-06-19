package com.example.wavex.feature.album.model

import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.Image
import com.example.wavex.core.model.SongItem

data class AlbumDetailUiState(
    val albumId: String = "",
    val albumName: String = "",
    val description: String = "",
    val songCount: String = "",
    val type: String = "",
    val year: Int = 0,
    val albumImages: List<Image> = emptyList(),
    val primaryArtists: List<Artists> = emptyList(),
    val songs: List<SongItem> = emptyList(),
    val totalDuration: Int = 0,
    val isError: Boolean = false,
    val errorMessage: String = ""
)