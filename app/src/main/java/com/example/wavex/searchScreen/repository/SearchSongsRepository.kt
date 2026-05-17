package com.example.wavex.searchScreen.repository

import com.example.wavex.HttpClientProvider
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import com.example.wavex.songData.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class SearchSongsRepository {

    suspend fun searchSongs(query: String,root: String): List<SongItem> = withContext(Dispatchers.IO) {
        val response = requestWithFallback(
            "/search/songs?query=$query&limit=30"
        )

        if (response.isEmpty()) return@withContext emptyList()

        parseSongs(response, root)
    }

    suspend fun fetchSuggestionSongs(songId: String): List<SongItem> = withContext(Dispatchers.IO) {
        val response = requestWithFallback(
            "/songs/$songId/suggestions?&limit=30"
        )

        if (response.isEmpty()) return@withContext emptyList()

        parseSuggestionSongs(response)
    }

    private fun parseSongs(jsonString: String,root: String): List<SongItem> {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return emptyList()

        val songsArray = json
            .getJSONObject("data")
            .getJSONArray(root)

        val songs = mutableListOf<SongItem>()

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val id = song.optString("id")
            val name = song.optString("name")
            val duration = song.optInt("duration")
            val playCount = song.optInt("playCount")

            val images = mutableListOf<Image>()
            song.optJSONArray("image")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    images.add(
                        Image(
                            quality = obj.optString("quality"),
                            url = obj.optString("url")
                        )
                    )
                }
            }

            val downloads = mutableListOf<Download>()
            song.optJSONArray("downloadUrl")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    downloads.add(
                        Download(
                            quality = obj.optString("quality"),
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

            songs.add(
                SongItem(
                    id = id,
                    name = name,
                    artist = primaryArtists,
                    album = album,
                    image = images,
                    duration = duration,
                    playCount = playCount,
                    downloadUrl = downloads
                )
            )
        }

        return songs
    }

    private fun parseSuggestionSongs(jsonString: String): List<SongItem> {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return emptyList()

        val songsArray = json.getJSONArray("data")

        val songs = mutableListOf<SongItem>()

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val id = song.optString("id")
            val name = song.optString("name")
            val duration = song.optInt("duration")
            val playCount = song.optInt("playCount")

            val images = mutableListOf<Image>()
            song.optJSONArray("image")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    images.add(
                        Image(
                            quality = obj.optString("quality"),
                            url = obj.optString("url")
                        )
                    )
                }
            }

            val downloads = mutableListOf<Download>()
            song.optJSONArray("downloadUrl")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    downloads.add(
                        Download(
                            quality = obj.optString("quality"),
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

            songs.add(
                SongItem(
                    id = id,
                    name = name,
                    artist = primaryArtists,
                    album = album,
                    image = images,
                    duration = duration,
                    playCount = playCount,
                    downloadUrl = downloads,
                    searchSource = "jiosaavn"
                )
            )
        }

        return songs
    }

    suspend fun ytMusicSearch(query: String, baseUrl: String): List<SongItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/search?q=$query&filter=songs")
                .get()
                .build()

            val call =  HttpClientProvider.client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            val response = call.execute()

            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            val body = response.body?.string().orEmpty()

            if (body.isEmpty()) {
                return@withContext emptyList()
            }

            parseYTMusicSearch(body)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseYTMusicSearch(jsonString: String): List<SongItem> {
        val json = JSONObject(jsonString)

        val resultsArray = json.optJSONArray("results")
            ?: return emptyList()

        val results = mutableListOf<SongItem>()

        for (i in 0 until resultsArray.length()) {
            val item = resultsArray.getJSONObject(i)
            val videoId = item.optString("videoId")

            val thumbnails = mutableListOf<Image>()

            item.optJSONArray("thumbnails")?.let { array ->
                for (j in 0 until array.length()) {
                    val thumb = array.getJSONObject(j)

                    val imageUrl = thumb
                        .optString("url")
                        .replace(Regex("w\\d+-h\\d+"), "w520-h520")

                    thumbnails.add(
                        Image(
                            quality = "",
                            url = imageUrl
                        )
                    )
                }
            }

            val artists = mutableListOf<Artists>()

            item.optJSONArray("artists")?.let { array ->
                for (j in 0 until array.length()) {
                    val artist = array.getJSONObject(j)

                    artists.add(
                        Artists(
                            id = artist.optString("id"),
                            name = artist.optString("name")
                        )
                    )
                }
            }

            results.add(
                SongItem(
                    id = videoId,
                    name = item.optString("title"),
                    artist = artists,
                    image = thumbnails,
                    searchSource = "ytmusic"
                )
            )
        }

        return results
    }
}