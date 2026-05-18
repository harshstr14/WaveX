package com.example.wavex.playlistScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class PlaylistViewModel(
    private val repository: PlaylistRepository =
        PlaylistRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "PlaylistViewModel"
        const val BASE_URL = BuildConfig.YT_API_BASE_URL
    }

    private val _playlists =
        MutableStateFlow(
            PlaylistDetailUiState()
        )

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