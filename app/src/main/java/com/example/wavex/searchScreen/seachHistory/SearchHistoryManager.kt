package com.example.wavex.searchScreen.seachHistory

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.searchDataStore by preferencesDataStore(name = "search_history")

class SearchHistoryManager(private val context: Context) {
    private val SEARCH_HISTORY = stringSetPreferencesKey("search_history")

    val historyFlow: Flow<List<String>> =
        context.searchDataStore.data.map { preferences ->
            preferences[SEARCH_HISTORY]
                ?.toList()
                ?.sortedDescending()
                ?: emptyList()
        }

    suspend fun addSearch(query: String) {
        if (query.isBlank()) return

        context.searchDataStore.edit { preferences ->
            val current = preferences[SEARCH_HISTORY]
                ?.toMutableList()
                ?: mutableListOf()

            current.remove(query)

            current.add(0, query)

            preferences[SEARCH_HISTORY] =
                current.take(10).toSet()
        }
    }

    suspend fun clearHistory() {
        context.searchDataStore.edit {
            it.remove(SEARCH_HISTORY)
        }
    }
}