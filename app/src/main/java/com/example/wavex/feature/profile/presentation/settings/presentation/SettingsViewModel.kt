package com.example.wavex.feature.profile.presentation.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.core.model.AudioStreamQualityPreference
import com.example.wavex.feature.profile.presentation.settings.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val streamQuality: StateFlow<AudioStreamQualityPreference> =
        repository.streamQualityFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AudioStreamQualityPreference.MEDIUM
            )

    val downloadQuality: StateFlow<AudioStreamQualityPreference> =
        repository.downloadQualityFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AudioStreamQualityPreference.MEDIUM
            )

    fun setStreamQuality(
        preference: AudioStreamQualityPreference
    ) {
        viewModelScope.launch {
            repository.setStreamQuality(preference)
        }
    }

    fun setDownloadQuality(
        preference: AudioStreamQualityPreference
    ) {
        viewModelScope.launch {
            repository.setDownloadQuality(preference)
        }
    }

    fun clearProfile() {
        viewModelScope.launch {
            repository.clearProfile()
        }
    }
}