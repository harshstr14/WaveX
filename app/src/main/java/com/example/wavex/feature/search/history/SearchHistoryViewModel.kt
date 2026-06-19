package com.example.wavex.feature.search.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchHistoryViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val manager =
        SearchHistoryManager(application)

    val recentSearches =
        manager.historyFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun saveSearch(query: String) {
        viewModelScope.launch {
            manager.addSearch(query)
        }
    }

    fun clearSearches() {
        viewModelScope.launch {
            manager.clearHistory()
        }
    }
}