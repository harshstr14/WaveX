package com.example.wavex.core.parser

import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.DataItem
import com.example.wavex.core.model.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object AlbumParser {
    suspend fun parse(
        jsonString: String,
        root: String
    ): List<DataItem> = withContext(Dispatchers.IO) {
        val parsedAlbums = mutableListOf<DataItem>()

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return@withContext emptyList()

        val albumsArray = json.getJSONObject("data").getJSONArray(root)

        for (i in 0 until albumsArray.length()) {
            val album = albumsArray.getJSONObject(i)

            val id = album.optString("id")
            val name = album.optString("name")

            val image = mutableListOf<Image>()
            album.optJSONArray("image")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    image.add(Image(obj.optString("quality"), obj.optString("url")))
                }
            }

            val primaryArtists = mutableListOf<Artists>()
            album.optJSONObject("artists")
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

            parsedAlbums.add(
                DataItem(
                    id = id,
                    name = name,
                    artist = primaryArtists,
                    image = image
                )
            )
        }

        parsedAlbums
    }
}