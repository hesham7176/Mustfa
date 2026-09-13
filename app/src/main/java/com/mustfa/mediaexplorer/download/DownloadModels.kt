package com.mustfa.mediaexplorer.download

import kotlinx.coroutines.flow.StateFlow
import java.io.File

sealed interface DownloadStatus {
    data object Queued : DownloadStatus
    data class Running(val completedBytes: Long, val totalBytes: Long?) : DownloadStatus
    data object Paused : DownloadStatus
    data object Completed : DownloadStatus
    data class Failed(val message: String) : DownloadStatus
    data object Cancelled : DownloadStatus
}

data class DownloadTask(val id: String, val source: String, val destination: File, val status: DownloadStatus)

interface DownloadManager {
    val tasks: StateFlow<List<DownloadTask>>
    suspend fun enqueue(source: String, destination: File): String
    suspend fun pause(id: String)
    suspend fun resume(id: String)
    suspend fun cancel(id: String)
    suspend fun retry(id: String)
}
