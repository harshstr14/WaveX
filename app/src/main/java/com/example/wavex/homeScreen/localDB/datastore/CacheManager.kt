package com.example.wavex.homeScreen.localDB.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(
    name = "cache_prefs"
)

class CacheManager(
    private val context: Context
) {
    companion object {
        private val ARTIST_LAST_REFRESH =
            longPreferencesKey("artist_last_refresh")

        private val SONG_LAST_REFRESH =
            longPreferencesKey("song_last_refresh")
    }

    suspend fun saveArtistRefreshTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[ARTIST_LAST_REFRESH] = time
        }
    }

    suspend fun getArtistRefreshTime(): Long {
        val prefs = context.dataStore.data.first()
        return prefs[ARTIST_LAST_REFRESH] ?: 0L
    }

    suspend fun saveSongRefreshTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[SONG_LAST_REFRESH] = time
        }
    }

    suspend fun getSongRefreshTime(): Long {
        val prefs = context.dataStore.data.first()
        return prefs[SONG_LAST_REFRESH] ?: 0L
    }
}