package com.example.wavex.searchScreen.repository

import com.example.wavex.HttpClientProvider
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class SearchAlbumsRepository {
    suspend fun searchAlbums(query: String,root: String): List<DataItem> = withContext(Dispatchers.IO) {
        val response = requestWithFallback("/search/albums?query=$query&limit=30")

        if (response.isEmpty()) return@withContext emptyList()

        parseAlbumListJson(response, root)
    }

    private fun parseAlbumListJson(jsonString: String,root: String): List<DataItem> {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success")) return emptyList()

        val albumsArray =
            json.getJSONObject("data").optJSONArray(root) ?: return emptyList()

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

    suspend fun ytMusicSearch(query: String, baseUrl: String): List<DataItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/search?q=$query&filter=albums")
                .get()
                .build()

            val call =  HttpClientProvider.client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            val response = call.execute()

            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            val body = response.body?.string().orEmpty()

            if (body.isEmpty()) {
                return@withContext emptyList()
            }

            parseYTMusicSearch(body)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseYTMusicSearch(jsonString: String): List<DataItem> {
        val json = JSONObject(jsonString)

        val resultsArray = json.optJSONArray("results")
            ?: return emptyList()

        val results = mutableListOf<DataItem>()

        for (i in 0 until resultsArray.length()) {
            val item = resultsArray.getJSONObject(i)
            val browseId = item.optString("browseId")

            val thumbnails = mutableListOf<Image>()

            item.optJSONArray("thumbnails")?.let { array ->
                for (j in 0 until array.length()) {
                    val thumb = array.getJSONObject(j)

                    val imageUrl = thumb
                        .optString("url")
                        .replace(Regex("w\\d+-h\\d+"), "w520-h520")

                    thumbnails.add(
                        Image(
                            quality = "",
                            url = imageUrl
                        )
                    )
                }
            }

            val artists = mutableListOf<Artists>()

            item.optJSONArray("artists")?.let { array ->
                for (j in 0 until array.length()) {
                    val artist = array.getJSONObject(j)

                    artists.add(
                        Artists(
                            id = artist.optString("id"),
                            name = artist.optString("name")
                        )
                    )
                }
            }

            results.add(
                DataItem(
                    id = browseId,
                    name = item.optString("title"),
                    artist = artists,
                    image = thumbnails
                )
            )
        }

        return results
    }
}