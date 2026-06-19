package com.example.wavex.feature.search.model

sealed class SearchAlbumsUiState {
    object Idle : SearchAlbumsUiState()
    object Loading : SearchAlbumsUiState()
    object Empty : SearchAlbumsUiState()
    data class Error(val message: String) : SearchAlbumsUiState()
    object Success : SearchAlbumsUiState()
}