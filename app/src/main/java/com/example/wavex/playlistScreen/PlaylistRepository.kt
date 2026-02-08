package com.example.wavex.playlistScreen

import com.example.musify.songData.Artists
import com.example.musify.songData.Download
import com.example.musify.songData.Image
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

                songs.add(
                    SongItem(
                        id = song.optString("id"),
                        name = song.optString("name"),
                        artist = parsePrimaryArtists(song.optJSONObject("artists")).toMutableList(),
                        image = parseImages(song.optJSONArray("image")).toMutableList(),
                        duration = duration,
                        downloadUrl = parseDownloads(song.optJSONArray("downloadUrl")).toMutableList()
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
}