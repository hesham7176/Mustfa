package com.mustfa.mediaexplorer.analyzer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

data class StorageCategory(val id: String, val bytes: Long, val fileCount: Int)
data class StorageAnalysis(
    val root: File,
    val totalBytes: Long,
    val usedBytes: Long,
    val categories: List<StorageCategory>,
    val largestFiles: List<File>,
    val largestFolders: List<Pair<File, Long>>
)

class StorageAnalyzer {
    suspend fun analyze(root: File, largestLimit: Int = 20): StorageAnalysis = withContext(Dispatchers.IO) {
        val categoryBytes = linkedMapOf<String, Long>()
        val categoryCounts = linkedMapOf<String, Int>()
        val files = mutableListOf<File>()
        val folderBytes = mutableMapOf<File, Long>()
        root.walkTopDown().onEnter { coroutineContext.ensureActive(); true }.forEach { file ->
            coroutineContext.ensureActive()
            if (file.isFile) {
                val bytes = file.length()
                files += file
                val category = category(file)
                categoryBytes[category] = (categoryBytes[category] ?: 0L) + bytes
                categoryCounts[category] = (categoryCounts[category] ?: 0) + 1
                var parent = file.parentFile
                while (parent != null && parent != root) {
                    folderBytes[parent] = (folderBytes[parent] ?: 0L) + bytes
                    parent = parent.parentFile
                }
            }
        }
        StorageAnalysis(root, root.totalSpace, files.sumOf(File::length), categoryBytes.map { (id, bytes) -> StorageCategory(id, bytes, categoryCounts[id] ?: 0) }.sortedByDescending { it.bytes }, files.sortedByDescending(File::length).take(largestLimit), folderBytes.entries.sortedByDescending { it.value }.take(largestLimit).map { it.toPair() })
    }

    private fun category(file: File): String = when (file.extension.lowercase()) {
        "jpg", "jpeg", "png", "webp", "gif" -> "images"
        "mp4", "mkv", "avi", "mov", "webm" -> "videos"
        "mp3", "wav", "flac", "m4a", "ogg", "aac" -> "audio"
        "zip", "rar", "7z", "tar", "gz" -> "archives"
        "pdf", "doc", "docx", "txt", "json", "xml", "html", "css", "js" -> "documents"
        "apk" -> "apps"
        else -> "other"
    }
}
