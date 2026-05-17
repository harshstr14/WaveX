package com.example.wavex.playlistScreen

import com.example.wavex.HttpClientProvider
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import com.example.wavex.songData.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class PlaylistRepository {
    suspend fun fetchPlaylistById(playlistId: String): PlaylistDetailUiState  =
        withContext(Dispatchers.IO) {
            val response = requestWithFallback("/playlists?id=$playlistId&limit=40")
            if (response.isEmpty()) {
                return@withContext PlaylistDetailUiState(
                    isError = true,
                    errorMessage = "Network error. Please try again."
                )
            }

            parsePlaylist(response)
        }

    private fun parsePlaylist(jsonString: String): PlaylistDetailUiState {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) {
            return PlaylistDetailUiState(
                isError = true,
                errorMessage = "Playlist not found"
            )
        }

        val data = json.getJSONObject("data")

        val images = parseImages(data.optJSONArray("image"))
        val artists = parseArtistsArray(data.optJSONArray("artists"))

        val songArray = data.optJSONArray("songs")
        val songs = mutableListOf<SongItem>()
        var totalDuration = 0

        if (songArray != null) {
            for (i in 0 until songArray.length()) {
                val song = songArray.getJSONObject(i)

                val duration = song.optInt("duration")
                totalDuration += duration

                val albumObject = song.optJSONObject("album")
                val album = Album(
                    id = albumObject?.optString("id") ?: "",
                    name = albumObject?.optString("name") ?: ""
                )

                songs.add(
                    SongItem(
                        id = song.optString("id"),
                        name = song.optString("name"),
                        artist = parsePrimaryArtists(song.optJSONObject("artists")).toMutableList(),
                        album = album,
                        image = parseImages(song.optJSONArray("image")).toMutableList(),
                        duration = duration,
                        playCount = song.optInt("playCount"),
                        downloadUrl = parseDownloads(song.optJSONArray("downloadUrl")).toMutableList(),
                        searchSource = "jiosaavn"
                    )
                )
            }
        }

        return PlaylistDetailUiState(
            id = data.optString("id"),
            name = data.optString("name"),
            description = data.optString("description"),
            songCount = data.optString("songCount"),
            images = images,
            artists = artists,
            songs = songs,
            totalDuration = totalDuration
        )
    }

    private fun parseArtistsArray(array: org.json.JSONArray?): List<Artists> {
        if (array == null) return emptyList()
        return List(array.length()) {
            val artist = array.getJSONObject(it)
            val imageUrl = artist.optJSONArray("image")
                ?.optJSONObject(2)
                ?.optString("url") ?: ""

            Artists(
                id = artist.optString("id"),
                name = artist.optString("name"),
                role = artist.optString("role"),
                image = imageUrl,
                type = artist.optString("type")
            )
        }
    }

    private fun parsePrimaryArtists(obj: JSONObject?): List<Artists> {
        val primary = obj?.optJSONArray("primary") ?: return emptyList()
        return List(primary.length()) {
            val artist = primary.getJSONObject(it)
            val imageUrl = artist.optJSONArray("image")
                ?.optJSONObject(2)
                ?.optString("url") ?: ""

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

    suspend fun fetchYTMusicPlaylist(
        playlistId: String,
        baseUrl: String
    ): PlaylistDetailUiState = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/playlists/$playlistId")
                .get()
                .build()

            val call = HttpClientProvider.client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            val response = call.execute()

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
                    errorMessage = "Empty response received."
                )
            }

            parseYTMusicPlaylist(body)
        } catch (e: Exception) {
            e.printStackTrace()
            PlaylistDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }

    fun parseYTMusicPlaylist(jsonString: String): PlaylistDetailUiState {
        try {
            val json = JSONObject(jsonString)

            val playlistId = json.optString("playlistId")
            val title = json.optString("title")
            val description = json.optString("description")
            val thumbnail = json.optString("thumbnail")
            val trackCount = json.optInt("trackCount", 0)

            val images = listOf(
                Image(
                    quality = "high",
                    url = thumbnail
                )
            )

            val artists = mutableListOf<Artists>()
            val author = json.optString("author")

            if (author.isNotBlank()) {
                artists.add(
                    Artists(
                        id = "",
                        name = author,
                        role = "Creator",
                        image = ""
                    )
                )
            }

            val songs = mutableListOf<SongItem>()
            val tracksArray = json.optJSONArray("tracks")
            var totalDuration = 0

            if (tracksArray != null) {
                for (i in 0 until tracksArray.length()) {
                    val item = tracksArray.getJSONObject(i)

                    val durationText = item.optString("duration")

                    val durationSeconds = durationText
                        .split(":")
                        .let {

                            val minutes = it.getOrNull(0)?.toIntOrNull() ?: 0
                            val seconds = it.getOrNull(1)?.toIntOrNull() ?: 0

                            (minutes * 60) + seconds
                        }

                    totalDuration += durationSeconds


                    val songImages = mutableListOf<Image>()
                    val thumbnailsArray = item.optJSONArray("thumbnails")

                    if (thumbnailsArray != null) {
                        for (j in 0 until thumbnailsArray.length()) {
                            val thumb = thumbnailsArray.getJSONObject(j)

                            songImages.add(
                                Image(
                                    quality = "high",
                                    url = thumb.optString("url")
                                )
                            )
                        }
                    }

                    val songArtists = mutableListOf<Artists>()
                    val artistsArray = item.optJSONArray("artists")

                    if (artistsArray != null) {
                        for (j in 0 until artistsArray.length()) {
                            val artist = artistsArray.getJSONObject(j)

                            songArtists.add(
                                Artists(
                                    id = artist.optString("id"),
                                    name = artist.optString("name"),
                                    role = "Artist",
                                    image = ""
                                )
                            )
                        }
                    }

                    songs.add(
                        SongItem(
                            id = item.optString("videoId"),
                            name = item.optString("title"),
                            duration = durationSeconds,
                            image = songImages,
                            artist = songArtists,
                            searchSource = "ytmusic"
                        )
                    )
                }
            }

            return PlaylistDetailUiState(
                id = playlistId,
                name = title,
                description = description,
                songCount = trackCount.toString(),
                images = images,
                artists = artists,
                songs = songs,
                totalDuration = totalDuration
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return PlaylistDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }
}