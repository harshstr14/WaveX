package com.example.wavex.feature.library.presentation.favourite.model

import com.example.wavex.core.model.SongItem

data class FavouriteSongUiState(
    val songs: List<SongItem> = emptyList(),
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null
)