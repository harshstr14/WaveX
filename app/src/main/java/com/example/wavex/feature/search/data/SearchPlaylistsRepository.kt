package com.example.wavex.feature.search.data

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
import timber.log.Timber
import java.net.URLEncoder

class SearchPlaylistsRepository {
    companion object {
        private const val TAG = "SearchPlaylistsRepo"
    }

    suspend fun searchPlaylists(query: String): List<DataItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val response = requestWithFallback(
                "/search/playlists?query=$encodedQuery&limit=20"
            )

            if (response.isEmpty()) {
                return@withContext emptyList()
            }

            parsePlaylists(response)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "searchPlaylists failed")
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
            Timber.tag(TAG).e(e, "ytMusicSearch failed")
            emptyList()
        }
    }

    private suspend fun parsePlaylists(jsonString: String): List<DataItem> {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return emptyList()
        }

        val playlistsArray = json
            .optJSONObject("data")
            ?.optJSONArray("results")
            ?: return emptyList()

        return buildList {
            for (i in 0 until playlistsArray.length()) {
                val playlist = playlistsArray.optJSONObject(i) ?: continue

                add(
                    DataItem(
                        id = playlist.optString("id"),
                        name = playlist.optString("name"),
                        artist = PrimaryArtistsParser.parse(playlist.optJSONObject("artists")).toMutableList(),
                        image = ImageParser.parse(playlist.optJSONArray("image")).toMutableList(),
                        searchSource = SearchSource.JIOSAAVN.toString()
                    )
                )
            }
        }.distinctBy { it.id }
    }

    private suspend fun parseYTMusicSearch(jsonString: String): List<DataItem> {
        val json = JSONObject(jsonString)

        val resultsArray =json.optJSONArray("results") ?: return emptyList()

        return buildList {
            for (i in 0 until resultsArray.length()) {
                val item =resultsArray.optJSONObject(i) ?: continue

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