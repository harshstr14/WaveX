package com.example.wavex.core.parser

import com.example.wavex.core.model.Image
import org.json.JSONArray

object ImageParser {
    suspend fun parse(array: JSONArray?): List<Image> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue

                add(
                    Image(
                        quality = obj.optString("quality"),
                        url = obj.optString("url")
                    )
                )
            }
        }
    }
}