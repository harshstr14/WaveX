package com.example.wavex.feature.library.presentation.playlist.model

import com.example.wavex.core.model.PlaylistData

data class PlaylistDetailUiState(
    val playlist: PlaylistData? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)