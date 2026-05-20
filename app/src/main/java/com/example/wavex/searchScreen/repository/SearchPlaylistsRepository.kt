package com.example.wavex.searchScreen.repository

import android.util.Log
import com.example.wavex.HttpClientProvider
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.requestWithFallback
import com.example.wavex.searchScreen.SearchSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

class SearchPlaylistsRepository {
    companion object {
        private const val TAG = "SearchPlaylistsRepository"
        private val imageSizeRegex = Regex("w\\d+-h\\d+")
    }

    suspend fun searchPlaylists(query: String,root: String): List<DataItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val response = requestWithFallback(
                "/search/playlists?query=$encodedQuery&limit=20"
            )

            if (response.isEmpty()) {
                return@withContext emptyList()
            }

            parsePlaylists(response, root)
        } catch (e: Exception) {
            Log.e(TAG, "searchPlaylists failed", e)
            emptyList()
        }
    }

    suspend fun ytMusicSearch(query: String,baseUrl: String): List<DataItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val request = Request.Builder()
                .url("$baseUrl/search?q=$encodedQuery&filter=playlists&page=20")
                .get()
                .build()

            val call =
                HttpClientProvider.client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            call.execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext emptyList()
                }

                val body = response.body?.string().orEmpty()

                if (body.isEmpty()) {
                    return@withContext emptyList()
                }

                parseYTMusicSearch(body)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ytMusicSearch failed", e)
            emptyList()
        }
    }

    private fun parsePlaylists(jsonString: String,root: String): List<DataItem> {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return emptyList()
        }

        val playlistsArray = json
            .optJSONObject("data")
            ?.optJSONArray(root)
            ?: return emptyList()

        return buildList {
            for (i in 0 until playlistsArray.length()) {
                val playlist = playlistsArray.optJSONObject(i) ?: continue

                add(
                    DataItem(
                        id = playlist.optString("id"),
                        name = playlist.optString("name"),
                        artist = parsePrimaryArtists(
                            playlist.optJSONObject("artists")
                        ).toMutableList(),
                        image = parseImages(
                            playlist.optJSONArray("image")
                        ).toMutableList(),
                        searchSource = SearchSource.JIOSAAVN.toString()
                    )
                )
            }
        }.distinctBy { it.id }
    }

    private fun parseYTMusicSearch(jsonString: String): List<DataItem> {
        val json = JSONObject(jsonString)

        val resultsArray =json.optJSONArray("results") ?: return emptyList()

        return buildList {
            for (i in 0 until resultsArray.length()) {
                val item =resultsArray.optJSONObject(i) ?: continue

                add(
                    DataItem(
                        id = item.optString("browseId"),
                        name = item.optString("title"),
                        artist = parseYTArtists(
                            item.optJSONArray("artists")
                        ).toMutableList(),
                        image = parseYTImages(
                            item.optJSONArray("thumbnails")
                        ).toMutableList(),
                        searchSource = SearchSource.YTMUSIC.toString()
                    )
                )
            }
        }.distinctBy { it.id }
    }

    private fun parseImages(array: JSONArray?): List<Image> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue

                add(
                    Image(
                        quality = obj.optString("quality"),
                        url = obj.optString("url")
                    )
                )
            }
        }
    }

    private fun parsePrimaryArtists(artistsObj: JSONObject?): List<Artists> {
        val primary = artistsObj
            ?.optJSONArray("primary")
            ?: return emptyList()

        return buildList {
            for (i in 0 until primary.length()) {
                val artist = primary.optJSONObject(i) ?: continue

                add(
                    Artists(
                        id = artist.optString("id"),
                        name = artist.optString("name"),
                        role = artist.optString("role"),
                        image = artist.optJSONArray("image")
                            ?.optJSONObject(2)
                            ?.optString("url")
                            .orEmpty(),
                        type = artist.optString("type")
                    )
                )
            }
        }
    }

    private fun parseYTArtists(array: JSONArray?): List<Artists> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val artist = array.optJSONObject(i) ?: continue

                add(
                    Artists(
                        id = artist.optString("id"),
                        name = artist.optString("name")
                    )
                )
            }
        }
    }

    private fun parseYTImages(array: JSONArray?): List<Image> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val thumb = array.optJSONObject(i) ?: continue

                val imageUrl = thumb
                    .optString("url")
                    .replace(
                        imageSizeRegex,
                        "w520-h520"
                    )

                add(
                    Image(
                        quality = "",
                        url = imageUrl
                    )
                )
            }
        }
    }
}