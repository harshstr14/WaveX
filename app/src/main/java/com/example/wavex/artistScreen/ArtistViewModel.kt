package com.example.wavex.artistScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ArtistViewModel(
    private val repository: ArtistRepository =
        ArtistRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "ArtistViewModel"
        const val BASE_URL = BuildConfig.YT_API_BASE_URL
    }

    private val _artists = MutableStateFlow(ArtistDetailUiState())

    val artists = _artists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    val isLoading = _isLoading.asStateFlow()

    private var loadJob: Job? = null

    fun loadArtist(artistId: String) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _isLoading.value = true

            try {
                _artists.value =
                    repository.fetchArtistById(
                        artistId
                    )

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _artists.value =
                    ArtistDetailUiState(
                        isError = true,
                        errorMessage =
                            "Something went wrong"
                    )

                Log.e(TAG,"Artist load failed",e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadYTArtist(artistId: String) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _isLoading.value = true

            try {
                _artists.value =
                    repository.fetchYTMusicArtist(
                        artistId = artistId,
                        baseUrl = BASE_URL
                    )

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _artists.value =
                    ArtistDetailUiState(
                        isError = true,
                        errorMessage =
                            "Something went wrong"
                    )

                Log.e(TAG,"YT Artist load failed",e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}