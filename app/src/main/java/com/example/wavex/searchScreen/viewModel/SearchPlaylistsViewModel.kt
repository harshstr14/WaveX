package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musify.songData.Artists
import com.example.musify.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.cancellation.CancellationException

class SearchPlaylistsViewModel : ViewModel() {
    private val _playlists = mutableStateOf<List<DataItem>>(emptyList())
    val playlists: State<List<DataItem>> = _playlists
    val isLoading = mutableStateOf(false)

    private var searchJob: Job? = null

    fun fetchPlayListByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            _playlists.value = emptyList()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            isLoading.value = true
            try {
                val responseBody = withContext(Dispatchers.IO) {
                    requestWithFallback("/search/playlists?query=$query&limit=20")
                }

                if (responseBody.isEmpty()) return@launch

                val parsed = parsePlaylistJson(responseBody, root)
                _playlists.value = parsed

            } catch (_: CancellationException) {
                // normal cancellation — ignore
            } catch (e: Exception) {
                Log.e("SAAVN", "Exception: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _playlists.value = emptyList()
        isLoading.value = false
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