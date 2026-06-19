package com.example.wavex.feature.playlist.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.BuildConfig
import com.example.wavex.feature.playlist.data.PlaylistRepository
import com.example.wavex.feature.playlist.model.PlaylistDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val repository: PlaylistRepository
) : ViewModel() {
    companion object {
        private const val TAG = "PlaylistViewModel"
        const val BASE_URL = BuildConfig.YT_API_BASE_URL
    }

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite = _isFavourite.asStateFlow()

    fun checkFavourite(
        playlistId: String
    ) {
        viewModelScope.launch {
            try {
                _isFavourite.value = repository.isFavouritePlaylist(playlistId)
            } catch (_: Exception) {

            }
        }
    }

    fun toggleFavourite(
        playlistId: String,
        playlistName: String,
        imageUrl: String,
        playlistSource: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (_isFavourite.value) {
                    repository.removeFromFavourite(playlistId)

                    _isFavourite.value = false

                    onResult("Removed From Favourite")
                } else {
                    repository.addToFavourite(
                        playlistId = playlistId,
                        playlistName = playlistName,
                        imageUrl = imageUrl,
                        playlistSource = playlistSource
                    )

                    _isFavourite.value = true

                    onResult("Added To Favourite")
                }
            } catch (e: Exception) {
                onResult(e.message ?: "Something went wrong")
            }
        }
    }

    private val _playlists = MutableStateFlow(PlaylistDetailUiState())

    val playlists = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    val isLoading = _isLoading.asStateFlow()

    private var loadJob: Job? = null

    fun loadPlaylist(playlistId: String) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _isLoading.value = true

            try {
                _playlists.value =
                    repository.fetchPlaylistById(
                        playlistId
                    )

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _playlists.value =
                    PlaylistDetailUiState(
                        isError = true,
                        errorMessage = "Something went wrong"
                    )

                Log.e(TAG,"Playlist load failed",e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadYTPlaylist(playlistId: String) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _isLoading.value = true

            try {
                _playlists.value =
                    repository.fetchYTMusicPlaylist(
                        playlistId = playlistId,
                        baseUrl = BASE_URL
                    )

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _playlists.value =
                    PlaylistDetailUiState(
                        isError = true,
                        errorMessage = "Something went wrong"
                    )

                Log.e(TAG,"YT Playlist load failed",e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}