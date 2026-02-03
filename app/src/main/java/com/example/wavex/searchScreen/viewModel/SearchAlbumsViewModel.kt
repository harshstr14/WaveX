package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musify.songData.Artists
import com.example.musify.songData.Image
import com.example.wavex.requestWithFallback
import com.example.wavex.homeScreen.DataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.cancellation.CancellationException

class SearchAlbumsViewModel : ViewModel() {
    private val _albums = mutableStateOf<List<DataItem>>(emptyList())
    val albums: State<List<DataItem>> = _albums
    val isLoading = mutableStateOf(false)
    private var searchJob: Job? = null

    fun fetchAlbumByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            _albums.value = emptyList()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            isLoading.value = true
            try {
                val responseBody = withContext(Dispatchers.IO) {
                    requestWithFallback("/search/albums?query=$query&limit=30")
                }

                if (responseBody.isEmpty()) return@launch

                _albums.value = parseAlbumListJson(responseBody, root)

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
        _albums.value = emptyList()
        isLoading.value = false
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

            val id = album.optString("id")
            val name = album.optString("name")

            val image = mutableListOf<Image>()
            album.optJSONArray("image")?.let { arr ->
                for (j in 0 until arr.length()) {
                    val obj = arr.getJSONObject(j)
                    image.add(Image(obj.optString("quality"), obj.optString("url")))
                }
            }

            val primaryArtists = mutableListOf<Artists>()
            album.optJSONObject("artists")
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

            parsedAlbums.add(
                DataItem(
                    id = id,
                    name = name,
                    artist = primaryArtists,
                    image = image
                )
            )
        }

        parsedAlbums
    }
}