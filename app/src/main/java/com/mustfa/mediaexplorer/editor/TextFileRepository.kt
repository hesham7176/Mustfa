package com.mustfa.mediaexplorer.editor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset

class TextFileRepository(private val maxEditableBytes: Long = 4L * 1024L * 1024L) {
    suspend fun read(file: File, charset: Charset = Charsets.UTF_8): Result<String> = withContext(Dispatchers.IO) { runCatching {
        require(file.isFile) { "File is unavailable" }
        require(file.length() <= maxEditableBytes) { "File is too large to edit" }
        file.readText(charset)
    } }

    suspend fun save(file: File, content: String, charset: Charset = Charsets.UTF_8): Result<File> = withContext(Dispatchers.IO) { runCatching {
        file.parentFile?.mkdirs()
        file.outputStream().use { it.write(content.toByteArray(charset)) }
        file
    } }

    suspend fun saveAs(source: File, destination: File, content: String, charset: Charset = Charsets.UTF_8): Result<File> = withContext(Dispatchers.IO) { runCatching {
        require(source.absolutePath != destination.absolutePath) { "Destination must differ from source" }
        destination.parentFile?.mkdirs()
        destination.outputStream().use { it.write(content.toByteArray(charset)) }
        destination
    } }
}
