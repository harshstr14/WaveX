package com.example.wavex.feature.artist.allsongs.model

import com.example.wavex.core.model.SongItem

data class AllSongsUiState(
    val songs: List<SongItem> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String = ""
)