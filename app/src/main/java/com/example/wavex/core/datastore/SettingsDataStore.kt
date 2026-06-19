package com.example.wavex.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.wavex.core.model.AudioStreamQualityPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {
    val streamQualityFlow: Flow<AudioStreamQualityPreference> =
        context.settingsDataStore.data.map { prefs ->
            val stored = prefs[SettingsKeys.STREAM_QUALITY]
            AudioStreamQualityPreference.fromLabel(stored)
        }

    suspend fun setStreamQuality(preference: AudioStreamQualityPreference) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.STREAM_QUALITY] = preference.label
        }
    }

    val downloadQualityFlow =
        context.settingsDataStore.data.map { preferences ->
            AudioStreamQualityPreference.valueOf(
                preferences[SettingsKeys.DOWNLOAD_QUALITY]
                    ?: AudioStreamQualityPreference.MEDIUM.name
            )
        }

    suspend fun setDownloadQuality(
        preference: AudioStreamQualityPreference
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[SettingsKeys.DOWNLOAD_QUALITY] =
                preference.name
        }
    }

    val profileUrlFlow: Flow<String?> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.PROFILE_URL]
        }

    val userNameFlow: Flow<String?> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.USER_NAME]
        }

    val userEmailFlow: Flow<String?> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.USER_EMAIL]
        }

    val userGenderFlow: Flow<String?> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.USER_GENDER]
        }

    val userPhoneNoFlow: Flow<String?> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.USER_PHONE_NO]
        }

    suspend fun saveProfileUrl(url: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.PROFILE_URL] = url
        }
    }

    suspend fun saveUserName(name: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.USER_NAME] = name
        }
    }

    suspend fun saveUserEmail(email: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.USER_EMAIL] = email
        }
    }

    suspend fun saveUserGender(gender: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.USER_GENDER] = gender
        }
    }

    suspend fun saveUserPhoneNo(phoneNo: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.USER_PHONE_NO] = phoneNo
        }
    }

    suspend fun clearProfile() {
        context.settingsDataStore.edit { prefs ->
            prefs.remove(SettingsKeys.PROFILE_URL)
            prefs.remove(SettingsKeys.USER_NAME)
            prefs.remove(SettingsKeys.USER_EMAIL)
            prefs.remove(SettingsKeys.USER_GENDER)
            prefs.remove(SettingsKeys.USER_PHONE_NO)
        }
    }
}