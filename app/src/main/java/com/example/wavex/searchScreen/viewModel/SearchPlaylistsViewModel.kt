package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.searchScreen.repository.SearchPlaylistsRepository
import com.example.wavex.searchScreen.uiState.SearchPlaylistUiState
import com.example.wavex.searchScreen.viewModel.SearchSongsViewModel.Companion.BASE_URL
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchPlaylistsViewModel(
    private val repository: SearchPlaylistsRepository = SearchPlaylistsRepository()
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<DataItem>>(emptyList())
    val playlists = _playlists.asStateFlow()

    private val _ytMusicPlaylists = MutableStateFlow<List<DataItem>>(emptyList())
    val ytMusicPlaylists = _ytMusicPlaylists.asStateFlow()

    private val _uiState =
        MutableStateFlow<SearchPlaylistUiState>(SearchPlaylistUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun fetchPlayListByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            _uiState.value = SearchPlaylistUiState.Idle
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _uiState.value = SearchPlaylistUiState.Loading
            try {
                val result = repository.searchPlaylists(query, root)
                _playlists.value = result

                _uiState.value =
                    if (result.isEmpty()) SearchPlaylistUiState.Empty
                    else SearchPlaylistUiState.Success

            } catch (_: CancellationException) {
            } catch (e: Exception) {
                _uiState.value =
                    SearchPlaylistUiState.Error("Something went wrong")
                Log.e("SAAVN", "Artist search failed", e)
            }
        }
    }

    fun fetchYTMusicPlaylists(query: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            _uiState.value = SearchPlaylistUiState.Idle
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _uiState.value = SearchPlaylistUiState.Loading

            try {
                val result = repository.ytMusicSearch(
                    query = query,
                    baseUrl = BASE_URL
                )

                _ytMusicPlaylists.value = result

                _uiState.value =
                    if (result.isEmpty())
                        SearchPlaylistUiState.Empty
                    else
                        SearchPlaylistUiState.Success

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _uiState.value =
                    SearchPlaylistUiState.Error("Something went wrong")

                Log.e("YT_MUSIC", "YT Music search failed", e)
            }
        }
    }

    fun clearResults() {
        _playlists.value = emptyList()
    }

    fun setIdle() {
        _uiState.value = SearchPlaylistUiState.Idle
    }
}