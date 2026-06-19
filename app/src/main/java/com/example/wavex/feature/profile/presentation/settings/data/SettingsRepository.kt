package com.example.wavex.feature.profile.presentation.settings.data

import com.example.wavex.core.datastore.SettingsDataStore
import com.example.wavex.core.model.AudioStreamQualityPreference
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