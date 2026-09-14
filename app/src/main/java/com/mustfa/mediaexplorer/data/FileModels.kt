package com.mustfa.mediaexplorer.data

import android.content.Context
import android.os.Environment
import java.io.File

enum class SortField { NAME, TYPE, SIZE, MODIFIED }
enum class SortDirection { ASCENDING, DESCENDING }
enum class FileFilter { ALL, FOLDERS, IMAGES, VIDEOS, AUDIO, DOCUMENTS }
enum class ViewMode(val grid: Boolean, val compact: Boolean, val details: Boolean) {
    SMALL_GRID(true, true, false), MEDIUM_GRID(true, false, false), LARGE_GRID(true, false, false),
    SMALL_LIST(false, true, false), MEDIUM_LIST(false, false, false), LARGE_LIST(false, false, false),
    SMALL_DETAILS(false, true, true), MEDIUM_DETAILS(false, false, true), LARGE_DETAILS(false, false, true)
}

data class StorageLocation(
    val id: String,
    val name: String,
    val root: File,
    val removable: Boolean,
    val accessible: Boolean = root.isDirectory
) {
    val totalBytes: Long get() = root.totalSpace
    val freeBytes: Long get() = root.freeSpace
    val usedBytes: Long get() = (totalBytes - freeBytes).coerceAtLeast(0)
    val usageRatio: Float get() = if (totalBytes == 0L) 0f else usedBytes.toFloat() / totalBytes
}

data class CategoryLocation(val id: String, val titleRes: Int, val iconKey: String, val directory: File)

fun storageLocations(context: Context): List<StorageLocation> = context.getExternalFilesDirs(null)
    .filterNotNull()
    .mapIndexed { index, file ->
        val removable = Environment.isExternalStorageRemovable(file)
        StorageLocation(
            id = if (removable) "removable-$index" else "internal",
            name = if (removable) "Removable storage" else "Internal storage",
            root = file,
            removable = removable
        )
    }
    .distinctBy { it.id }

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
