package com.example.wavex.feature.playlist.data

import com.example.wavex.HttpClientProvider
import com.example.wavex.core.model.Album
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.Download
import com.example.wavex.core.model.Image
import com.example.wavex.core.model.Quality
import com.example.wavex.core.model.SongItem
import com.example.wavex.feature.playlist.model.PlaylistDetailUiState
import com.example.wavex.feature.search.presentation.SearchSource
import com.example.wavex.requestWithFallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

class PlaylistRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    companion object {
        private const val TAG = "PlaylistRepository"
        private val imageSizeRegex = Regex("w\\d+-h\\d+")
    }

    private fun getFavouritePlaylistRef(playlistId: String): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        return firebaseDatabase
            .getReference("Users")
            .child(userId)
            .child("Favourites")
            .child("Playlists")
            .child(playlistId)
    }

    suspend fun isFavouritePlaylist(playlistId: String): Boolean {
        val snapshot = getFavouritePlaylistRef(playlistId)
            .get()
            .await()

        return snapshot.exists()
    }

    suspend fun addToFavourite(
        playlistId: String,
        playlistName: String,
        imageUrl: String,
        playlistSource: String
    ) {
        val playlistData = mapOf(
            "playlistId" to playlistId,
            "playlistName" to playlistName,
            "playlistImageUrl" to imageUrl,
            "isFavourite" to true,
            "source" to playlistSource
        )

        getFavouritePlaylistRef(playlistId)
            .setValue(playlistData)
            .await()
    }

    suspend fun removeFromFavourite(playlistId: String) {
        getFavouritePlaylistRef(playlistId)
            .removeValue()
            .await()
    }

    suspend fun fetchPlaylistById(playlistId: String): PlaylistDetailUiState =
        withContext(Dispatchers.IO) {
            try {
                val response = requestWithFallback(
                    "/playlists?id=$playlistId&limit=40"
                )

                if (response.isEmpty()) {
                    return@withContext PlaylistDetailUiState(
                        isError = true,
                        errorMessage =
                            "Network error. Please try again."
                    )
                }

                parsePlaylist(response)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "fetchPlaylistById failed")

                PlaylistDetailUiState(
                    isError = true,
                    errorMessage = e.message ?: "Something went wrong."
                )
            }
        }

    private fun parsePlaylist(jsonString: String): PlaylistDetailUiState {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return PlaylistDetailUiState(
                isError = true,
                errorMessage = "Playlist not found"
            )
        }

        val data = json.optJSONObject("data")
            ?: return PlaylistDetailUiState(
                isError = true,
                errorMessage = "Invalid response"
            )

        val songsArray = data.optJSONArray("songs")

        val songs = buildList {
            if (songsArray != null) {
                for (i in 0 until songsArray.length()) {
                    val song = songsArray.optJSONObject(i) ?: continue

                    val duration = song.optInt("duration")

                    val albumObject = song.optJSONObject("album")

                    add(
                        SongItem(
                            id = song.optString("id"),
                            name = song.optString("name"),
                            artist = parsePrimaryArtists(
                                song.optJSONObject("artists")
                            ).toMutableList(),
                            album = Album(
                                id = albumObject
                                    ?.optString("id")
                                    .orEmpty(),
                                name = albumObject
                                    ?.optString("name")
                                    .orEmpty()
                            ),
                            image = parseImages(
                                song.optJSONArray("image")
                            ).toMutableList(),
                            duration = duration,
                            playCount = song.optInt("playCount"),
                            downloadUrl = parseDownloads(
                                song.optJSONArray("downloadUrl")
                            ).toMutableList(),
                            songSource = SearchSource.JIOSAAVN.toString()
                        )
                    )
                }
            }
        }

        return PlaylistDetailUiState(
            id = data.optString("id"),
            name = data.optString("name"),
            description = data.optString("description"),
            songCount = data.optString("songCount"),
            images = parseImages(
                data.optJSONArray("image")
            ),
            artists = parseArtistsArray(
                data.optJSONArray("artists")
            ),
            songs = songs,
            totalDuration = songs.sumOf { it.duration }
        )
    }

    suspend fun fetchYTMusicPlaylist(playlistId: String, baseUrl: String)
    : PlaylistDetailUiState = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/playlists/$playlistId")
                .get()
                .build()

            val call = HttpClientProvider.client.newCall(request)

            coroutineContext.job
                .invokeOnCompletion {
                    call.cancel()
                }

            call.execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext PlaylistDetailUiState(
                        isError = true,
                        errorMessage = "Network error. Please try again."
                    )
                }

                val body = response.body?.string().orEmpty()

                if (body.isEmpty()) {
                    return@withContext PlaylistDetailUiState(
                        isError = true,
                        errorMessage =
                            "Empty response received."
                    )
                }

                parseYTMusicPlaylist(body)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "fetchYTMusicPlaylist failed")

            PlaylistDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }

    private fun parseYTMusicPlaylist(jsonString: String): PlaylistDetailUiState {
        return try {
            val json = JSONObject(jsonString)

            val tracksArray = json.optJSONArray("tracks")

            val songs = buildList {
                if (tracksArray != null) {
                    for (i in 0 until tracksArray.length()) {
                        val item = tracksArray.optJSONObject(i) ?: continue

                        val durationSeconds = parseDuration(item.optString("duration"))

                        add(
                            SongItem(
                                id = item.optString("videoId"),
                                name = item.optString("title"),
                                duration = durationSeconds,
                                image = parseYTImages(
                                    item.optJSONArray(
                                        "thumbnails"
                                    )
                                ).toMutableList(),
                                artist = parseYTArtists(
                                    item.optJSONArray("artists")
                                ).toMutableList(),
                                songSource = SearchSource.YTMUSIC.toString()
                            )
                        )
                    }
                }
            }

            val artists = buildList {
                val author = json.optString("author")

                if (author.isNotBlank()) {
                    add(
                        Artists(
                            id = "",
                            name = author,
                            role = "Creator",
                            image = "",
                            searchSource = SearchSource.YTMUSIC.name
                        )
                    )
                }
            }

            PlaylistDetailUiState(
                id = json.optString("playlistId"),
                name = json.optString("title"),
                description = json.optString("description"),
                songCount = json.optInt("trackCount").toString(),
                images = listOf(
                    Image(
                        quality = "high",
                        url = optimizeImage(
                            json.optString("thumbnail")
                        )
                    )
                ),
                artists = artists,
                songs = songs,
                totalDuration = songs.sumOf { it.duration }
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "parseYTMusicPlaylist failed")

            PlaylistDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }

    private fun parseArtistsArray(array: JSONArray?): List<Artists> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val artist = array.optJSONObject(i) ?: continue

                add(
                    Artists(
                        id = artist.optString("id"),
                        name = artist.optString("name"),
                        role = artist.optString("role"),
                        image = artist
                            .optJSONArray("image")
                            ?.optJSONObject(2)
                            ?.optString("url")
                            .orEmpty(),
                        type = artist.optString("type"),
                        searchSource = SearchSource.JIOSAAVN.name
                    )
                )
            }
        }
    }

    private fun parsePrimaryArtists(obj: JSONObject?): List<Artists> {
        val primary = obj?.optJSONArray("primary") ?: return emptyList()

        return buildList {
            for (i in 0 until primary.length()) {
                val artist = primary.optJSONObject(i) ?: continue

                add(
                    Artists(
                        id = artist.optString("id"),
                        name = artist.optString("name"),
                        role = artist.optString("role"),
                        image = artist
                            .optJSONArray("image")
                            ?.optJSONObject(2)
                            ?.optString("url")
                            .orEmpty(),
                        type = artist.optString("type")
                    )
                )
            }
        }
    }

    private fun parseImages(array: JSONArray?): List<Image> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue

                add(
                    Image(
                        quality = obj.optString("quality"),
                        url = obj.optString("url")
                    )
                )
            }
        }
    }

    private fun parseDownloads(array: JSONArray?): List<Download> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue

                val qualityString = obj.optString("quality")

                val quality = when (qualityString.lowercase()) {
                    "12kbps", "48kbps" -> Quality.LOW
                    "96kbps", "160kbps" -> Quality.MEDIUM
                    "320kbps" -> Quality.HIGH
                    else -> Quality.MEDIUM
                }

                add(
                    Download(
                        quality = quality,
                        url = obj.optString("url")
                    )
                )
            }
        }
    }

    private fun parseYTArtists(array: JSONArray?): List<Artists> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val artist = array.optJSONObject(i) ?: continue

                add(
                    Artists(
                        id = artist.optString("id"),
                        name = artist.optString("name"),
                        searchSource = SearchSource.YTMUSIC.name
                    )
                )
            }
        }
    }

    private fun parseYTImages(array: JSONArray?): List<Image> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val thumb = array.optJSONObject(i) ?: continue

                add(
                    Image(
                        quality = "high",
                        url = optimizeImage(thumb.optString("url"))
                    )
                )
            }
        }
    }

    private fun parseDuration(duration: String): Int {
        val parts = duration.split(":")

        val minutes =
            parts.getOrNull(0)
                ?.toIntOrNull() ?: 0

        val seconds =
            parts.getOrNull(1)
                ?.toIntOrNull() ?: 0

        return (minutes * 60) + seconds
    }

    private fun optimizeImage(url: String): String {
        return if (url.contains("i.ytimg.com/vi/")) {
            val videoId = url.substringAfter("/vi/").substringBefore("/")
            "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg"
        } else {
            url.replace(
                imageSizeRegex,
                "w520-h520"
            )
        }
    }
}