package com.example.wavex.searchScreen.repository

import com.example.wavex.songData.Artists
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
}