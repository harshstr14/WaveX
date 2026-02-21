package com.example.wavex.searchScreen.repository

import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import com.example.wavex.songData.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SearchSongsRepository {

    suspend fun searchSongs(query: String,root: String): List<SongItem> = withContext(Dispatchers.IO) {

        val response = requestWithFallback(
            "/search/songs?query=$query&limit=30"
        )

        if (response.isEmpty()) return@withContext emptyList()

        parseSongs(response, root)
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
}