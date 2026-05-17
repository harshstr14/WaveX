package com.example.wavex.searchScreen.repository

import com.example.wavex.HttpClientProvider
import com.example.wavex.requestWithFallback
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class SearchArtistsRepository {

    suspend fun searchArtists(query: String,root: String): List<Artists> = withContext(Dispatchers.IO) {

        val response = requestWithFallback(
            "/search/artists?query=$query&limit=20"
        )

        if (response.isEmpty()) return@withContext emptyList()

        parseArtists(response, root)
    }

    private fun parseArtists(jsonString: String,root: String): List<Artists> {

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return emptyList()

        val resultArray = json
            .getJSONObject("data")
            .getJSONArray(root)

        val artists = mutableListOf<Artists>()

        for (i in 0 until resultArray.length()) {
            val artist = resultArray.getJSONObject(i)

            artists.add(
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

        return artists
    }

    suspend fun ytMusicSearch(query: String, baseUrl: String): List<Artists> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/search?q=$query&filter=artists")
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

    private fun parseYTMusicSearch(jsonString: String): List<Artists> {
        val json = JSONObject(jsonString)

        val resultsArray = json.optJSONArray("results")
            ?: return emptyList()

        val results = mutableListOf<Artists>()

        for (i in 0 until resultsArray.length()) {
            val item = resultsArray.getJSONObject(i)
            val browseId = item.optString("browseId")

            val thumbnails = mutableListOf<Image>()

            var imageUrl = ""

            item.optJSONArray("thumbnails")?.let { array ->
                for (j in 0 until array.length()) {
                    val thumb = array.getJSONObject(j)

                    imageUrl = thumb
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
                Artists(
                    id = browseId,
                    name = item.optString("title"),
                    image = imageUrl
                )
            )
        }

        return results
    }
}