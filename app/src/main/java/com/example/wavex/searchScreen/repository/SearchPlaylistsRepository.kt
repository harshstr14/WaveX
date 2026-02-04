package com.example.wavex.searchScreen.repository

import com.example.musify.songData.Artists
import com.example.musify.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SearchPlaylistsRepository {

    suspend fun searchPlaylists(query: String,root: String): List<DataItem> = withContext(Dispatchers.IO) {

        val response = requestWithFallback(
            "/search/playlists?query=$query&limit=20"
        )

        if (response.isEmpty()) return@withContext emptyList()

        parsePlaylists(response, root)
    }

    private fun parsePlaylists(jsonString: String,root: String): List<DataItem> {

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return emptyList()

        val playlistsArray = json
            .getJSONObject("data")
            .getJSONArray(root)

        val playlists = mutableListOf<DataItem>()

        for (i in 0 until playlistsArray.length()) {
            val playlist = playlistsArray.getJSONObject(i)

            val id = playlist.optString("id")
            val name = playlist.optString("name")

            val image = mutableListOf<Image>()
            playlist.optJSONArray("image")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    image.add(
                        Image(
                            quality = obj.optString("quality"),
                            url = obj.optString("url")
                        )
                    )
                }
            }

            val primaryArtists = mutableListOf<Artists>()
            playlist.optJSONObject("artists")
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

            playlists.add(
                DataItem(
                    id = id,
                    name = name,
                    artist = primaryArtists,
                    image = image
                )
            )
        }

        return playlists
    }
}