package com.example.wavex.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wavex.core.database.dao.ArtistDao
import com.example.wavex.core.database.dao.DownloadedSongDao
import com.example.wavex.core.database.dao.RecentlyPlayedDao
import com.example.wavex.core.database.dao.SongDao
import com.example.wavex.core.database.entity.ArtistEntity
import com.example.wavex.core.database.entity.DownloadedSongEntity
import com.example.wavex.core.database.entity.RecentlyPlayedEntity
import com.example.wavex.core.database.entity.SongEntity

@Database(
    entities = [
        ArtistEntity::class,
        SongEntity::class,
        RecentlyPlayedEntity::class,
        DownloadedSongEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao

    abstract fun songDao(): SongDao

    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun downloadedSongDao(): DownloadedSongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cache_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}