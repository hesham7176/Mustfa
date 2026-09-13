package com.mustfa.mediaexplorer.data

import java.io.File

 enum class SortField { NAME, TYPE, SIZE, MODIFIED }

data class FileEntry(
    val file: File,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isFile) file.length() else 0L,
    val modified: Long = file.lastModified()
) {
    val name: String get() = file.name
    val extension: String get() = file.extension.lowercase()
}

data class StorageSummary(
    val root: File,
    val totalBytes: Long,
    val freeBytes: Long
) {
    val usedBytes: Long get() = (totalBytes - freeBytes).coerceAtLeast(0)
    val usageRatio: Float get() = if (totalBytes == 0L) 0f else usedBytes.toFloat() / totalBytes
}
