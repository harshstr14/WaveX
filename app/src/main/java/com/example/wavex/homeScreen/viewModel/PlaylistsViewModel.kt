package com.example.wavex.homeScreen.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PlaylistsViewModel : ViewModel() {
    private val _playlists = mutableStateOf<List<DataItem>>(emptyList())
    val playlists: State<List<DataItem>> = _playlists

    var isLoading by mutableStateOf(false)
        private set

    fun fetchPlayListByQuery(query: String, root: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val responseBody = withContext(Dispatchers.IO) {
                    requestWithFallback("/search/playlists?query=$query&limit=20")
                }

                if (responseBody.isEmpty()) return@launch

                val parsed = parsePlaylistJson(responseBody, root)
                _playlists.value = parsed

            } catch (e: Exception) {
                Log.e("SAAVN", "Exception: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun parsePlaylistJson(
        jsonString: String,
        root: String
    ): List<DataItem> = withContext(Dispatchers.Default) {

        val parsedPlaylist = mutableListOf<DataItem>()

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return@withContext emptyList()

        val songsArray = json.getJSONObject("data").getJSONArray(root)

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val id = song.optString("id")
            val name = song.optString("name")

            val image = mutableListOf<Image>()
            song.optJSONArray("image")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    image.add(Image(obj.optString("quality"), obj.optString("url")))
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

            parsedPlaylist.add(
                DataItem(id, name, primaryArtists, image)
            )
        }

        parsedPlaylist
    }
}