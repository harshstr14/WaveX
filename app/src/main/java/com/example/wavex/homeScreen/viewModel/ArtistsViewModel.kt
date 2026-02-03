package com.example.wavex.homeScreen.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musify.songData.Artists
import com.example.wavex.requestWithFallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ArtistsViewModel : ViewModel() {
    private val _artists = mutableStateOf<List<Artists>>(emptyList())
    val artists: State<List<Artists>> = _artists

    fun fetchArtistsByQuery(query: String, root: String) {
        viewModelScope.launch {
            try {
                val responseBody = withContext(Dispatchers.IO) {
                    requestWithFallback("/search/artists?query=$query&limit=20")
                }

                if (responseBody.isEmpty()) return@launch

                _artists.value = parseArtistsJson(responseBody, root)

            } catch (e: Exception) {
                Log.e("SAAVN", "Exception: ${e.message}")
            }
        }
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