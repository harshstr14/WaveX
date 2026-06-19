package com.example.wavex.feature.search.model

sealed class SearchArtistsUiState {
    object Idle : SearchArtistsUiState()
    object Loading : SearchArtistsUiState()
    object Empty : SearchArtistsUiState()
    data class Error(val message: String) : SearchArtistsUiState()
    object Success : SearchArtistsUiState()
}