package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.searchScreen.repository.SearchSongsRepository
import com.example.wavex.searchScreen.uiState.SearchSongsUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchSongsViewModel(
    private val repository: SearchSongsRepository = SearchSongsRepository()
) : ViewModel() {

    private val _songs = MutableStateFlow<List<SongItem>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _uiState =
        MutableStateFlow<SearchSongsUiState>(SearchSongsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun fetchSongByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            _uiState.value = SearchSongsUiState.Idle
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _uiState.value = SearchSongsUiState.Loading
            try {
                val result = repository.searchSongs(query, root)
                _songs.value = result

                _uiState.value =
                    if (result.isEmpty()) SearchSongsUiState.Empty
                    else SearchSongsUiState.Success

            } catch (_: CancellationException) {
            } catch (e: Exception) {
                _uiState.value =
                    SearchSongsUiState.Error("Something went wrong")
                Log.e("SAAVN", "Artist search failed", e)
            }
        }
    }

    suspend fun fetchSuggestionSongs(songId: String): List<SongItem> {
        return try {
            repository.fetchSuggestionSongs(songId)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clearResults() {
        _songs.value = emptyList()
    }

    fun setIdle() {
        _uiState.value = SearchSongsUiState.Idle
    }
}