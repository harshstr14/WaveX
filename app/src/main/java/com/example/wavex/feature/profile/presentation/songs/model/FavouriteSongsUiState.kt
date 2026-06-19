package com.example.wavex.feature.profile.presentation.songs.model

import com.example.wavex.core.model.SongItem

data class FavouriteSongsUiState(
    val songs: List<SongItem> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)