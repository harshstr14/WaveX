package com.example.wavex.artistScreen

import com.example.wavex.homeScreen.DataItem
import com.example.wavex.homeScreen.SongItem

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

