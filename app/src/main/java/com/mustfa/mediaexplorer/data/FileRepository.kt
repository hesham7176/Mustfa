package com.mustfa.mediaexplorer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository {
    suspend fun list(directory: File, showHidden: Boolean = false): List<FileEntry> = withContext(Dispatchers.IO) {
        directory.listFiles()
            .orEmpty()
            .filter { showHidden || !it.isHidden }
            .map(::FileEntry)
    }

    suspend fun createDirectory(parent: File, name: String): Result<File> = withContext(Dispatchers.IO) {
        val cleanName = name.trim()
        if (cleanName.isBlank() || cleanName.contains(File.separator)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid folder name"))
        }
        val destination = File(parent, cleanName)
        if (destination.exists()) Result.failure(IllegalStateException("A file already exists"))
        else if (destination.mkdir()) Result.success(destination)
        else Result.failure(IllegalStateException("Unable to create folder"))
    }

    suspend fun storageSummary(root: File): StorageSummary = withContext(Dispatchers.IO) {
        StorageSummary(root, root.totalSpace, root.freeSpace)
    }
}
