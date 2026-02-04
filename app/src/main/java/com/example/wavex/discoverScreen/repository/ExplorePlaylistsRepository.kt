package com.example.wavex.discoverScreen.repository

import com.example.musify.songData.Artists
import com.example.musify.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ExplorePlaylistsRepository {

    suspend fun fetchPlaylists(query: String,root: String): List<DataItem> = withContext(Dispatchers.IO) {

        val response = requestWithFallback("/search/playlists?query=$query&limit=20")

        if (response.isEmpty()) return@withContext emptyList()

        parsePlaylistJson(response, root)
    }

    private fun parsePlaylistJson(jsonString: String,root: String): List<DataItem> {

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success")) return emptyList()

        val playlistsArray =
            json.getJSONObject("data").optJSONArray(root) ?: return emptyList()

        val playlists = mutableListOf<DataItem>()

        for (i in 0 until playlistsArray.length()) {
            val playlist = playlistsArray.getJSONObject(i)

            val images = parseImages(playlist.optJSONArray("image"))
            val artists = parsePrimaryArtists(playlist.optJSONObject("artists"))

            playlists.add(
                DataItem(
                    id = playlist.optString("id"),
                    name = playlist.optString("name"),
                    artist = artists.toMutableList(),
                    image = images.toMutableList()
                )
            )
        }

        return playlists
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