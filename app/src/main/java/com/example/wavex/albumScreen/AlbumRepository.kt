package com.example.wavex.albumScreen

import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

                totalDuration += song.optInt("duration")

                songs.add(
                    SongItem(
                        id = song.optString("id"),
                        name = song.optString("name"),
                        artist = artists,
                        image = images,
                        duration = song.optInt("duration"),
                        downloadUrl = downloads
                    )
                )
            }
        }

        return AlbumDetailUiState(
            albumId = data.optString("id"),
            albumName = data.optString("name"),
            description = data.optString("description"),
            songCount = data.optString("songCount"),
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
}