package com.example.wavex.feature.album.data

import android.util.Log
import com.example.wavex.HttpClientProvider
import com.example.wavex.feature.album.model.AlbumDetailUiState
import com.example.wavex.core.model.Album
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.Download
import com.example.wavex.core.model.Image
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.model.Quality
import com.example.wavex.requestWithFallback
import com.example.wavex.feature.search.presentation.SearchSource
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
class AlbumRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    companion object {
        private const val TAG = "AlbumRepository"
        private val imageSizeRegex = Regex("w\\d+-h\\d+")
    }

    private fun getFavouriteAlbumRef(albumId: String): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        return firebaseDatabase
            .getReference("Users")
            .child(userId)
            .child("Favourites")
            .child("Albums")
            .child(albumId)
    }

    suspend fun isFavouriteAlbum(albumId: String): Boolean {
        return getFavouriteAlbumRef(albumId)
            .get()
            .await()
            .exists()
    }

    suspend fun addToFavourite(
        albumId: String,
        albumName: String,
        imageUrl: String,
        primaryArtists: String,
        source: String
    ) {
        val albumData = mapOf(
            "albumId" to albumId,
            "albumName" to albumName,
            "albumImageUrl" to imageUrl,
            "primaryArtists" to primaryArtists,
            "isFavourite" to true,
            "source" to source
        )

        getFavouriteAlbumRef(albumId)
            .setValue(albumData)
            .await()
    }

    suspend fun removeFromFavourite(
        albumId: String
    ) {
        getFavouriteAlbumRef(albumId)
            .removeValue()
            .await()
    }

    suspend fun fetchAlbumById(albumId: String): AlbumDetailUiState = withContext(Dispatchers.IO) {
        try {
            val response = requestWithFallback(
                "/albums?id=$albumId&limit=30"
            )

            if (response.isEmpty()) {
                return@withContext AlbumDetailUiState(
                    isError = true,
                    errorMessage = "Network error. Please try again."
                )
            }

            parseAlbum(response)
        } catch (e: Exception) {
            Log.e(TAG, "fetchAlbumById failed", e)

            AlbumDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }

    private fun parseAlbum(jsonString: String): AlbumDetailUiState {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return AlbumDetailUiState(
                isError = true,
                errorMessage = "Album not found"
            )
        }

        val data = json.optJSONObject("data")
            ?: return AlbumDetailUiState(
                isError = true,
                errorMessage = "Invalid response"
            )

        val primaryArtists = parsePrimaryArtists(
            data.optJSONObject("artists")
        )

        val albumImages = parseImages(
            data.optJSONArray("image")
        )

        val songsArray = data.optJSONArray("songs")

        val songs = buildList {
            if (songsArray != null) {
                for (i in 0 until songsArray.length()) {
                    val song = songsArray.optJSONObject(i) ?: continue

                    val albumObject = song.optJSONObject("album")

                    add(
                        SongItem(
                            id = song.optString("id"),
                            name = song.optString("name"),
                            artist = parsePrimaryArtists(
                                song.optJSONObject("artists")
                            ).toMutableList(),
                            album = Album(
                                id = albumObject?.optString("id").orEmpty(),
                                name = albumObject?.optString("name").orEmpty()
                            ),
                            image = parseImages(
                                song.optJSONArray("image")
                            ).toMutableList(),
                            duration = song.optInt("duration"),
                            playCount = song.optInt("playCount"),
                            downloadUrl = parseDownloads(
                                song.optJSONArray("downloadUrl")
                            ).toMutableList(),
                            songSource = SearchSource.JIOSAAVN.name
                        )
                    )
                }
            }
        }

        val totalDuration = songs.sumOf { it.duration }

        return AlbumDetailUiState(
            albumId = data.optString("id"),
            albumName = data.optString("name"),
            description = data.optString("description"),
            songCount = data.optString("songCount"),
            type = data.optString("type"),
            year = data.optInt("year"),
            albumImages = albumImages.toMutableList(),
            primaryArtists = primaryArtists.toMutableList(),
            songs = songs,
            totalDuration = totalDuration
        )
    }

    suspend fun fetchYTMusicAlbum(
        albumId: String,baseUrl: String
    ): AlbumDetailUiState = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/albums/$albumId")
                .get()
                .build()

            val call = HttpClientProvider.client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            call.execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext AlbumDetailUiState(
                        isError = true,
                        errorMessage = "Network error. Please try again."
                    )
                }

                val body = response.body?.string().orEmpty()

                if (body.isEmpty()) {
                    return@withContext AlbumDetailUiState(
                        isError = true,
                        errorMessage = "Empty response received."
                    )
                }

                parseYTMusicAlbum(body)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchYTMusicAlbum failed", e)

            AlbumDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }

    private fun parseYTMusicAlbum(jsonString: String): AlbumDetailUiState {
        return try {
            val json = JSONObject(jsonString)

            if (!json.optBoolean("success")) {
                return AlbumDetailUiState(
                    isError = true,
                    errorMessage = "Failed to load album."
                )
            }

            val albumObject = json.optJSONObject("album")

            val thumbnail = albumObject
                ?.optString("thumbnail")
                ?.replace(
                    imageSizeRegex,
                    "w520-h520"
                )
                .orEmpty()

            val albumImages = mutableListOf(
                Image(
                    quality = "high",
                    url = thumbnail
                )
            )

            val primaryArtists = buildList {
                when (val artistData = json.opt("artist")) {
                    is JSONArray -> {
                        for (i in 0 until artistData.length()) {
                            val artist = artistData.optJSONObject(i) ?: continue

                            add(
                                Artists(
                                    id = artist.optString("browseId"),
                                    name = artist.optString("name"),
                                    role = "Artist",
                                    image = "",
                                    searchSource = SearchSource.YTMUSIC.name
                                )
                            )
                        }
                    }
                    is JSONObject -> {
                        add(
                            Artists(
                                id = artistData.optString("browseId"),
                                name = artistData.optString("name"),
                                role = "Artist",
                                image = "",
                                searchSource = SearchSource.YTMUSIC.name
                            )
                        )
                    }
                }
            }

            val tracksArray = json.optJSONArray("tracks")

            val songs = buildList {
                if (tracksArray != null) {
                    for (i in 0 until tracksArray.length()) {
                        val item = tracksArray
                            .optJSONObject(i)
                            ?: continue

                        val durationSeconds = parseDuration(item.optString("duration"))

                        add(
                            SongItem(
                                id = item.optString("videoId"),
                                name = item.optString("title"),
                                duration = durationSeconds,
                                image = albumImages,
                                artist = primaryArtists.toMutableList(),
                                songSource = SearchSource.YTMUSIC.name
                            )
                        )
                    }
                }
            }

            AlbumDetailUiState(
                albumId = albumObject?.optString("browseId").orEmpty(),
                albumName = albumObject?.optString("title").orEmpty(),
                description = "",
                songCount = albumObject
                    ?.optInt("trackCount", 0)
                    ?.toString()
                    .orEmpty(),
                type = "Album",
                year = albumObject
                    ?.optString("year")
                    ?.toIntOrNull() ?: 0,
                albumImages = albumImages,
                primaryArtists = primaryArtists,
                songs = songs,
                totalDuration = songs.sumOf {
                    it.duration
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "parseYTMusicAlbum failed", e)

            AlbumDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }

    private fun parsePrimaryArtists(artistsObj: JSONObject?): List<Artists> {
        val primary = artistsObj
            ?.optJSONArray("primary")
            ?: return emptyList()

        return buildList {
            for (i in 0 until primary.length()) {
                val artist = primary.optJSONObject(i) ?: continue

                add(
                    Artists(
                        id = artist.optString("id"),
                        name = artist.optString("name"),
                        role = artist.optString("role"),
                        image = artist.optJSONArray("image")
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

    private fun parseDuration(duration: String): Int {
        val parts = duration.split(":")

        val minutes = parts.getOrNull(0)?.toIntOrNull() ?: 0

        val seconds = parts.getOrNull(1)?.toIntOrNull() ?: 0

        return (minutes * 60) + seconds
    }
}
