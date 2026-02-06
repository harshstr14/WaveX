package com.example.wavex.artistScreen.allSongsScreen

import com.example.wavex.homeScreen.SongItem

data class AllSongsUiState(
    val songs: List<SongItem> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String = ""
)

