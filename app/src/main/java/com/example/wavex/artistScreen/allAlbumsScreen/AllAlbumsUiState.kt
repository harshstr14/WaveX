package com.example.wavex.artistScreen.allAlbumsScreen

import com.example.wavex.homeScreen.DataItem

data class AllAlbumsUiState(
    val albums: List<DataItem> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String = ""
)

