package com.example.wavex.core.parser

import com.example.wavex.core.model.Album
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.Download
import com.example.wavex.core.model.Image
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.model.Quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object SongParser {
    suspend fun parse(
        jsonString: String,
        root: String
    ): List<SongItem> = withContext(Dispatchers.IO) {
        val parsedSongs = mutableListOf<SongItem>()
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success", false)) return@withContext emptyList()

        val songsArray = json.getJSONObject("data").getJSONArray(root)

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val id = song.optString("id")
            val name = song.optString("name")
            val duration = song.optInt("duration")
            val playCount = song.optInt("playCount")

            val image = mutableListOf<Image>()
            song.optJSONArray("image")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    image.add(Image(obj.optString("quality"), obj.optString("url")))
                }
            }

            val download = mutableListOf<Download>()
            song.optJSONArray("downloadUrl")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)

                    val qualityString = obj.optString("quality")

                    val quality = when (qualityString.lowercase()) {
                        "12kbps", "48kbps" -> Quality.LOW
                        "96kbps", "160kbps" -> Quality.MEDIUM
                        "320kbps" -> Quality.HIGH
                        else -> Quality.MEDIUM
                    }

                    download.add(
                        Download(
                            quality = quality,
                            url = obj.optString("url")
                        )
                    )
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

            val albumObject = song.optJSONObject("album")
            val album = Album(
                id = albumObject?.optString("id") ?: "",
                name = albumObject?.optString("name") ?: ""
            )

            parsedSongs.add(
                SongItem(id, name, primaryArtists, album, image, duration, playCount, download)
            )
        }

        parsedSongs
    }
}