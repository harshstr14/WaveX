package com.example.wavex.discoverScreen.repository

import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import com.example.wavex.songData.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ExploreSongsRepository {

    suspend fun fetchSongsByPlaylist(playlistId: String,root: String): List<SongItem> = withContext(Dispatchers.IO) {

        val response = requestWithFallback("/playlists?id=$playlistId&limit=40")

        if (response.isEmpty()) return@withContext emptyList()

        parseSongsJson(response, root)
    }

    private fun parseSongsJson(jsonString: String,root: String): List<SongItem> {

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success")) return emptyList()

        val songsArray =
            json.getJSONObject("data").optJSONArray(root) ?: return emptyList()

        val songs = mutableListOf<SongItem>()

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val images = parseImages(song.optJSONArray("image"))
            val downloads = parseDownloads(song.optJSONArray("downloadUrl"))
            val artists = parsePrimaryArtists(song.optJSONObject("artists"))
            val albumObject = song.optJSONObject("album")
            val album = Album(
                id = albumObject?.optString("id") ?: "",
                name = albumObject?.optString("name") ?: ""
            )

            songs.add(
                SongItem(
                    id = song.optString("id"),
                    name = song.optString("name"),
                    artist = artists.toMutableList(),
                    album = album,
                    image = images.toMutableList(),
                    duration = song.optInt("duration"),
                    downloadUrl = downloads.toMutableList()
                )
            )
        }

        return songs
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