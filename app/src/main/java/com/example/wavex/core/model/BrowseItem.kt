package com.example.wavex.core.model

import androidx.compose.ui.graphics.Color

data class BrowseItem(
    val title: String,
    val subtitle: String,
    val playlistId: String,
    val gradient: List<Color>
)