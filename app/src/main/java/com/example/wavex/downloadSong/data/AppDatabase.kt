package com.example.wavex.downloadSong.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wavex.downloadSong.data.DownloadDao
import com.example.wavex.downloadSong.data.DownloadedSong

@Database(
    entities = [DownloadedSong::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadDao(): DownloadDao
}