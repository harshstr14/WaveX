package com.example.wavex.core.parser

import com.example.wavex.core.model.Artists
import org.json.JSONObject

object PrimaryArtistsParser {
    suspend fun parse(artistsObj: JSONObject?): List<Artists> {
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
}