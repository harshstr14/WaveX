package com.example.wavex.deeplink

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow

data class DeepLinkEvent(
    val id: String,
    val deepLink: DeepLink
)

object DeepLinkManager {
    val events = MutableSharedFlow<DeepLinkEvent>(
        replay = 1,
        extraBufferCapacity = 1
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clear() {
        events.resetReplayCache()
    }
}