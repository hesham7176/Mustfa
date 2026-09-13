package com.mustfa.mediaexplorer.thumbnails

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.LruCache
import com.mustfa.mediaexplorer.data.FileEntry
import com.mustfa.mediaexplorer.domain.FolderCoverResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed interface ThumbnailResult {
    data class Image(val file: File) : ThumbnailResult
    data object None : ThumbnailResult
}

class ThumbnailEngine(context: Context) {
    private val cacheDirectory = File(context.cacheDir, "thumbnails").apply { mkdirs() }
    private val memoryCache = object : LruCache<String, File>(24) {}
    private val folderCoverResolver = FolderCoverResolver()

    suspend fun resolve(entry: FileEntry): ThumbnailResult = withContext(Dispatchers.IO) {
        if (entry.isDirectory) return@withContext folderCoverResolver.resolve(entry.file)?.let { ThumbnailResult.Image(it) } ?: ThumbnailResult.None
        if (entry.extension in IMAGE_EXTENSIONS) return@withContext ThumbnailResult.Image(entry.file)
        if (entry.extension !in VIDEO_EXTENSIONS && entry.extension !in AUDIO_EXTENSIONS) return@withContext ThumbnailResult.None
        val key = cacheKey(entry)
        memoryCache.get(key)?.takeIf(File::exists)?.let { return@withContext ThumbnailResult.Image(it) }
        val cached = File(cacheDirectory, "$key.jpg")
        if (cached.exists()) {
            memoryCache.put(key, cached)
            return@withContext ThumbnailResult.Image(cached)
        }
        val generated = runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(entry.file.absolutePath)
                val bitmap = if (entry.extension in VIDEO_EXTENSIONS) retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) else retriever.embeddedPicture?.let { bytes -> BitmapFactory.decode(bytes) }
                bitmap?.let { save(it, cached) }
            } finally {
                retriever.release()
            }
        }.getOrNull()
        generated?.let { memoryCache.put(key, it); ThumbnailResult.Image(it) } ?: ThumbnailResult.None
    }

    private fun save(bitmap: Bitmap, destination: File): File? {
        destination.outputStream().use { output -> if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 86, output)) return null }
        bitmap.recycle()
        return destination
    }

    private fun cacheKey(entry: FileEntry): String = "${entry.file.absolutePath.hashCode()}_${entry.file.length()}_${entry.file.lastModified()}"

    companion object {
        val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")
        val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "webm")
        val AUDIO_EXTENSIONS = setOf("mp3", "wav", "flac", "m4a", "ogg", "aac")
    }
}

private object BitmapFactory {
    fun decode(bytes: ByteArray): Bitmap? = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
