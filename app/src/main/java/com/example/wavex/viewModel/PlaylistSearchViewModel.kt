package com.example.wavex.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PlaylistSearchViewModel : ViewModel() {
    private val _playlists = mutableStateOf<List<DataItem>>(emptyList())
    val playlists: State<List<DataItem>> = _playlists

    fun fetchPlayListByQuery(query: String, root: String) {
        viewModelScope.launch {
            try {
                val responseBody =
                    requestWithFallback("/search/playlists?query=$query&limit=20")

                if (responseBody.isEmpty()) return@launch

                val parsed = parsePlaylistJson(responseBody, root)
                _playlists.value = parsed

            } catch (e: Exception) {
                Log.e("SAAVN", "Exception: ${e.message}")
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

            val imageUrl =
                song.optJSONArray("image")
                    ?.optJSONObject(2)
                    ?.optString("url") ?: ""

            val artistName =
                song.optJSONObject("artists")
                    ?.optJSONArray("primary")
                    ?.optJSONObject(0)
                    ?.optString("name") ?: ""

            parsedPlaylist.add(
                DataItem(id, name, artistName, imageUrl)
            )
        }

        parsedPlaylist
    }
}