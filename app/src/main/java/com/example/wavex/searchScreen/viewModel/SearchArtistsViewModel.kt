package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musify.songData.Artists
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.cancellation.CancellationException

class SearchArtistsViewModel : ViewModel() {
    private val _artists = mutableStateOf<List<Artists>>(emptyList())
    val artists: State<List<Artists>> = _artists
    val isLoading = mutableStateOf(false)
    private var searchJob: Job? = null

    fun fetchArtistsByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            _artists.value = emptyList()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            isLoading.value = true
            try {
                val responseBody = withContext(Dispatchers.IO) {
                    requestWithFallback("/search/artists?query=$query&limit=20")
                }

                if (responseBody.isEmpty()) return@launch

                _artists.value = parseArtistsJson(responseBody, root)

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
        _artists.value = emptyList()
        isLoading.value = false
    }

    private suspend fun parseArtistsJson(
        jsonString: String,
        root: String
    ): List<Artists> = withContext(Dispatchers.Default) {

        val parsedArtists = mutableListOf<Artists>()

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) return@withContext emptyList()

        val resultArray =
            json.getJSONObject("data").getJSONArray(root)

        for (i in 0 until resultArray.length()) {
            val artist = resultArray.getJSONObject(i)

            parsedArtists.add(
                Artists(
                    id = artist.optString("id"),
                    name = artist.optString("name"),
                    role = artist.optString("role"),
                    image =
                        artist.optJSONArray("image")
                            ?.optJSONObject(2)
                            ?.optString("url") ?: "",
                    type = artist.optString("type")
                )
            )
        }

        parsedArtists
    }
}