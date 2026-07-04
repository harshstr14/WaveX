package com.example.wavex.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import com.cloudinary.android.MediaManager
import com.example.wavex.BuildConfig
import com.example.wavex.core.service.MusicPlayerService
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WaveXApplication : Application() {
    companion object {
        const val MUSIC_CHANNEL_ID = "music_player_channel"
        const val FCM_CHANNEL_ID = "fcm_channel"
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        createMusicChannel()
        createFCMChannel()

        val intent = Intent(this, MusicPlayerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        val config = HashMap<String, String>()
        config["cloud_name"] = BuildConfig.CLOUD_NAME
        config["api_key"] = BuildConfig.API_KEY
        config["api_secret"] = BuildConfig.API_SECRET
        MediaManager.init(this,config)
    }

    private fun createMusicChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                MUSIC_CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun createFCMChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                "WaveX Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }
}