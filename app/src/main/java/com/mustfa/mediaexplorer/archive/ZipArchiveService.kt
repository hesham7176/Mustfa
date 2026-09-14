package com.mustfa.mediaexplorer.archive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.coroutines.coroutineContext

class ZipArchiveService {
    suspend fun createArchive(sources: List<File>, destination: File): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            destination.parentFile?.mkdirs()
            ZipOutputStream(destination.outputStream()).use { zip -> sources.forEach { source -> add(zip, source, source.name) } }
            destination
        }
    }

    suspend fun list(archive: File): Result<List<String>> = withContext(Dispatchers.IO) { runCatching { ZipFile(archive).use { it.entries().asSequence().map(ZipEntry::getName).toList() } } }

    suspend fun extract(archive: File, destination: File): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            destination.mkdirs()
            ZipFile(archive).use { zip -> zip.entries().asSequence().forEach { entry ->
                coroutineContext.ensureActive()
                val target = File(destination, entry.name).canonicalFile
                require(target.path == destination.canonicalFile.path || target.path.startsWith(destination.canonicalPath + File.separator)) { "Unsafe archive entry" }
                if (entry.isDirectory) target.mkdirs() else { target.parentFile?.mkdirs(); zip.getInputStream(entry).use { input -> target.outputStream().use(input::copyTo) } }
            } }
            destination
        }
    }

    private suspend fun add(zip: ZipOutputStream, source: File, path: String) {
        coroutineContext.ensureActive()
        if (source.isDirectory) {
            zip.putNextEntry(ZipEntry("$path/")); zip.closeEntry()
            source.listFiles().orEmpty().forEach { add(zip, it, "$path/${it.name}") }
        } else {
            zip.putNextEntry(ZipEntry(path)); source.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
        }
    }
}
