package com.example.wavex.homeScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.localDB.entity.RecentlyPlayedEntity
import com.example.wavex.homeScreen.repository.RecentlyPlayedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentlyPlayedViewModel @Inject constructor(
    private val repository: RecentlyPlayedRepository
) : ViewModel() {

    val recentlyPlayed = repository.getRecentlyPlayed()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun onSongPlayed(song: RecentlyPlayedEntity) {
        viewModelScope.launch {
            repository.addSong(song)
        }
    }

    fun clearRecentlyPlayed() {
        viewModelScope.launch {
            repository.clearRecentlyPlayed()
        }
    }

    suspend fun getPlayableSong(
        song: SongItem
    ): SongItem {
        return repository.getPlayableSong(song)
    }
}