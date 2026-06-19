package com.example.wavex.core.service

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.example.wavex.HttpClientProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.cancellation.CancellationException

object ParallelDownloader {
    private val semaphore = Semaphore(5)
    private val jobs = mutableMapOf<String, Job>()

    enum class DownloadState {
        DOWNLOADING,
        PAUSED,
        COMPLETED,
        FAILED
    }

    val downloadStates = mutableStateMapOf<String, DownloadState>()

    fun getState(songId: String): DownloadState? {
        return downloadStates[songId]
    }

    fun pause(songId: String) {
        jobs[songId]?.cancel()
        jobs.remove(songId)

        downloadStates[songId] = DownloadState.PAUSED
    }

    fun resume(
        scope: CoroutineScope,
        songId: String,
        url: String,
        fileName: String,
        context: Context,
        onFinished: suspend (String?) -> Unit
    ) {
        if (downloadStates[songId] != DownloadState.PAUSED) return

        if (jobs[songId]?.isActive == true) return

        jobs[songId]?.cancel()
        jobs.remove(songId)

        downloadStates[songId] = DownloadState.DOWNLOADING

        val job = scope.launch {
            Log.d("DOWNLOAD_DEBUG", "Resume download for $songId")

            try {
                val path = semaphore.withPermit {
                    downloadSong(url, fileName, context)
                }

                if (isActive) {
                    downloadStates[songId] =
                        if (path != null) DownloadState.COMPLETED else DownloadState.FAILED

                    onFinished(path)
                }

            } catch (e: CancellationException) {
                Log.d("DOWNLOAD_DEBUG", "Cancelled $songId")
                throw e

            } finally {
                jobs.remove(songId)
            }
        }

        jobs[songId] = job
    }

    fun start(
        scope: CoroutineScope,
        songId: String,
        url: String,
        fileName: String,
        context: Context,
        onFinished: suspend (String?) -> Unit
    ) {
        val currentState = downloadStates[songId]

        if (jobs[songId]?.isActive == true && currentState == DownloadState.DOWNLOADING) return

        jobs[songId]?.cancel()
        jobs.remove(songId)

        downloadStates[songId] = DownloadState.DOWNLOADING

        val job = scope.launch {
            Log.d("DOWNLOAD_DEBUG", "Start download for $songId")

            try {
                val path = semaphore.withPermit {
                    downloadSong(url, fileName, context)
                }

                if (isActive) {
                    downloadStates[songId] =
                        if (path != null) DownloadState.COMPLETED else DownloadState.FAILED

                    onFinished(path)
                }

            } catch (e: CancellationException) {
                Log.d("DOWNLOAD_DEBUG", "Cancelled $songId")
                throw e

            } finally {
                jobs.remove(songId)
            }
        }

        jobs[songId] = job
    }

    suspend fun downloadSong(
        url: String,
        fileName: String,
        context: Context
    ): String? = withContext(Dispatchers.IO) {
        val client = HttpClientProvider.client
        Log.d("DOWNLOAD_DEBUG", "Download started for $fileName")

        try {
            val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")

            val initialRequest = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Accept", "*/*")
                .addHeader("Referer", "https://www.jiosaavn.com/")
                .build()

            val initialResponse = client.newCall(initialRequest).execute()

            if (!initialResponse.isSuccessful) {
                Log.e("DOWNLOAD_DEBUG", "Initial request failed: ${initialResponse.code}")
                return@withContext null
            }

            val contentType = initialResponse.header("Content-Type") ?: ""

            val extension = when {
                contentType.contains("mp4") -> ".mp4"
                contentType.contains("mpeg") -> ".mp3"
                else -> ".mp3"
            }

            val file = File(context.filesDir, "$safeFileName$extension")

            val downloadedBytes = if (file.exists()) file.length() else 0
            Log.d("DOWNLOAD_DEBUG", "Resuming from byte: $downloadedBytes")

            initialResponse.close()

            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Accept", "*/*")
                .addHeader("Referer", "https://www.jiosaavn.com/")

            if (downloadedBytes > 0) {
                requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
                Log.d("DOWNLOAD_DEBUG", "Range header: bytes=$downloadedBytes-")
            }

            val request = requestBuilder.build()
            val call = client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            val response = call.execute()

            Log.d("DOWNLOAD_DEBUG", "Response code: ${response.code}")
            Log.d("DOWNLOAD_DEBUG", "Accept-Ranges: ${response.header("Accept-Ranges")}")

            if (!response.isSuccessful) return@withContext null

            if (downloadedBytes > 0 && response.code != 206) {
                Log.w("DOWNLOAD_DEBUG", "Server doesn't support resume. Restarting...")
                file.delete()
                return@withContext downloadSong(url, fileName, context)
            }

            val body = response.body ?: return@withContext null

            body.byteStream().use { input ->
                FileOutputStream(file, true).buffered().use { output ->

                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytes = downloadedBytes

                    while (input.read(buffer).also { bytesRead = it } != -1) {

                        output.write(buffer, 0, bytesRead)

                        output.flush()

                        ensureActive()

                        totalBytes += bytesRead
                        //Log.d("DOWNLOAD_DEBUG", "Downloaded: $totalBytes bytes")
                    }
                }
            }

            Log.d("DOWNLOAD_DEBUG", "Saved: ${file.absolutePath}")
            file.absolutePath

        } catch (e: Exception) {
            if (e is CancellationException) {
                Log.d("DOWNLOAD_DEBUG", "Download cancelled")
                throw e
            }

            Log.e("DOWNLOAD_DEBUG", "Error", e)
            null
        }
    }
}