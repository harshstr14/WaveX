package com.example.wavex.feature.profile.presentation.downloads.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.core.database.entity.DownloadedSongEntity
import com.example.wavex.feature.profile.presentation.downloads.data.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {
    val downloadedSongs = repository.allDownloads

    val downloadedSongIds = repository.allDownloads
        .map { songs ->
            songs.map { it.id }.toSet()
        }

    fun insertSong(song: DownloadedSongEntity) {
        viewModelScope.launch {
            repository.insert(song)
        }
    }

    fun deleteSong(
        id: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val song = repository.getSongById(id)

                if (song != null) {
                    val file = File(song.localPath)

                    val isDeleted = if (file.exists()) {
                        file.delete()
                    } else true

                    repository.delete(id)

                    onResult(true, "Removed from downloads")
                } else {
                    onResult(false, "Song not found")
                }

            } catch (_: Exception) {
                onResult(false, "Failed to remove song")
            }
        }
    }

    fun deleteAllSongs() {
        viewModelScope.launch {
            val songs = downloadedSongs.first()

            songs.forEach { song ->
                song.localPath.let { path ->
                    val file = File(path)

                    if (file.exists()) {
                        file.delete()
                    }
                }
            }

            repository.deleteAll()
        }
    }
}