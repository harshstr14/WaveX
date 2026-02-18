package com.example.wavex.artistScreen.allSongsScreen

import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import com.example.wavex.songData.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AllSongsRepository {

    suspend fun fetchSongsByArtistID(artistId: String,root: String, page: Int): AllSongsUiState = withContext(Dispatchers.IO) {

        val response = requestWithFallback("/artists/$artistId/songs?page=$page")

        if (response.isEmpty()) {
            return@withContext AllSongsUiState(
                isError = true,
                errorMessage = "Network error. Please try again."
            )
        }

        parseSongsJson(response, root)
    }

    private fun parseSongsJson(
        jsonString: String,
        root: String
    ): AllSongsUiState {

        val json = JSONObject(jsonString)

        if (!json.optBoolean("success", false)) {
            return AllSongsUiState(
                isError = true,
                errorMessage = "Artist not found"
            )
        }

        val songsArray =
            json.getJSONObject("data").optJSONArray(root)
                ?: return AllSongsUiState(
                    isError = true,
                    errorMessage = "No songs found"
                )

        val songs = mutableListOf<SongItem>()

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)
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
                    duration = song.optInt("duration"),
                    downloadUrl = parseDownloads(song.optJSONArray("downloadUrl")).toMutableList()
                )
            )
        }

        return AllSongsUiState(songs = songs)
    }

    private fun parseImages(array: JSONArray?): List<Image> {
        if (array == null) return emptyList()
        return List(array.length()) {
            val obj = array.getJSONObject(it)
            Image(
                quality = obj.optString("quality"),
                url = obj.optString("url")
            )
        }
    }

    private fun parseDownloads(array: JSONArray?): List<Download> {
        if (array == null) return emptyList()
        return List(array.length()) {
            val obj = array.getJSONObject(it)
            Download(
                quality = obj.optString("quality"),
                url = obj.optString("url")
            )
        }
    }

    private fun parsePrimaryArtists(artistsObj: JSONObject?): List<Artists> {
        val primary = artistsObj?.optJSONArray("primary") ?: return emptyList()

        val artists = mutableListOf<Artists>()

        for (i in 0 until primary.length()) {
            val artist = primary.getJSONObject(i)

            val imageUrl = artist.optJSONArray("image")
                    ?.optJSONObject(2)
                    ?.optString("url") ?: ""

            artists.add(
                Artists(
                    id = artist.optString("id"),
                    name = artist.optString("name"),
                    role = artist.optString("role"),
                    image = imageUrl,
                    type = artist.optString("type")
                )
            )
        }

        return artists
    }
}