package com.example.wavex.artistScreen

import com.example.musify.songData.Artists
import com.example.musify.songData.Download
import com.example.musify.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ArtistRepository {
    suspend fun fetchArtistById(artistId: String): ArtistDetailUiState =
        withContext(Dispatchers.IO) {
            val response = requestWithFallback("/artists?id=$artistId")
            if (response.isEmpty()) {
                return@withContext ArtistDetailUiState(
                    isError = true,
                    errorMessage = "Network error. Please try again."
                )
            }

            parseArtist(response)
        }

    private fun parseArtist(jsonString: String): ArtistDetailUiState {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) {
            return ArtistDetailUiState(
                isError = true,
                errorMessage = "Artist not found"
            )
        }

        val data = json.getJSONObject("data")

        val imageUrl = data.optJSONArray("image")
            ?.optJSONObject(2)
            ?.optString("url") ?: ""

        return ArtistDetailUiState(
            id = data.optString("id"),
            name = data.optString("name"),
            followerCount = data.optInt("followerCount"),
            fanCount = data.optString("fanCount"),
            isVerified = data.optBoolean("isVerified"),
            imageUrl = imageUrl,
            topSongs = parseTopSongs(data),
            topAlbums = parseAlbums(data.optJSONArray("topAlbums")),
            singles = parseAlbums(data.optJSONArray("singles"))
        )
    }

    private fun parseTopSongs(data: JSONObject): List<SongItem> {
        val list = mutableListOf<SongItem>()
        val array = data.optJSONArray("topSongs") ?: return emptyList()

        for (i in 0 until array.length()) {
            val song = array.getJSONObject(i)

            val images = song.optJSONArray("image")?.let { arr ->
                MutableList(arr.length()) { idx ->
                    val obj = arr.getJSONObject(idx)
                    Image(obj.optString("quality"), obj.optString("url"))
                }
            } ?: emptyList()

            val downloads = song.optJSONArray("downloadUrl")?.let { arr ->
                MutableList(arr.length()) { idx ->
                    val obj = arr.getJSONObject(idx)
                    Download(obj.optString("quality"), obj.optString("url"))
                }
            } ?: emptyList()

            val artists = parsePrimaryArtists(song.optJSONObject("artists"))

            list.add(
                SongItem(
                    id = song.optString("id"),
                    name = song.optString("name"),
                    artist = artists.toMutableList(),
                    image = images.toMutableList(),
                    duration = song.optInt("duration"),
                    downloadUrl = downloads.toMutableList()
                )
            )
        }

        return list
    }

    private fun parseAlbums(array: org.json.JSONArray?): List<DataItem> {
        if (array == null) return emptyList()

        val list = mutableListOf<DataItem>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            val imageUrl = parseImages(item.optJSONArray("image"))

            val artistName = parsePrimaryArtists(item.optJSONObject("artists"))

            list.add(
                DataItem(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    artist = artistName.toMutableList(),
                    image = imageUrl.toMutableList()
                )
            )
        }

        return list
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
}