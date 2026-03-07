package com.example.wavex.downloadSong.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: DownloadedSong)

    @Query("SELECT * FROM downloaded_songs")
    fun getAllSongs(): Flow<List<DownloadedSong>>

    @Query("DELETE FROM downloaded_songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE id = :id)")
    fun isDownloaded(id: String): Flow<Boolean>
}