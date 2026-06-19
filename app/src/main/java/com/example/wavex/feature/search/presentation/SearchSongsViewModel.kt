package com.example.wavex.feature.search.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.BuildConfig
import com.example.wavex.core.model.SongItem
import com.example.wavex.feature.search.data.SearchSongsRepository
import com.example.wavex.feature.search.model.SearchSongsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchSongsViewModel(
    private val repository: SearchSongsRepository = SearchSongsRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "SearchSongsViewModel"
        const val BASE_URL = BuildConfig.YT_API_BASE_URL
    }

    private val _songs =MutableStateFlow<List<SongItem>>(emptyList())

    val songs = _songs.asStateFlow()

    private val _uiState =
        MutableStateFlow<SearchSongsUiState>(
            SearchSongsUiState.Idle
        )

    val uiState = _uiState.asStateFlow()

    fun fetchSongByQuery(query: String) {
        if (!validateQuery(query)) {
            return
        }

        launchSearch(
            tag = "SAAVN"
        ) {
            repository.searchSongs(query)
        }
    }

    fun fetchYTMusicSongs(query: String) {
        if (!validateQuery(query)) {
            return
        }

        launchSearch(
            tag = "YT_MUSIC"
        ) {
            repository.ytMusicSearch(
                query = query,
                baseUrl = BASE_URL
            )
        }
    }

    suspend fun fetchYTStreamData(
        songId: String,
        baseUrl: String
    ) = repository.fetchYTStreamData(songId, baseUrl)

    private fun launchSearch(tag: String,block: suspend () -> List<SongItem>) {
        viewModelScope.launch {
            _uiState.value = SearchSongsUiState.Loading

            try {
                val result = block()
                _songs.value = result

                _uiState.value =
                    if (result.isEmpty()) {
                        SearchSongsUiState.Empty
                    } else {
                        SearchSongsUiState.Success
                    }

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _uiState.value =
                    SearchSongsUiState.Error(
                        "Something went wrong"
                    )

                Log.e(tag, "Search failed", e)
            }
        }
    }

    private fun validateQuery(query: String): Boolean {
        if (query.length < 2) {
            clearResults()

            _uiState.value = SearchSongsUiState.Idle

            return false
        }
        return true
    }

    fun clearResults() {
        _songs.value = emptyList()
    }

    fun setIdle() {
        _uiState.value = SearchSongsUiState.Idle
    }
}