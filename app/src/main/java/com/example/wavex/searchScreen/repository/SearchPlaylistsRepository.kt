package com.example.wavex.searchScreen.repository

import com.example.wavex.HttpClientProvider
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class SearchPlaylistsRepository {

    suspend fun searchPlaylists(query: String,root: String): List<DataItem> = withContext(Dispatchers.IO) {

        val response = requestWithFallback(
            "/search/playlists?query=$query&limit=20"
        )

        if (response.isEmpty()) return@withContext emptyList()

        parsePlaylists(response, root)
    }

    private fun parsePlaylists(jsonString: String,root: String): List<DataItem> {

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return emptyList()

        val playlistsArray = json
            .getJSONObject("data")
            .getJSONArray(root)

        val playlists = mutableListOf<DataItem>()

        for (i in 0 until playlistsArray.length()) {
            val playlist = playlistsArray.getJSONObject(i)

            val id = playlist.optString("id")
            val name = playlist.optString("name")

            val image = mutableListOf<Image>()
            playlist.optJSONArray("image")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    image.add(
                        Image(
                            quality = obj.optString("quality"),
                            url = obj.optString("url")
                        )
                    )
                }
            }

            val primaryArtists = mutableListOf<Artists>()
            playlist.optJSONObject("artists")
                ?.optJSONArray("primary")
                ?.let { arr ->
                    for (j in 0 until arr.length()) {
                        val artist = arr.getJSONObject(j)
                        primaryArtists.add(
                            Artists(
                                id = artist.optString("id"),
                                name = artist.optString("name"),
                                role = artist.optString("role"),
                                image = artist.optJSONArray("image")
                                    ?.optJSONObject(2)
                                    ?.optString("url") ?: "",
                                type = artist.optString("type")
                            )
                        )
                    }
                }

            playlists.add(
                DataItem(
                    id = id,
                    name = name,
                    artist = primaryArtists,
                    image = image
                )
            )
        }

        return playlists
    }

    suspend fun ytMusicSearch(query: String, baseUrl: String): List<DataItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/search?q=$query&filter=playlists")
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