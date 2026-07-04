package com.example.wavex.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.BuildConfig
import com.example.wavex.core.model.DataItem
import com.example.wavex.feature.search.data.SearchPlaylistsRepository
import com.example.wavex.feature.search.model.SearchPlaylistUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

class SearchPlaylistsViewModel(
    private val repository: SearchPlaylistsRepository =
        SearchPlaylistsRepository()
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<DataItem>>(emptyList())

    val playlists = _playlists.asStateFlow()

    private val _uiState =
        MutableStateFlow<SearchPlaylistUiState>(
            SearchPlaylistUiState.Idle
        )

    val uiState = _uiState.asStateFlow()

    companion object {
        private const val TAG = "SearchPlaylistsViewModel"
        const val BASE_URL = BuildConfig.YT_API_BASE_URL
    }

    fun fetchPlayListByQuery(query: String) {
        if (!validateQuery(query)) {
            return
        }

        launchSearch(
            tag = "SAAVN"
        ) {
            repository.searchPlaylists(query = query)
        }
    }

    fun fetchYTMusicPlaylists(query: String) {
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

    private fun launchSearch(tag: String,block: suspend () -> List<DataItem>) {
        viewModelScope.launch {
            _uiState.value = SearchPlaylistUiState.Loading

            try {
                val result = block()

                _playlists.value = result

                _uiState.value =
                    if (result.isEmpty()) {
                        SearchPlaylistUiState.Empty
                    } else {
                        SearchPlaylistUiState.Success
                    }
            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _uiState.value =
                    SearchPlaylistUiState.Error(
                        "Something went wrong"
                    )

                Timber.tag(tag).e(e, "Search failed")
            }
        }
    }

    private fun validateQuery(query: String): Boolean {
        if (query.length < 2) {
            clearResults()

            _uiState.value = SearchPlaylistUiState.Idle

            return false
        }
        return true
    }

    fun clearResults() {
        _playlists.value = emptyList()
    }

    fun setIdle() {
        _uiState.value =
            SearchPlaylistUiState.Idle
    }
}