package com.example.wavex.downloadSong.repository

import com.example.wavex.downloadSong.data.DownloadDao
import com.example.wavex.downloadSong.data.DownloadedSong
import kotlinx.coroutines.flow.Flow

class DownloadRepository(
    private val dao: DownloadDao
) {
    val allDownloads = dao.getAllSongs()

    suspend fun insert(song: DownloadedSong) {
        dao.insertSong(song)
    }

    suspend fun delete(id: String) {
        dao.deleteSong(id)
    }

    suspend fun deleteAll() {
        dao.deleteAllSongs()
    }

    suspend fun getSongById(id: String): DownloadedSong? {
        return dao.getSongById(id)
    }
}