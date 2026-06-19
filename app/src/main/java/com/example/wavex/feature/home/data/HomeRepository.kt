package com.example.wavex.feature.home.data

import com.example.wavex.core.database.entity.RecentlyPlayedEntity
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.DataItem
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.network.requestWithFallback
import com.example.wavex.core.parser.AlbumParser
import com.example.wavex.core.parser.PlaylistParser
import com.example.wavex.core.parser.SongParser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val topArtistsRepository: TopArtistsRepository,
    private val recentlyPlayedRepository: RecentlyPlayedRepository
) {
    suspend fun getPlaylists(): List<DataItem> {
        val responseBody =
            requestWithFallback("/search/playlists?query=Top&limit=20")

        return PlaylistParser.parse(
            responseBody,
            "results"
        )
    }

    suspend fun getAlbums(): List<DataItem> {
        val responseBody =
            requestWithFallback(
                "/search/albums?query=latest&limit=30"
            )

        return AlbumParser.parse(
            responseBody,
            "results"
        )
    }

    suspend fun getTrendingSongs(): List<SongItem> {
        val responseBody =
            requestWithFallback("/playlists?id=946682072&limit=40")

        return SongParser.parse(
            responseBody,
            "songs"
        )
    }

    suspend fun getNewReleases(): List<SongItem> {
        val responseBody =
            requestWithFallback("/playlists?id=6689255&limit=40")

        return SongParser.parse(
            responseBody,
            "songs"
        )
    }

    suspend fun refreshTopArtists() {
        topArtistsRepository.refreshArtists()
    }

    fun getTopArtists(): Flow<List<Artists>> =
        topArtistsRepository.artists

    fun getRecentlyPlayed(): Flow<List<RecentlyPlayedEntity>> =
        recentlyPlayedRepository.getRecentlyPlayed()

    suspend fun clearRecentlyPlayed() {
        recentlyPlayedRepository.clearRecentlyPlayed()
    }
}