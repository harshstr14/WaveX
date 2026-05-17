package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.searchScreen.repository.SearchArtistsRepository
import com.example.wavex.searchScreen.uiState.SearchArtistsUiState
import com.example.wavex.searchScreen.viewModel.SearchSongsViewModel.Companion.BASE_URL
import com.example.wavex.songData.Artists
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchArtistsViewModel(
    private val repository: SearchArtistsRepository = SearchArtistsRepository()
) : ViewModel() {

    private val _artists = MutableStateFlow<List<Artists>>(emptyList())
    val artists = _artists.asStateFlow()

    private val _ytMusicArtists = MutableStateFlow<List<Artists>>(emptyList())
    val ytMusicArtists = _ytMusicArtists.asStateFlow()

    private val _uiState =
        MutableStateFlow<SearchArtistsUiState>(SearchArtistsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun fetchArtistsByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            _uiState.value = SearchArtistsUiState.Idle
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _uiState.value = SearchArtistsUiState.Loading
            try {
                val result = repository.searchArtists(query, root)
                _artists.value = result

                _uiState.value =
                    if (result.isEmpty()) SearchArtistsUiState.Empty
                    else SearchArtistsUiState.Success

            } catch (_: CancellationException) {
            } catch (e: Exception) {
                _uiState.value =
                    SearchArtistsUiState.Error("Something went wrong")
                Log.e("SAAVN", "Artist search failed", e)
            }
        }
    }

    fun fetchYTMusicArtists(query: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            _uiState.value = SearchArtistsUiState.Idle
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _uiState.value = SearchArtistsUiState.Loading
            try {
                val result = repository.ytMusicSearch(
                    query = query,
                    baseUrl = BASE_URL
                )
                _ytMusicArtists.value = result

                _uiState.value =
                    if (result.isEmpty()) SearchArtistsUiState.Empty
                    else SearchArtistsUiState.Success

            } catch (_: CancellationException) {
            } catch (e: Exception) {
                _uiState.value =
                    SearchArtistsUiState.Error("Something went wrong")
                Log.e("SAAVN", "Artist search failed", e)
            }
        }
    }


    fun clearResults() {
        _artists.value = emptyList()
    }

    fun setIdle() {
        _uiState.value = SearchArtistsUiState.Idle
    }
}