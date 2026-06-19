package com.example.wavex.core.parser

import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.DataItem
import com.example.wavex.core.model.Image
import com.example.wavex.feature.search.presentation.SearchSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object PlaylistParser {
    suspend fun parse(
        jsonString: String,
        root: String
    ): List<DataItem> = withContext(Dispatchers.IO) {
        val parsedPlaylist = mutableListOf<DataItem>()

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return@withContext emptyList()

        val songsArray = json.getJSONObject("data").getJSONArray(root)

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val id = song.optString("id")
            val name = song.optString("name")

            val image = mutableListOf<Image>()
            song.optJSONArray("image")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    image.add(Image(obj.optString("quality"), obj.optString("url")))
                }
            }

            val primaryArtists = mutableListOf<Artists>()
            song.optJSONObject("artists")
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

            parsedPlaylist.add(
                DataItem(id, name, primaryArtists, image, searchSource = SearchSource.JIOSAAVN.name)
            )
        }

        parsedPlaylist
    }
}