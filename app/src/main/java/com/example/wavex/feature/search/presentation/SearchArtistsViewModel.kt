package com.example.wavex.feature.search.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.BuildConfig
import com.example.wavex.feature.search.data.SearchArtistsRepository
import com.example.wavex.feature.search.model.SearchArtistsUiState
import com.example.wavex.core.model.Artists
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchArtistsViewModel(
    private val repository: SearchArtistsRepository = SearchArtistsRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "SearchArtistsViewModel"
        const val BASE_URL = BuildConfig.YT_API_BASE_URL
    }

    private val _artists = MutableStateFlow<List<Artists>>(emptyList())

    val artists = _artists.asStateFlow()

    private val _uiState =
        MutableStateFlow<SearchArtistsUiState>(
            SearchArtistsUiState.Idle
        )

    val uiState = _uiState.asStateFlow()

    fun fetchArtistsByQuery(query: String) {
        if (!validateQuery(query)) {
            return
        }

        launchSearch(
            tag = "SAAVN"
        ) {
            repository.searchArtists(query = query)
        }
    }

    fun fetchYTMusicArtists(query: String) {
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

    private fun launchSearch(tag: String,block: suspend () -> List<Artists>) {
        viewModelScope.launch {
            _uiState.value = SearchArtistsUiState.Loading

            try {
                val result = block()
                _artists.value = result

                _uiState.value =
                    if (result.isEmpty()) {
                        SearchArtistsUiState.Empty
                    } else {
                        SearchArtistsUiState.Success
                    }

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _uiState.value =
                    SearchArtistsUiState.Error(
                        "Something went wrong"
                    )

                Log.e(tag, "Artist search failed", e)
            }
        }
    }

    private fun validateQuery(query: String): Boolean {
        if (query.length < 2) {
            clearResults()

            _uiState.value = SearchArtistsUiState.Idle

            return false
        }

        return true
    }

    fun clearResults() {
        _artists.value = emptyList()
    }

    fun setIdle() {
        _uiState.value =
            SearchArtistsUiState.Idle
    }
}