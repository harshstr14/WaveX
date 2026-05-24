package com.example.wavex.homeScreen.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.repository.TopArtistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopArtistsViewModel @Inject constructor(
    private val repository: TopArtistsRepository
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var isError by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    val artists = repository.artists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshArtists()
    }

    fun refreshArtists() {
        viewModelScope.launch {
            try {
                isLoading = true
                isError = false
                errorMessage = ""

                repository.refreshArtists()
            } catch (e: Exception) {
                isError = true
                errorMessage = e.message ?: "Unknown Error"

            } finally {
                isLoading = false
            }
        }
    }
}