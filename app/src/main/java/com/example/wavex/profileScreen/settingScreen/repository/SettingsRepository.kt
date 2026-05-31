package com.example.wavex.profileScreen.settingScreen.repository

import com.example.wavex.profileScreen.settingScreen.AudioStreamQualityPreference
import com.example.wavex.profileScreen.settingScreen.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: SettingsDataStore
) {
    val streamQualityFlow: Flow<AudioStreamQualityPreference> =
        dataStore.streamQualityFlow

    val downloadQualityFlow: Flow<AudioStreamQualityPreference> =
        dataStore.downloadQualityFlow

    suspend fun setStreamQuality(
        preference: AudioStreamQualityPreference
    ) {
        dataStore.setStreamQuality(preference)
    }

    suspend fun setDownloadQuality(
        preference: AudioStreamQualityPreference
    ) {
        dataStore.setDownloadQuality(preference)
    }

    suspend fun clearProfile() {
        dataStore.clearProfile()
    }
}