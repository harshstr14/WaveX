package com.example.wavex.updateAppScreen

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class AppDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val version = inputData.getString("version") ?: ""
        val expectedSizeInBytes = inputData.getLong("expectedSizeInBytes", 0)

        val file = File(applicationContext.getExternalFilesDir(null), "waveX.apk")
        val client = OkHttpClient()

        // Check how many bytes have already been downloaded
        val downloadedBytes = if (file.exists()) file.length() else 0L

        // Add Range header only if file exists (for resume)
        val request = Request.Builder()
            .url(url)
            .apply {
                if (downloadedBytes > 0) {
                    addHeader("Range", "bytes=$downloadedBytes-")
                }
            }
            .build()

        return try {
            val response = client.newCall(request).execute()

            // Ensure server supports resume or new download
            if (response.code != 200 && response.code != 206) {
                return Result.retry()
            }

            // Append only if server responded with 206 Partial Content
            val append = downloadedBytes > 0 && response.code == 206
            val body = response.body ?: return Result.failure()
            val contentLength = body.contentLength()
            val totalBytes = if (contentLength != -1L) {
                contentLength + if (append) downloadedBytes else 0L
            } else {
                -1L
            }

            withContext(Dispatchers.IO) {
                body.byteStream().use { input ->
                    FileOutputStream(file, append).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var progressBytes = if (append) downloadedBytes else 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            if (isStopped) return@withContext  // Stop gracefully on pause

                            output.write(buffer, 0, bytesRead)
                            progressBytes += bytesRead

                            val progress = if (totalBytes > 0) {
                                (progressBytes * 100 / totalBytes).toInt()
                            } else 0

                            setProgress(workDataOf("progress" to progress))
                        }
                    }
                }
            }

            // Make sure the file was downloaded correctly
            if (!file.exists() || file.length() == 0L) {
                return Result.retry()
            }

            // Save the version
            if (file.length() >= expectedSizeInBytes) {
                saveDownloadedVersion(applicationContext, version)
            }

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}