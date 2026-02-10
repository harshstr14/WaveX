package com.example.wavex.searchScreen.uiState

sealed class SearchPlaylistUiState {
    object Idle : SearchPlaylistUiState()
    object Loading : SearchPlaylistUiState()
    object Empty : SearchPlaylistUiState()
    data class Error(val message: String) : SearchPlaylistUiState()
    object Success : SearchPlaylistUiState()
}