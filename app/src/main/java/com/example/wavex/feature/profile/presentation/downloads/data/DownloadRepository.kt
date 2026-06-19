package com.example.wavex.feature.profile.presentation.downloads.data

import com.example.wavex.core.database.dao.DownloadedSongDao
import com.example.wavex.core.database.entity.DownloadedSongEntity
import javax.inject.Inject

class DownloadRepository @Inject constructor(
    private val dao: DownloadedSongDao
) {
    val allDownloads = dao.getAllSongs()

    suspend fun insert(song: DownloadedSongEntity) {
        dao.insertSong(song)
    }

    suspend fun delete(id: String) {
        dao.deleteSong(id)
    }

    suspend fun deleteAll() {
        dao.deleteAllSongs()
    }

    suspend fun getSongById(id: String): DownloadedSongEntity? {
        return dao.getSongById(id)
    }
}