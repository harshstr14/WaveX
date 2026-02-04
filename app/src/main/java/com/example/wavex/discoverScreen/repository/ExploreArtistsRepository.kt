package com.example.wavex.discoverScreen.repository

import com.example.musify.songData.Artists
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ExploreArtistsRepository {

    suspend fun fetchArtists(query: String,root: String): List<Artists> = withContext(Dispatchers.IO) {

        val response = requestWithFallback("/search/artists?query=$query&limit=20")

        if (response.isEmpty()) return@withContext emptyList()

        parseArtistsJson(response, root)
    }

    private fun parseArtistsJson(jsonString: String,root: String): List<Artists> {

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success")) return emptyList()

        val resultArray = json.getJSONObject("data").optJSONArray(root) ?: return emptyList()

        val artists = mutableListOf<Artists>()

        for (i in 0 until resultArray.length()) {
            val artist = resultArray.getJSONObject(i)

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