package com.example.wavex.deeplink

import android.net.Uri
import android.util.Log
import com.example.wavex.CryptoUtils
import java.net.URLDecoder

object DeepLinkParser {
    fun parse(uri: Uri): DeepLink {
        val segments = uri.pathSegments

        return try {
            when (segments.firstOrNull()) {

                "song" -> {
                    val token = segments.getOrNull(1)
                        ?: return DeepLink.Unknown

                    val decodedToken = URLDecoder.decode(token, "UTF-8")
                    val decrypted = CryptoUtils.decrypt(decodedToken)
                    val (_, id) = decrypted.split("|")

                    DeepLink.Song(id)
                }

                "album" -> {
                    val token = segments.getOrNull(1)
                        ?: return DeepLink.Unknown

                    val decodedToken = URLDecoder.decode(token, "UTF-8")
                    val decrypted = CryptoUtils.decrypt(decodedToken)
                    val (source, id) = decrypted.split("|")

                    DeepLink.Album(
                        id = id,
                        source = source
                    )
                }

                "playlist" -> {
                    val token = segments.getOrNull(1)
                        ?: return DeepLink.Unknown

                    val decodedToken = URLDecoder.decode(token, "UTF-8")
                    val decrypted = CryptoUtils.decrypt(decodedToken)
                    val (source, id) = decrypted.split("|")

                    DeepLink.Playlist(
                        id = id,
                        source = source
                    )
                }

                "artist" -> {
                    val token = segments.getOrNull(1)
                        ?: return DeepLink.Unknown

                    val decodedToken = URLDecoder.decode(token, "UTF-8")
                    val decrypted = CryptoUtils.decrypt(decodedToken)
                    val (source, id) = decrypted.split("|")

                    DeepLink.Artist(
                        id = id,
                        source = source
                    )
                }

                "userplaylist" -> {
                    DeepLink.UserPlaylist(
                        id = uri.toString()
                    )
                }

                else -> DeepLink.Unknown
            }
        } catch (e: Exception) {
            Log.e("DeepLinkParser", "Failed to parse deep link", e)
            DeepLink.Unknown
        }
    }
}