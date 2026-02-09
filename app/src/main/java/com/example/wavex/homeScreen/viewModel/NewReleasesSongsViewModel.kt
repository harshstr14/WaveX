package com.example.wavex.homeScreen.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image

class NewReleasesSongsViewModel : ViewModel() {
    private val _songs = mutableStateOf<List<SongItem>>(emptyList())
    val songs: State<List<SongItem>> = _songs

    var isLoading by mutableStateOf(false)
        private set
    fun fetchPlaylistsByID(playListId: String, root: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val responseBody = withContext(Dispatchers.IO) {
                    requestWithFallback("/playlists?id=$playListId&limit=40")
                }

                if (responseBody.isEmpty()) return@launch

                val parsed = parseNewSongsJson(responseBody, root)
                _songs.value = parsed

            } catch (e: Exception) {
                Log.e("SAAVN", "Exception: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun parseNewSongsJson(jsonString: String, root: String):
            List<SongItem> = withContext(Dispatchers.Default) {

        val parsedSongs = mutableListOf<SongItem>()
        val json = JSONObject(jsonString)

        if (!json.optBoolean("success", false)) return@withContext emptyList()

        val songsArray = json.getJSONObject("data").getJSONArray(root)

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val id = song.optString("id")
            val name = song.optString("name")
            val duration = song.optInt("duration")

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
                    download.add(Download(obj.optString("quality"), obj.optString("url")))
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

            parsedSongs.add(
                SongItem(id, name, primaryArtists, image, duration, download)
            )
        }

        parsedSongs
    }
}