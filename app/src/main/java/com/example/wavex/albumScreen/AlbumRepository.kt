package com.example.wavex.albumScreen

import com.example.wavex.HttpClientProvider
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import com.example.wavex.songData.Album
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class AlbumRepository {

    suspend fun fetchAlbumById(albumId: String): AlbumDetailUiState =
        withContext(Dispatchers.IO) {
            val response = requestWithFallback("/albums?id=$albumId&limit=30")
            if (response.isEmpty()) {
                return@withContext AlbumDetailUiState(
                    isError = true,
                    errorMessage = "Network error. Please try again."
                )
            }

            parseAlbum(response)
        }

    private fun parseAlbum(jsonString: String): AlbumDetailUiState {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) {
            return AlbumDetailUiState(
                isError = true,
                errorMessage = "Album not found"
            )
        }

        val data = json.getJSONObject("data")

        val primaryArtists = parsePrimaryArtists(data.optJSONObject("artists"))
        val albumImages = parseImages(data.optJSONArray("image"))

        val songsArray = data.optJSONArray("songs")
        val songs = mutableListOf<SongItem>()
        var totalDuration = 0

        if (songsArray != null) {
            for (i in 0 until songsArray.length()) {
                val song = songsArray.getJSONObject(i)

                val images = parseImages(song.optJSONArray("image")).toMutableList()
                val downloads = parseDownloads(song.optJSONArray("downloadUrl")).toMutableList()
                val artists = parsePrimaryArtists(song.optJSONObject("artists")).toMutableList()
                val albumObject = song.optJSONObject("album")
                val album = Album(
                    id = albumObject?.optString("id") ?: "",
                    name = albumObject?.optString("name") ?: ""
                )

                totalDuration += song.optInt("duration")

                songs.add(
                    SongItem(
                        id = song.optString("id"),
                        name = song.optString("name"),
                        artist = artists,
                        album = album,
                        image = images,
                        duration = song.optInt("duration"),
                        playCount = song.optInt("playCount"),
                        downloadUrl = downloads,
                        searchSource = "jiosaavn"
                    )
                )
            }
        }

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

    private fun parsePrimaryArtists(artistsObj: JSONObject?): List<Artists> {
        val primary = artistsObj?.optJSONArray("primary") ?: return emptyList()
        return List(primary.length()) {
            val artist = primary.getJSONObject(it)
            val imageUrl = artist.optJSONArray("image")?.optJSONObject(2)?.optString("url") ?: ""
            Artists(
                id = artist.optString("id"),
                name = artist.optString("name"),
                role = artist.optString("role"),
                image = imageUrl,
                type = artist.optString("type")
            )
        }
    }

    private fun parseImages(array: org.json.JSONArray?): List<Image> {
        if (array == null) return emptyList()
        return List(array.length()) {
            val obj = array.getJSONObject(it)
            Image(
                quality = obj.optString("quality"),
                url = obj.optString("url")
            )
        }
    }

    private fun parseDownloads(array: org.json.JSONArray?): List<Download> {
        if (array == null) return emptyList()
        return List(array.length()) {
            val obj = array.getJSONObject(it)
            Download(
                quality = obj.optString("quality"),
                url = obj.optString("url")
            )
        }
    }

    suspend fun fetchYTMusicAlbum(
        albumId: String,
        baseUrl: String
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

            val response = call.execute()

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
        } catch (e: Exception) {
            e.printStackTrace()
            AlbumDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }

    fun parseYTMusicAlbum(jsonString: String): AlbumDetailUiState {
        try {
            val json = JSONObject(jsonString)

            val success = json.optBoolean("success", false)

            if (!success) {
                return AlbumDetailUiState(
                    isError = true,
                    errorMessage = "Failed to load album."
                )
            }

            val albumObject = json.optJSONObject("album")

            val albumId = albumObject?.optString("browseId").orEmpty()
            val albumName = albumObject?.optString("title").orEmpty()

            val year = albumObject
                ?.optString("year")
                ?.toIntOrNull() ?: 0

            val thumbnail = albumObject
                ?.optString("thumbnail")
                ?.replace(Regex("w\\d+-h\\d+"), "w520-h520")
                .orEmpty()

            val songCount = albumObject
                ?.optInt("trackCount", 0)
                ?.toString()
                .orEmpty()

            val albumImages = listOf(
                Image(
                    quality = "high",
                    url = thumbnail
                )
            )

            val artistObject = json.optJSONObject("artist")

            val primaryArtists = mutableListOf(
                Artists(
                    id = artistObject?.optString("browseId").orEmpty(),
                    name = artistObject?.optString("name").orEmpty(),
                    role = "Artist",
                    image = ""
                )
            )

            val songs = mutableListOf<SongItem>()
            val tracksArray = json.optJSONArray("tracks")

            var totalDuration = 0

            if (tracksArray != null) {
                for (i in 0 until tracksArray.length()) {
                    val item = tracksArray.getJSONObject(i)

                    val durationText = item.optString("duration")

                    val durationSeconds = durationText.split(":").let {

                        val minutes = it.getOrNull(0)?.toIntOrNull() ?: 0
                        val seconds = it.getOrNull(1)?.toIntOrNull() ?: 0

                        (minutes * 60) + seconds
                    }

                    totalDuration += durationSeconds

                    songs.add(
                        SongItem(
                            id = item.optString("videoId"),
                            name = item.optString("title"),
                            duration = durationSeconds,
                            image = albumImages.toMutableList(),
                            artist = primaryArtists,
                            searchSource = "ytmusic"
                        )
                    )
                }
            }

            return AlbumDetailUiState(
                albumId = albumId,
                albumName = albumName,
                description = "",
                songCount = songCount,
                type = "Album",
                year = year,
                albumImages = albumImages,
                primaryArtists = primaryArtists,
                songs = songs,
                totalDuration = totalDuration
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return AlbumDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }
}