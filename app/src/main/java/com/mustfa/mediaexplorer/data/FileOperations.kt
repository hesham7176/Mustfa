package com.mustfa.mediaexplorer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

data class OperationProgress(val completedBytes: Long, val totalBytes: Long, val currentName: String)

class FileOperations {
    suspend fun rename(source: File, newName: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            require(newName.isNotBlank() && newName != "." && newName != "..") { "Invalid name" }
            val target = File(source.parentFile, newName.trim())
            require(!target.exists()) { "A file already exists" }
            require(source.renameTo(target)) { "Unable to rename item" }
            target
        }
    }

    suspend fun copy(source: File, destination: File, onProgress: (OperationProgress) -> Unit = {}): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            require(source.exists()) { "Source does not exist" }
            val target = uniqueTarget(destination)
            if (source.isDirectory) copyDirectory(source, target, onProgress) else copyFile(source, target, onProgress)
            target
        }
    }

    suspend fun move(source: File, destination: File): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            require(source.exists()) { "Source does not exist" }
            val target = uniqueTarget(destination)
            if (!source.renameTo(target)) {
                if (source.isDirectory) copyDirectory(source, target) else copyFile(source, target)
                require(source.deleteRecursively()) { "Unable to remove source" }
            }
            target
        }
    }

    suspend fun delete(source: File): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { require(source.deleteRecursively()) { "Unable to delete item" } }
    }

    private suspend fun copyDirectory(source: File, target: File, onProgress: (OperationProgress) -> Unit = {}) {
        coroutineContext.ensureActive()
        require(target.mkdirs() || target.isDirectory) { "Unable to create destination" }
        source.listFiles().orEmpty().forEach { child ->
            val childTarget = File(target, child.name)
            if (child.isDirectory) copyDirectory(child, childTarget, onProgress) else copyFile(child, childTarget, onProgress)
        }
    }

    private suspend fun copyFile(source: File, target: File, onProgress: (OperationProgress) -> Unit = {}) {
        target.parentFile?.mkdirs()
        source.inputStream().use { input -> target.outputStream().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var copied = 0L
            var read: Int
            while (input.read(buffer).also { read = it } > 0) {
                coroutineContext.ensureActive()
                output.write(buffer, 0, read)
                copied += read
                onProgress(OperationProgress(copied, source.length(), source.name))
            }
        }}
    }

    private fun uniqueTarget(destination: File): File {
        if (!destination.exists()) return destination
        val base = destination.nameWithoutExtension
        val extension = destination.extension.takeIf { it.isNotBlank() }?.let { ".${it}" }.orEmpty()
        return generateSequence(1) { it + 1 }.map { File(destination.parentFile, "$base ($it)$extension") }.first { !it.exists() }
    }
}
