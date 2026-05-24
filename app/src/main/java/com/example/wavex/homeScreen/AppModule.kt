package com.example.wavex.homeScreen

import android.content.Context
import com.example.wavex.homeScreen.localDB.dao.ArtistDao
import com.example.wavex.homeScreen.localDB.dao.DownloadedSongDao
import com.example.wavex.homeScreen.localDB.dao.RecentlyPlayedDao
import com.example.wavex.homeScreen.localDB.datastore.CacheManager
import com.example.wavex.homeScreen.localDB.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideDownloadedSongDao(db: AppDatabase) : DownloadedSongDao {
        return db.downloadedSongDao()
    }

    @Provides
    fun provideRecentlyPlayedDao(db: AppDatabase): RecentlyPlayedDao {
        return db.recentlyPlayedDao()
    }

    @Provides
    fun provideArtistDao(db: AppDatabase): ArtistDao {
        return db.artistDao()
    }

    @Provides
    @Singleton
    fun provideCacheManager(
        @ApplicationContext context: Context
    ): CacheManager {
        return CacheManager(context)
    }
}