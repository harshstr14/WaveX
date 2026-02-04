package com.example.wavex.discoverScreen.repository

import com.example.musify.songData.Artists
import com.example.musify.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ExploreAlbumsRepository {
    suspend fun fetchAlbums(query: String,root: String): List<DataItem> = withContext(Dispatchers.IO) {

        val response = requestWithFallback("/search/albums?query=$query&limit=30")

        if (response.isEmpty()) return@withContext emptyList()

        parseAlbumListJson(response, root)
    }

    private fun parseAlbumListJson(jsonString: String,root: String): List<DataItem> {

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success")) return emptyList()

        val albumsArray = json.getJSONObject("data").optJSONArray(root) ?: return emptyList()

        val albums = mutableListOf<DataItem>()

        for (i in 0 until albumsArray.length()) {
            val album = albumsArray.getJSONObject(i)

            val images = parseImages(album.optJSONArray("image"))
            val artists = parsePrimaryArtists(album.optJSONObject("artists"))

            albums.add(
                DataItem(
                    id = album.optString("id"),
                    name = album.optString("name"),
                    artist = artists.toMutableList(),
                    image = images.toMutableList()
                )
            )
        }

        return albums
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