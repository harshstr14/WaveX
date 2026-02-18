package com.example.wavex.artistScreen.allAlbumsScreen

import com.example.wavex.songData.Artists
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AllAlbumsRepository {
    suspend fun fetchAlbums(artistId: String,root: String, page: Int): AllAlbumsUiState = withContext(Dispatchers.IO) {

        val response = requestWithFallback("/artists/$artistId/albums?page=$page")

        if (response.isEmpty()) {
            return@withContext AllAlbumsUiState(
                isError = true,
                errorMessage = "Network error. Please try again."
            )
        }

        parseAlbumListJson(response, root)
    }


    private fun parseAlbumListJson(jsonString: String,root: String): AllAlbumsUiState {

        val json = JSONObject(jsonString)

        if (!json.optBoolean("success", false)) {
            return AllAlbumsUiState(
                isError = true,
                errorMessage = "Something went wrong"
            )
        }

        val albumsArray =
            json.getJSONObject("data").optJSONArray(root)
                ?: return AllAlbumsUiState(
                    isError = false,
                    albums = emptyList()
                )

        val albums = mutableListOf<DataItem>()

        for (i in 0 until albumsArray.length()) {
            val album = albumsArray.getJSONObject(i)

            albums.add(
                DataItem(
                    id = album.optString("id"),
                    name = album.optString("name"),
                    artist = parsePrimaryArtists(album.optJSONObject("artists")).toMutableList(),
                    image = parseImages(album.optJSONArray("image")).toMutableList()
                )
            )
        }

        return AllAlbumsUiState(albums = albums)
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