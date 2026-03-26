package com.example.wavex.downloadSong.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.downloadSong.data.DownloadedSong
import com.example.wavex.downloadSong.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class DownloadViewModel(
    private val repository: DownloadRepository
) : ViewModel() {

    val downloadedSongs = repository.allDownloads

    val downloadedSongIds = repository.allDownloads
        .map { songs ->
            songs.map { it.id }.toSet()
        }

    fun insertSong(song: DownloadedSong) {
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

            } catch (e: Exception) {
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

    fun isDownloaded(id: String): Flow<Boolean> {
        return repository.isDownloaded(id)
    }
}