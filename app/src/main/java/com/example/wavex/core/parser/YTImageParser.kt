package com.example.wavex.core.parser

import com.example.wavex.core.model.Image
import org.json.JSONArray

object YTImageParser {
    private val imageSizeRegex = Regex("w\\d+-h\\d+")

    suspend fun parse(array: JSONArray?): List<Image> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val thumb = array.optJSONObject(i) ?: continue

                val imageUrl = thumb
                    .optString("url")
                    .replace(imageSizeRegex, "w520-h520")

                add(
                    Image(
                        quality = "",
                        url = imageUrl
                    )
                )
            }
        }
    }
}