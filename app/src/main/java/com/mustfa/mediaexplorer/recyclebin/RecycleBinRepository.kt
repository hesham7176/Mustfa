package com.mustfa.mediaexplorer.recyclebin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Properties

class RecycleBinRepository(private val root: File) {
    private val bin = File(root, "recycle-bin").apply { mkdirs() }

    suspend fun moveToBin(source: File): Result<File> = withContext(Dispatchers.IO) { runCatching {
        require(source.exists()) { "Item does not exist" }
        val id = "${System.currentTimeMillis()}_${source.name.hashCode()}"
        val item = File(bin, id)
        if (!source.renameTo(item)) { source.copyRecursively(item, overwrite = false); require(source.deleteRecursively()) }
        val metadata = Properties().apply { setProperty("original", source.absolutePath); setProperty("deletedAt", System.currentTimeMillis().toString()); setProperty("name", source.name) }
        metadata.store(File(bin, "$id.properties").outputStream(), null)
        item
    } }

    suspend fun list(): List<RecycleItem> = withContext(Dispatchers.IO) { bin.listFiles().orEmpty().filter { it.extension != "properties" }.mapNotNull { item ->
        val metadata = Properties()
        File(bin, "${item.name}.properties").takeIf(File::exists)?.inputStream()?.use(metadata::load)
        val original = metadata.getProperty("original") ?: return@mapNotNull null
        RecycleItem(item, File(original), metadata.getProperty("deletedAt")?.toLongOrNull() ?: 0L)
    } }

    suspend fun restore(item: RecycleItem): Result<File> = withContext(Dispatchers.IO) { runCatching {
        item.original.parentFile?.mkdirs()
        require(!item.original.exists()) { "Original path already exists" }
        require(item.bin.renameTo(item.original)) { "Unable to restore item" }
        File(bin, "${item.bin.name}.properties").delete()
        item.original
    } }

    suspend fun deletePermanently(item: RecycleItem): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        require(item.bin.deleteRecursively())
        File(bin, "${item.bin.name}.properties").delete()
        Unit
    } }
    suspend fun empty() { withContext(Dispatchers.IO) { bin.listFiles().orEmpty().forEach(File::deleteRecursively) } }
}

data class RecycleItem(val bin: File, val original: File, val deletedAt: Long)
