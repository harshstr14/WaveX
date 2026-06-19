package com.example.wavex.feature.home.model

import com.example.wavex.core.database.entity.RecentlyPlayedEntity
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.DataItem
import com.example.wavex.core.model.SongItem

data class HomeUiState(
    val playlists: List<DataItem> = emptyList(),
    val newReleases: List<SongItem> = emptyList(),
    val trendingSongs: List<SongItem> = emptyList(),
    val topAlbums: List<DataItem> = emptyList(),
    val topArtists: List<Artists> = emptyList(),
    val recentlyPlayed: List<RecentlyPlayedEntity> = emptyList(),

    val isLoading: Boolean = false,
    val error: String? = null
)