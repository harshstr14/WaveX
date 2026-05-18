package com.example.wavex.searchScreen.repository

import android.util.Log
import com.example.wavex.HttpClientProvider
import com.example.wavex.requestWithFallback
import com.example.wavex.searchScreen.SearchSource
import com.example.wavex.songData.Artists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class SearchArtistsRepository {
    companion object {
        private const val TAG = "SearchArtistsRepository"
        private val imageSizeRegex = Regex("w\\d+-h\\d+")
    }

    suspend fun searchArtists(query: String,root: String): List<Artists> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val response = requestWithFallback(
                "/search/artists?query=$encodedQuery&limit=20"
            )

            if (response.isEmpty()) {
                return@withContext emptyList()
            }

            parseArtists(response, root)
        } catch (e: Exception) {
            Log.e(TAG, "searchArtists failed", e)
            emptyList()
        }
    }

    private fun parseArtists(jsonString: String,root: String): List<Artists> {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return emptyList()
        }

        val resultArray = json
            .optJSONObject("data")
            ?.optJSONArray(root)
            ?: return emptyList()

        return buildList {
            for (i in 0 until resultArray.length()) {
                val artist = resultArray.optJSONObject(i) ?: continue

                val imageArray = artist.optJSONArray("image")

                val imageUrl = imageArray
                    ?.optJSONObject(
                        minOf(2, imageArray.length() - 1)
                    )
                    ?.optString("url")
                    .orEmpty()

                add(
                    Artists(
                        id = artist.optString("id"),
                        name = artist.optString("name"),
                        role = artist.optString("role"),
                        image = imageUrl,
                        type = artist.optString("type"),
                        searchSource = SearchSource.JIOSAAVN.name
                    )
                )
            }
        }.distinctBy { it.id }
    }

    suspend fun ytMusicSearch(query: String,baseUrl: String): List<Artists> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val request = Request.Builder()
                .url(
                    "$baseUrl/search?q=$encodedQuery&filter=artists"
                )
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

    private fun parseYTMusicSearch(jsonString: String): List<Artists> {
        val json = JSONObject(jsonString)

        val resultsArray = json.optJSONArray("results") ?: return emptyList()

        return buildList {
            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.optJSONObject(i) ?: continue

                val imageUrl = item
                    .optJSONArray("thumbnails")
                    ?.optJSONObject(0)
                    ?.optString("url")
                    ?.replace(
                        imageSizeRegex,
                        "w520-h520"
                    )
                    .orEmpty()

                add(
                    Artists(
                        id = item.optString("browseId"),
                        name = item.optString("title"),
                        image = imageUrl,
                        searchSource = SearchSource.YTMUSIC.name
                    )
                )
            }
        }.distinctBy { it.id }
    }
}