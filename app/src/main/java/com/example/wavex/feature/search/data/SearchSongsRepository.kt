package com.example.wavex.feature.search.data

import androidx.core.net.toUri
import com.example.wavex.HttpClientProvider
import com.example.wavex.core.model.Album
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.Download
import com.example.wavex.core.model.Image
import com.example.wavex.core.model.Quality
import com.example.wavex.core.model.SongItem
import com.example.wavex.feature.search.presentation.SearchSource
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.net.URLEncoder
import javax.inject.Inject

class SearchSongsRepository @Inject constructor() {
    companion object {
        private const val TAG = "SearchSongsRepository"
        private val imageSizeRegex = Regex("w\\d+-h\\d+")
    }

    suspend fun searchSongs(query: String): List<SongItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val response = requestWithFallback(
                "/search/songs?query=$encodedQuery&limit=30"
            )

            if (response.isEmpty()) {
                return@withContext emptyList()
            }

            parseSongs(response)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "searchSongs failed")
            emptyList()
        }
    }

    suspend fun fetchSuggestionSongs(songId: String): List<SongItem> = withContext(Dispatchers.IO) {
        try {
            val response = requestWithFallback(
                "/songs/$songId/suggestions?limit=30"
            )

            if (response.isEmpty()) {
                return@withContext emptyList()
            }

            parseSuggestionSongs(response)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "fetchSuggestionSongs failed")
            emptyList()
        }
    }

    suspend fun ytMusicSearch(query: String,baseUrl: String): List<SongItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val request = Request.Builder()
                .url("$baseUrl/search?q=$encodedQuery&filter=songs&page=20")
                .get()
                .build()

            val call = HttpClientProvider.client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            call.execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext emptyList()
                }

                val body = response.body?.string().orEmpty()

                if (body.isEmpty()) {
                    return@withContext emptyList()
                }

                parseYTMusicSearch(body)
            }

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "ytMusicSearch failed")
            emptyList()
        }
    }

    private fun parseSongs(jsonString: String): List<SongItem> {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return emptyList()
        }

        val songsArray = json
            .optJSONObject("data")
            ?.optJSONArray("results")
            ?: return emptyList()

        return buildList {
            for (i in 0 until songsArray.length()) {
                val song = songsArray.optJSONObject(i) ?: continue

                add(parseSong(song))
            }
        }.distinctBy { it.id }
    }

    private fun parseSuggestionSongs(jsonString: String): List<SongItem> {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return emptyList()
        }

        val songsArray = json.optJSONArray("data") ?: return emptyList()

        return buildList {
            for (i in 0 until songsArray.length()) {
                val song = songsArray.optJSONObject(i) ?: continue

                add(parseSong(song))
            }
        }.distinctBy { it.id }
    }

    private fun parseSong(song: JSONObject): SongItem {
        val albumObject = song.optJSONObject("album")

        return SongItem(
            id = song.optString("id"),
            name = song.optString("name"),
            artist = parseArtists(song).toMutableList(),
            album = Album(
                id = albumObject?.optString("id").orEmpty(),
                name = albumObject?.optString("name").orEmpty()
            ),
            image = parseImages(song.optJSONArray("image")).toMutableList(),
            duration = song.optInt("duration"),
            playCount = song.optInt("playCount"),
            downloadUrl = parseDownloads(song.optJSONArray("downloadUrl")).toMutableList(),
            songSource = SearchSource.JIOSAAVN.toString()
        )
    }

    private fun parseImages(array: JSONArray?): List<Image> {
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

    private fun parseDownloads(array: JSONArray?): List<Download> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue

                val qualityString = obj.optString("quality")

                val quality = when (qualityString.lowercase()) {
                    "12kbps", "48kbps" -> Quality.LOW
                    "96kbps", "160kbps" -> Quality.MEDIUM
                    "320kbps" -> Quality.HIGH
                    else -> Quality.MEDIUM
                }

                add(
                    Download(
                        quality = quality,
                        url = obj.optString("url")
                    )
                )
            }
        }
    }

    private fun parseArtists(song: JSONObject): List<Artists> {
        val artistsArray = song
            .optJSONObject("artists")
            ?.optJSONArray("primary")
            ?: return emptyList()

        return buildList {
            for (i in 0 until artistsArray.length()) {
                val artist = artistsArray.optJSONObject(i) ?: continue

                add(
                    Artists(
                        id = artist.optString("id"),
                        name = artist.optString("name"),
                        role = artist.optString("role"),
                        image = artist.optJSONArray("image")
                            ?.optJSONObject(2)
                            ?.optString("url")
                            .orEmpty(),
                        type = artist.optString("type"),
                        searchSource = SearchSource.JIOSAAVN.name
                    )
                )
            }
        }
    }

    private fun parseYTMusicSearch(jsonString: String): List<SongItem> {
        val json = JSONObject(jsonString)

        val resultsArray = json.optJSONArray("results")
            ?: return emptyList()

        return buildList {
            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.optJSONObject(i) ?: continue

                add(
                    SongItem(
                        id = item.optString("videoId"),
                        name = item.optString("title"),
                        artist = parseYTArtists(
                            item.optJSONArray("artists")
                        ).toMutableList(),
                        image = parseYTImages(
                            item.optJSONArray("thumbnails")
                        ).toMutableList(),
                        songSource = SearchSource.YTMUSIC.toString()
                    )
                )
            }
        }.distinctBy { it.id }
    }

    private fun parseYTArtists(array: JSONArray?): List<Artists> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val artist = array.optJSONObject(i) ?: continue
                add(
                    Artists(
                        id = artist.optString("id"),
                        name = artist.optString("name"),
                        searchSource = SearchSource.YTMUSIC.name
                    )
                )
            }
        }
    }

    private fun parseYTImages(array: JSONArray?): List<Image> {
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

    suspend fun fetchYTStreamData(songId: String,baseUrl: String): SongItem? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/$songId")
                .get()
                .build()

            HttpClientProvider.client.newCall(request)
                .execute()
                .use { response ->

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    val body = response.body?.string().orEmpty()

                    if (body.isEmpty()) {
                        return@withContext null
                    }

                    Timber.tag("YT_STREAM").d(
                        """
                        Response Length: ${body.length}
                        Response:
                        $body
                        """.trimIndent()
                    )

                    parseYTStream(body)
                }

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "fetchYTStreamData failed")
            null
        }
    }

    private fun parseYTStream(jsonString: String): SongItem? {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return null
        }

        Timber.tag("YT_STREAM").d("Success = ${json.optBoolean("success")}")

        val metadata = json.optJSONObject("metadata")
            ?: return null

        Timber.tag("YT_STREAM").d("Metadata null = ${metadata == null}")

        val streams = json.optJSONArray("streamingUrls")
            ?: JSONArray()

        Timber.tag("YT_STREAM").d("StreamingUrls Count = ${streams?.length() ?: 0}")

        return SongItem(
            duration = metadata.optInt("lengthSeconds"),
            downloadUrl = parseYTDownloads(streams).toMutableList(),
            songSource = SearchSource.YTMUSIC.name
        )
    }

    private fun parseYTDownloads(array: JSONArray?): List<Download> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue

                val mimeType = obj.optString("type")

                Timber.tag("YT_STREAM").d(
                    """
                    Stream[$i]
                    MimeType : $mimeType
                    Url Empty: ${obj.optString("directUrl").isEmpty()}
                    """.trimIndent()
                )

                if (!mimeType.contains("audio")) {
                    Timber.tag("YT_STREAM").d("Skipping non-audio stream: $mimeType")
                    continue
                }

                val url = obj.optString("directUrl")

                val uri = url.toUri()

                val itag = uri.getQueryParameter("itag")?.toIntOrNull()

                val expire = uri.getQueryParameter("expire")?.toLongOrNull()

                val quality = when (itag) {
                    249 -> Quality.LOW
                    250 -> Quality.MEDIUM
                    140 -> Quality.HIGH
                    251 -> Quality.LOSSLESS

                    else -> Quality.MEDIUM
                }

                Timber.tag("YT_STREAM").d(
                    """
                    Added stream
                    Itag    : $itag
                    Quality : $quality
                    Expire  : $expire
                    """.trimIndent()
                )

                add(
                    Download(
                        quality = quality,
                        url = url,
                        expiresAt = expire
                    )
                )
            }
        }
    }
}