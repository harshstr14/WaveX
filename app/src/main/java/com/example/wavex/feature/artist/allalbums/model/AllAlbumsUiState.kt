package com.example.wavex.feature.artist.allalbums.model

import com.example.wavex.core.model.DataItem

data class AllAlbumsUiState(
    val albums: List<DataItem> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String = ""
)