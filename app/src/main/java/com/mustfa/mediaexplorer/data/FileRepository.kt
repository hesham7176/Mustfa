package com.mustfa.mediaexplorer.data

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository(private val context: Context) {
    private val preferences = context.getSharedPreferences("file_browser_preferences", Context.MODE_PRIVATE)

    fun savedViewMode(): ViewMode = runCatching { ViewMode.valueOf(preferences.getString("view_mode", null).orEmpty()) }.getOrDefault(ViewMode.MEDIUM_LIST)
    fun saveViewMode(mode: ViewMode) { preferences.edit().putString("view_mode", mode.name).apply() }
    fun savedSortField(): SortField = runCatching { SortField.valueOf(preferences.getString("sort_field", null).orEmpty()) }.getOrDefault(SortField.NAME)
    fun savedSortDirection(): SortDirection = runCatching { SortDirection.valueOf(preferences.getString("sort_direction", null).orEmpty()) }.getOrDefault(SortDirection.ASCENDING)
    fun saveSort(field: SortField, direction: SortDirection) { preferences.edit().putString("sort_field", field.name).putString("sort_direction", direction.name).apply() }

    fun locations(): List<StorageLocation> = storageLocations(context)

    fun categories(location: StorageLocation): List<CategoryLocation> = listOf(
        CategoryLocation("images", com.mustfa.mediaexplorer.R.string.photos, "image", File(location.root, Environment.DIRECTORY_PICTURES)),
        CategoryLocation("videos", com.mustfa.mediaexplorer.R.string.videos, "video", File(location.root, Environment.DIRECTORY_MOVIES)),
        CategoryLocation("music", com.mustfa.mediaexplorer.R.string.music, "music", File(location.root, Environment.DIRECTORY_MUSIC)),
        CategoryLocation("downloads", com.mustfa.mediaexplorer.R.string.downloads, "download", File(location.root, Environment.DIRECTORY_DOWNLOADS)),
        CategoryLocation("documents", com.mustfa.mediaexplorer.R.string.documents, "document", File(location.root, Environment.DIRECTORY_DOCUMENTS))
    )

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
