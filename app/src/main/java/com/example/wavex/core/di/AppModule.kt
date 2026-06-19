package com.example.wavex.core.di

import android.content.Context
import com.example.wavex.core.database.AppDatabase
import com.example.wavex.core.database.dao.ArtistDao
import com.example.wavex.core.database.dao.DownloadedSongDao
import com.example.wavex.core.database.dao.RecentlyPlayedDao
import com.example.wavex.core.datastore.CacheManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
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

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig
    }
}