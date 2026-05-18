package com.example.wavex.artistScreen

import android.util.Log
import com.example.wavex.HttpClientProvider
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import com.example.wavex.searchScreen.SearchSource
import com.example.wavex.songData.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class ArtistRepository {
    companion object {
        private const val TAG = "ArtistRepository"
        private val imageSizeRegex = Regex("w\\d+-h\\d+")
    }

    suspend fun fetchArtistById(artistId: String): ArtistDetailUiState = withContext(Dispatchers.IO) {
        try {
            val response = requestWithFallback(
                "/artists?id=$artistId"
            )

            if (response.isEmpty()) {
                return@withContext ArtistDetailUiState(
                    isError = true,
                    errorMessage =
                        "Network error. Please try again."
                )
            }

            parseArtist(response)
        } catch (e: Exception) {
            Log.e(TAG,"fetchArtistById failed",e)

            ArtistDetailUiState(
                isError = true,
                errorMessage =
                    e.message ?: "Something went wrong."
            )
        }
    }

    suspend fun fetchYTMusicArtist(artistId: String,baseUrl: String): ArtistDetailUiState = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/artists/$artistId")
                .get()
                .build()

            val call = HttpClientProvider.client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            call.execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ArtistDetailUiState(
                        isError = true,
                        errorMessage =
                            "Network error. Please try again."
                    )
                }

                val body = response.body?.string().orEmpty()

                if (body.isEmpty()) {
                    return@withContext ArtistDetailUiState(
                        isError = true,
                        errorMessage =
                            "Empty response received."
                    )
                }

                parseYTMusicArtist(body)
            }
        } catch (e: Exception) {
            Log.e(TAG,"fetchYTMusicArtist failed",e)

            ArtistDetailUiState(
                isError = true,
                errorMessage =
                    e.message ?: "Something went wrong."
            )
        }
    }

    private fun parseArtist(jsonString: String): ArtistDetailUiState {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return ArtistDetailUiState(
                isError = true,
                errorMessage = "Artist not found"
            )
        }

        val data = json.optJSONObject("data")
            ?: return ArtistDetailUiState(
                isError = true,
                errorMessage = "Invalid response"
            )

        val imageUrl = data
            .optJSONArray("image")
            ?.optJSONObject(2)
            ?.optString("url")
            .orEmpty()

        return ArtistDetailUiState(
            id = data.optString("id"),
            name = data.optString("name"),
            followerCount = data.optInt("followerCount"),
            fanCount = data.optString("fanCount"),
            isVerified = data.optBoolean("isVerified"),
            imageUrl = imageUrl,
            topSongs = parseTopSongs(data),
            topAlbums = parseAlbums(data.optJSONArray("topAlbums")),
            singles = parseAlbums(data.optJSONArray("singles"))
        )
    }

    private fun parseYTMusicArtist(jsonString: String): ArtistDetailUiState {
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success")) {
            return ArtistDetailUiState(
                isError = true,
                errorMessage =
                    "Failed to load artist."
            )
        }

        val artistObject = json.optJSONObject("artist")

        val fanCount = artistObject
            ?.optString("subscribers")
            .orEmpty()

        val followerCount = fanCount
            .replace(",", "")
            .filter { it.isDigit() }
            .toIntOrNull() ?: 0

        return ArtistDetailUiState(
            id = artistObject
                ?.optString("browseId")
                .orEmpty(),
            name = artistObject
                ?.optString("name")
                .orEmpty(),
            followerCount = followerCount,
            fanCount = fanCount,
            isVerified = false,
            imageUrl = optimizeImage(
                artistObject
                    ?.optString("thumbnail")
                    .orEmpty()
            ),
            topSongs = parseYTSongs(json.optJSONArray("topSongs")),
            topAlbums = parseYTAlbums(json.optJSONArray("albums")),
            singles = parseYTAlbums(json.optJSONArray("singles"))
        )
    }

    private fun parseTopSongs(data: JSONObject): List<SongItem> {
        val array = data.optJSONArray("topSongs") ?: return emptyList()

        return buildList {
            for (i in 0 until array.length()) {
                val song = array.optJSONObject(i) ?: continue

                val albumObject = song.optJSONObject("album")

                add(
                    SongItem(
                        id = song.optString("id"),
                        name = song.optString("name"),
                        artist =
                            parsePrimaryArtists(
                                song.optJSONObject(
                                    "artists"
                                )
                            ).toMutableList(),
                        album = Album(
                            id = albumObject
                                ?.optString("id")
                                .orEmpty(),
                            name = albumObject
                                ?.optString("name")
                                .orEmpty()
                        ),
                        image =
                            parseImages(
                                song.optJSONArray(
                                    "image"
                                )
                            ).toMutableList(),
                        duration = song.optInt("duration"),
                        playCount = song.optInt("playCount"),
                        downloadUrl =
                            parseDownloads(
                                song.optJSONArray(
                                    "downloadUrl"
                                )
                            ).toMutableList(),
                        songSource = SearchSource.JIOSAAVN.toString()
                    )
                )
            }
        }
    }

    private fun parseAlbums(array: JSONArray?): List<DataItem> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue

                add(
                    DataItem(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        artist =
                            parsePrimaryArtists(
                                item.optJSONObject(
                                    "artists"
                                )
                            ).toMutableList(),
                        image =
                            parseImages(
                                item.optJSONArray(
                                    "image"
                                )
                            ).toMutableList(),
                        searchSource = SearchSource.JIOSAAVN.toString()
                    )
                )
            }
        }
    }

    private fun parseYTSongs(array: JSONArray?): List<SongItem> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue

                add(
                    SongItem(
                        id = item.optString("videoId"),
                        name = item.optString("title"),
                        image =
                            mutableListOf(
                                Image(
                                    quality = "high",
                                    url = optimizeImage(
                                        item.optString(
                                            "thumbnail"
                                        )
                                    )
                                )
                            ),
                        songSource = SearchSource.YTMUSIC.toString()
                    )
                )
            }
        }
    }

    private fun parseYTAlbums(array: JSONArray?): List<DataItem> {
        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue

                add(
                    DataItem(
                        id = item.optString("browseId"),
                        name = item.optString("title"),
                        image =
                            mutableListOf(
                                Image(
                                    quality = "high",
                                    url = optimizeImage(
                                        item.optString(
                                            "thumbnail"
                                        )
                                    )
                                )
                            ),
                        searchSource = SearchSource.YTMUSIC.toString()
                    )
                )
            }
        }
    }

    private fun parsePrimaryArtists(artistsObj: JSONObject?): List<Artists> {
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
                        image =
                            artist.optJSONArray(
                                "image"
                            )
                                ?.optJSONObject(2)
                                ?.optString("url")
                                .orEmpty(),
                        type = artist.optString("type")
                    )
                )
            }
        }
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

                add(
                    Download(
                        quality = obj.optString("quality"),
                        url = obj.optString("url")
                    )
                )
            }
        }
    }

    private fun optimizeImage(url: String): String {
        return url.replace(
            imageSizeRegex,
            "w520-h520"
        )
    }
}