package com.example.wavex.feature.discover.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.R
import com.example.wavex.core.model.BrowseItem
import com.example.wavex.feature.discover.data.DiscoverRepository
import com.example.wavex.feature.discover.model.DiscoverUiState
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: DiscoverRepository,
    private val remoteConfig: FirebaseRemoteConfig
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())

    val uiState = _uiState.asStateFlow()

    fun loadDiscoverData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val exploreDeferred = async {
                    fetchExploreItems()
                }

                val albumsDeferred = async {
                    repository.fetchAlbums(
                        query = "popular",
                        root = "results"
                    )
                }

                val artistsDeferred = async {
                    repository.fetchArtists(
                        query = "trending artists",
                        root = "results"
                    )
                }

                val playlistsDeferred = async {
                    repository.fetchPlaylists(
                        query = "Top",
                        root = "results"
                    )
                }

                val songDeferred = async {
                    repository.fetchSongsByPlaylist(
                        playlistId = "946682072",
                        root = "songs"
                    )
                }

                _uiState.update {
                    it.copy(
                        albums = albumsDeferred.await(),
                        artists = artistsDeferred.await(),
                        playlists = playlistsDeferred.await(),
                        songs = songDeferred.await(),
                        exploreLists = exploreDeferred.await(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private suspend fun fetchExploreItems(): List<BrowseItem> =
        suspendCancellableCoroutine { continuation ->
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }

            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

            remoteConfig.fetchAndActivate()
                .addOnSuccessListener {
                    val json = remoteConfig.getString("playlistsID_json")

                    val type = object : TypeToken<Map<String, String>>() {}.type

                    val playlistIds: Map<String, String> =
                        Gson().fromJson(json, type) ?: emptyMap()

                    val exploreList = listOf(
                        BrowseItem(
                            title = "Made\nFor You",
                            subtitle = "fresh picks",
                            playlistId = playlistIds["made_for_you"].orEmpty(),
                            gradient = listOf(
                                Color(0xFFF9A26C),
                                Color(0xFFD63384)
                            )
                        ),
                        BrowseItem(
                            title = "New\nReleases",
                            subtitle = "this week",
                            playlistId = playlistIds["new_releases"].orEmpty(),
                            gradient = listOf(
                                Color(0xFFFFB347),
                                Color(0xFFFF6B35)
                            )
                        ),
                        BrowseItem(
                            title = "Top\nCharts",
                            subtitle = "India 100",
                            playlistId = playlistIds["top_charts"].orEmpty(),
                            gradient = listOf(
                                Color(0xFFA18CD1),
                                Color(0xFFFBC2EB)
                            )
                        ),
                        BrowseItem(
                            title = "Hindi",
                            subtitle = "100 songs",
                            playlistId = playlistIds["hindi"].orEmpty(),
                            gradient = listOf(
                                Color(0xFFC471ED),
                                Color(0xFF6A11CB)
                            )
                        ),
                        BrowseItem(
                            title = "Haryanvi",
                            subtitle = "100 songs",
                            playlistId = playlistIds["haryanvi"].orEmpty(),
                            gradient = listOf(
                                Color(0xFF434343),
                                Color(0xFF5B4BFF)
                            )
                        ),
                        BrowseItem(
                            title = "Punjabi",
                            subtitle = "100 songs",
                            playlistId = playlistIds["punjabi"].orEmpty(),
                            gradient = listOf(
                                Color(0xFFFF9966),
                                Color(0xFFFF5E62)
                            )
                        ),
                        BrowseItem(
                            title = "Workout",
                            subtitle = "gym vibes",
                            playlistId = playlistIds["workout"].orEmpty(),
                            gradient = listOf(
                                Color(0xFFF9A26C),
                                Color(0xFFD63384)
                            )
                        ),
                        BrowseItem(
                            title = "Romantic",
                            subtitle = "love hits",
                            playlistId = playlistIds["romantic"].orEmpty(),
                            gradient = listOf(
                                Color(0xFFFFB347),
                                Color(0xFFFF6B35)
                            )
                        )
                    )

                    continuation.resume(exploreList) { _,_,_ -> }
                }
                .addOnFailureListener {
                    continuation.resume(emptyList()) { _,_,_ -> }
                }
        }
}