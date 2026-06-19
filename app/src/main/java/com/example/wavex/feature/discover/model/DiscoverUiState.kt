package com.example.wavex.feature.discover.model

import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.BrowseItem
import com.example.wavex.core.model.DataItem
import com.example.wavex.core.model.SongItem

data class DiscoverUiState(
    val albums: List<DataItem> = emptyList(),
    val artists: List<Artists> = emptyList(),
    val playlists: List<DataItem> = emptyList(),
    val songs: List<SongItem> = emptyList(),
    val exploreLists: List<BrowseItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)