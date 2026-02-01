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

class AlbumSearchViewModel : ViewModel() {
    private val _albums = mutableStateOf<List<DataItem>>(emptyList())
    val albums: State<List<DataItem>> = _albums

    fun fetchAlbumByQuery(query: String, root: String) {
        viewModelScope.launch {
            try {
                val responseBody =
                    requestWithFallback("/search/albums?query=$query&limit=30")

                if (responseBody.isEmpty()) return@launch

                _albums.value = parseAlbumListJson(responseBody, root)

            } catch (e: Exception) {
                Log.e("SAAVN", "Exception: ${e.message}")
            }
        }
    }

    private suspend fun parseAlbumListJson(
        jsonString: String,
        root: String
    ): List<DataItem> = withContext(Dispatchers.Default) {

        val parsedAlbums = mutableListOf<DataItem>()

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return@withContext emptyList()

        val albumsArray =
            json.getJSONObject("data").getJSONArray(root)

        for (i in 0 until albumsArray.length()) {
            val album = albumsArray.getJSONObject(i)

            parsedAlbums.add(
                DataItem(
                    id = album.optString("id"),
                    name = album.optString("name"),
                    artist =
                        album.optJSONObject("artists")
                            ?.optJSONArray("primary")
                            ?.optJSONObject(0)
                            ?.optString("name") ?: "",
                    image =
                        album.optJSONArray("image")
                            ?.optJSONObject(2)
                            ?.optString("url") ?: ""
                )
            )
        }

        parsedAlbums
    }
}