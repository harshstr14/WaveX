package com.example.wavex.homeScreen.repository

import android.util.Log
import com.example.wavex.BuildConfig
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.localDB.dao.RecentlyPlayedDao
import com.example.wavex.homeScreen.localDB.entity.RecentlyPlayedEntity
import com.example.wavex.homeScreen.toSongItem
import com.example.wavex.searchScreen.repository.SearchSongsRepository
import javax.inject.Inject

class RecentlyPlayedRepository @Inject constructor(
    private val dao: RecentlyPlayedDao,
    private val searchSongsRepository: SearchSongsRepository
) {
    companion object {
        private const val MAX_SIZE = 24
    }

    fun getRecentlyPlayed() = dao.getRecentlyPlayed()

    suspend fun addSong(song: RecentlyPlayedEntity) {
        val existing = dao.getSong(song.id)
        val now = System.currentTimeMillis()

        if (existing != null) {
            dao.updateStreamData(
                songId = song.id,
                downloadUrl = song.downloadUrl,
                duration = song.duration,
                time = now
            )
        } else {
            dao.insert(song.copy(playedAt = now))
        }

        val currentList = dao.getRecentlyPlayedOnce()
        val extra = currentList.size - MAX_SIZE

        if (extra > 0) {
            dao.deleteOldest(extra)
        }
    }

    suspend fun clearRecentlyPlayed() {
        dao.deleteAll()
    }

    suspend fun getPlayableSong(
        clickedSong: SongItem
    ): SongItem {
        Log.d(
            "PLAY_FLOW",
            """
            Clicked Song
            Id     : ${clickedSong.id}
            Title  : ${clickedSong.name}
            Source : ${clickedSong.songSource}
            """.trimIndent()
        )

        Log.d(
            "PLAY_FLOW",
            "Checking cache for song: ${clickedSong.id}"
        )

        val cached = dao.getSong(clickedSong.id)

        if (cached == null) {
            Log.d(
                "PLAY_FLOW",
                "Song not cached -> Fetching fresh stream data"
            )

            val fresh = searchSongsRepository.fetchYTStreamData(
                clickedSong.id,
                BuildConfig.YT_STREAM_URL
            )

            return fresh?.let {
                Log.d(
                    "PLAY_FLOW",
                    "Fresh streams fetched: ${it.downloadUrl.size}"
                )

                Log.d("Duration","${it.duration}")

                clickedSong.copy(
                    duration = it.duration,
                    downloadUrl = it.downloadUrl
                )
            } ?: clickedSong
        }

        Log.d(
            "PLAY_FLOW",
            """
            Cached song found
            Streams : ${cached.downloadUrl.size}
            """.trimIndent()
        )

        val now = System.currentTimeMillis() / 1000

        cached.downloadUrl.forEachIndexed { index, stream ->
            Log.d(
                "PLAY_FLOW",
                """
            Stream[$index]
            Quality  : ${stream.quality}
            ExpireAt : ${stream.expiresAt}
            Current  : $now
            Valid    : ${stream.expiresAt == null || stream.expiresAt > now}
            """.trimIndent()
            )
        }

        val allStreamsValid =
            cached.downloadUrl.isNotEmpty() && cached.downloadUrl.all {
                        it.expiresAt == null || it.expiresAt > now
            }

        Log.d(
            "PLAY_FLOW",
            "All Streams Valid = $allStreamsValid"
        )

        if (allStreamsValid) {
            return cached.toSongItem()
        }

        Log.d(
            "PLAY_FLOW",
            "URLs expired -> Fetching fresh URLs"
        )

        val fresh = searchSongsRepository.fetchYTStreamData(
            clickedSong.id,
            BuildConfig.YT_STREAM_URL
        )

        return fresh?.let {
            Log.d(
                "PLAY_FLOW",
                "Fresh streams fetched: ${it.downloadUrl.size}"
            )

            cached.toSongItem().copy(
                duration = it.duration,
                downloadUrl = it.downloadUrl
            )
        } ?: cached.toSongItem()
    }
}