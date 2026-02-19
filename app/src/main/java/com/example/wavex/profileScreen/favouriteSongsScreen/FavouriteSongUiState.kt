package com.example.wavex.profileScreen.favouriteSongsScreen

import com.example.wavex.homeScreen.SongItem


data class FavouriteSongUiState(
    val songs: List<SongItem> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)
