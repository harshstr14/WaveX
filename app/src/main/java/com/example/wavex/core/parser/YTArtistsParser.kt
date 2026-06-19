package com.example.wavex.core.parser

import com.example.wavex.core.model.Artists
import org.json.JSONArray

object YTArtistsParser {
    suspend fun parse(array: JSONArray?): List<Artists> {
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
}