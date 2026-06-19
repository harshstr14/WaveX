package com.example.wavex.feature.library.model

import com.example.wavex.core.model.PlaylistData

data class LibraryUiState(
    val playlists: List<PlaylistData> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)
