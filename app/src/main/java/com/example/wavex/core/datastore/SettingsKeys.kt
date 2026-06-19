package com.example.wavex.core.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val STREAM_QUALITY = stringPreferencesKey("stream_quality")
    val DOWNLOAD_QUALITY = stringPreferencesKey("download_quality")
    val PROFILE_URL = stringPreferencesKey("profile_url")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_GENDER = stringPreferencesKey("user_gender")
    val USER_PHONE_NO = stringPreferencesKey("user_phoneNo")
}