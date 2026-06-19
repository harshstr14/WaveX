package com.example.wavex.feature.search.data

import android.util.Log
import com.example.wavex.HttpClientProvider
import com.example.wavex.core.model.DataItem
import com.example.wavex.core.parser.ImageParser
import com.example.wavex.core.parser.PrimaryArtistsParser
import com.example.wavex.core.parser.YTArtistsParser
import com.example.wavex.core.parser.YTImageParser
import com.example.wavex.feature.search.presentation.SearchSource
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class SearchAlbumsRepository {
    companion object {
        private const val TAG = "SearchAlbumsRepository"
    }

    suspend fun searchAlbums(query: String): List<DataItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val response = requestWithFallback(
                "/search/albums?query=$encodedQuery&limit=30"
            )

            if (response.isEmpty()) {
                return@withContext emptyList()
            }

            parseAlbumListJson(response)
        } catch (e: Exception) {
            Log.e(TAG, "searchAlbums failed", e)
            emptyList()
        }
    }

    suspend fun ytMusicSearch(query: String,baseUrl: String): List<DataItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val request = Request.Builder()
                .url("$baseUrl/search?q=$encodedQuery&filter=albums&page=20")
                .get()
                .build()

            val call = HttpClientProvider.client.newCall(request)

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

    private suspend fun parseAlbumListJson(jsonString: String): List<DataItem> {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return emptyList()
        }

        val albumsArray = json
            .optJSONObject("data")
            ?.optJSONArray("results")
            ?: return emptyList()

        return buildList {
            for (i in 0 until albumsArray.length()) {
                val album = albumsArray.optJSONObject(i) ?: continue

                add(
                    DataItem(
                        id = album.optString("id"),
                        name = album.optString("name"),
                        artist = PrimaryArtistsParser.parse(album.optJSONObject("artists")).toMutableList(),
                        image = ImageParser.parse(album.optJSONArray("image")).toMutableList(),
                        searchSource = SearchSource.JIOSAAVN.toString()
                    )
                )
            }
        }.distinctBy { it.id }
    }

    private suspend fun parseYTMusicSearch(jsonString: String): List<DataItem> {
        val json = JSONObject(jsonString)

        val resultsArray = json.optJSONArray("results") ?: return emptyList()

        return buildList {
            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.optJSONObject(i) ?: continue

                add(
                    DataItem(
                        id = item.optString("browseId"),
                        name = item.optString("title"),
                        artist = YTArtistsParser.parse(item.optJSONArray("artists")).toMutableList(),
                        image = YTImageParser.parse(item.optJSONArray("thumbnails")).toMutableList(),
                        searchSource = SearchSource.YTMUSIC.toString()
                    )
                )
            }
        }.distinctBy { it.id }
    }
}