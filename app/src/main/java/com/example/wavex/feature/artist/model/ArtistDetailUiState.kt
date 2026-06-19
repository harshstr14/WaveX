package com.example.wavex.feature.artist.model

import com.example.wavex.core.model.DataItem
import com.example.wavex.core.model.SongItem

data class ArtistDetailUiState(
    val id: String = "",
    val name: String = "",
    val followerCount: Int = 0,
    val fanCount: String = "",
    val isVerified: Boolean = false,
    val imageUrl: String = "",
    val topSongs: List<SongItem> = emptyList(),
    val topAlbums: List<DataItem> = emptyList(),
    val singles: List<DataItem> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String = ""
)