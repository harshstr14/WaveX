package com.example.wavex.service

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.example.wavex.homeScreen.downloadSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.cancellation.CancellationException

object ParallelDownloader {
    private val semaphore = kotlinx.coroutines.sync.Semaphore(5)
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
}