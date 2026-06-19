package com.example.wavex.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.home.data.HomeRepository
import com.example.wavex.feature.home.model.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeLocalData()
        loadHomeData()
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            combine(
                repository.getTopArtists(),
                repository.getRecentlyPlayed()
            ) { artists, recentSongs ->
                artists to recentSongs
            }.collect { (artists, recentSongs) ->
                _uiState.update {
                    it.copy(
                        topArtists = artists,
                        recentlyPlayed = recentSongs
                    )
                }
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }

    fun clearRecentlyPlayed() {
        viewModelScope.launch {
            repository.clearRecentlyPlayed()
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            repository.refreshTopArtists()

            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val playlists = async {
                    repository.getPlaylists()
                }

                val albums = async {
                    repository.getAlbums()
                }

                val trending = async {
                    repository.getTrendingSongs()
                }

                val newReleases = async {
                    repository.getNewReleases()
                }

                _uiState.update {
                    it.copy(
                        playlists = playlists.await(),
                        topAlbums = albums.await(),
                        trendingSongs = trending.await(),
                        newReleases = newReleases.await(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }
}