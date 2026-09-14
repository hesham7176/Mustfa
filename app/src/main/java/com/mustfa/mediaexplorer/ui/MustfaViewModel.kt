package com.mustfa.mediaexplorer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mustfa.mediaexplorer.data.FileEntry
import com.mustfa.mediaexplorer.data.FileOperations
import com.mustfa.mediaexplorer.data.FileFilter
import com.mustfa.mediaexplorer.data.FileRepository
import com.mustfa.mediaexplorer.data.CategoryLocation
import com.mustfa.mediaexplorer.data.SortField
import com.mustfa.mediaexplorer.data.SortDirection
import com.mustfa.mediaexplorer.data.StorageSummary
import com.mustfa.mediaexplorer.data.StorageLocation
import com.mustfa.mediaexplorer.data.ViewMode
import com.mustfa.mediaexplorer.media.MediaEngine
import com.mustfa.mediaexplorer.media.MediaSelection
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MustfaViewModel(private val repository: FileRepository, private val mediaEngine: MediaEngine) : ViewModel() {
    private val fileOperations = FileOperations()
    private val initialLocation = repository.locations().firstOrNull()
    private val _locations = MutableStateFlow(repository.locations())
    val locations: StateFlow<List<StorageLocation>> = _locations.asStateFlow()
    private val _safTreeUris = MutableStateFlow(repository.safTreeUris())
    val safTreeUris: StateFlow<List<String>> = _safTreeUris.asStateFlow()
    private val _isHome = MutableStateFlow(true)
    val isHome: StateFlow<Boolean> = _isHome.asStateFlow()
    private val _directory = MutableStateFlow(initialLocation?.root ?: File("/"))
    val directory: StateFlow<File> = _directory.asStateFlow()
    private val _entries = MutableStateFlow<List<FileEntry>>(emptyList())
    val entries: StateFlow<List<FileEntry>> = _entries.asStateFlow()
    private val _storage = MutableStateFlow<StorageSummary?>(null)
    val storage: StateFlow<StorageSummary?> = _storage.asStateFlow()
    private val _sort = MutableStateFlow(repository.savedSortField())
    val sort: StateFlow<SortField> = _sort.asStateFlow()
    private val _sortDirection = MutableStateFlow(repository.savedSortDirection())
    val sortDirection: StateFlow<SortDirection> = _sortDirection.asStateFlow()
    private val _viewMode = MutableStateFlow(repository.savedViewMode())
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()
    private val _selectedPaths = MutableStateFlow<Set<String>>(emptySet())
    val selectedPaths: StateFlow<Set<String>> = _selectedPaths.asStateFlow()
    private val _history = MutableStateFlow<List<File>>(emptyList())
    val history: StateFlow<List<File>> = _history.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _filter = MutableStateFlow(FileFilter.ALL)
    val filter: StateFlow<FileFilter> = _filter.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    private val _playerVisible = MutableStateFlow(false)
    val playerVisible: StateFlow<Boolean> = _playerVisible.asStateFlow()

    init { initialLocation?.let { load(it.root, addHistory = false) } }

    fun categories(location: StorageLocation): List<CategoryLocation> = repository.categories(location)
    fun rememberSafTree(uri: String) { repository.saveSafTreeUri(uri); _safTreeUris.value = repository.safTreeUris() }
    fun navigateHome() { _isHome.value = true; _selectedPaths.value = emptySet() }
    fun openLocation(location: StorageLocation) { _isHome.value = false; load(location.root) }
    fun openCategory(category: CategoryLocation) { _isHome.value = false; load(category.directory) }

    fun load(target: File = _directory.value, addHistory: Boolean = true) {
        if (!target.isDirectory) { _message.value = "Location is unavailable"; return }
        if (addHistory && target.absolutePath != _directory.value.absolutePath) _history.value = (_history.value + target).takeLast(30)
        _directory.value = target
        viewModelScope.launch {
            _loading.value = true
            runCatching {
                val listed = repository.list(target)
                _entries.value = sortEntries(listed)
                _storage.value = repository.storageSummary(target)
            }.onFailure { _message.value = it.message ?: "Unable to read location" }
            _loading.value = false
        }
    }

    fun open(entry: FileEntry) { if (entry.isDirectory) load(entry.file) }
    fun openMedia(entry: FileEntry) {
        if (!MediaSelection.supported(entry.file)) { _message.value = "This file type is not supported"; return }
        val queue = _entries.value.filter { MediaSelection.supported(it.file) }.map { Uri.fromFile(it.file) }
        val index = queue.indexOf(Uri.fromFile(entry.file)).coerceAtLeast(0)
        mediaEngine.setQueue(queue, index)
        _playerVisible.value = true
        mediaEngine.play()
    }
    fun closePlayer() { mediaEngine.pause(); _playerVisible.value = false }
    fun back() { _directory.value.parentFile?.let(::load) }
    fun setSort(field: SortField) { _sort.value = field; repository.saveSort(field, _sortDirection.value); _entries.value = sortEntries(_entries.value) }
    fun toggleSortDirection() { _sortDirection.value = if (_sortDirection.value == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING; repository.saveSort(_sort.value, _sortDirection.value); _entries.value = sortEntries(_entries.value) }
    fun setViewMode(mode: ViewMode) { _viewMode.value = mode; repository.saveViewMode(mode) }
    fun setQuery(value: String) { _query.value = value; loadEntriesFromCurrent() }
    fun setFilter(value: FileFilter) { _filter.value = value; loadEntriesFromCurrent() }
    fun refresh() { load(_directory.value, addHistory = false) }
    fun toggleSelection(entry: FileEntry) { _selectedPaths.value = if (entry.file.absolutePath in _selectedPaths.value) _selectedPaths.value - entry.file.absolutePath else _selectedPaths.value + entry.file.absolutePath }
    fun clearSelection() { _selectedPaths.value = emptySet() }
    fun deleteSelected() {
        val paths = _selectedPaths.value.toList()
        viewModelScope.launch {
            paths.forEach { path -> fileOperations.delete(File(path)).onFailure { _message.value = it.message ?: "Unable to delete item" } }
            clearSelection()
            refresh()
        }
    }
    fun rename(entry: FileEntry, newName: String) {
        viewModelScope.launch { fileOperations.rename(entry.file, newName).fold(onSuccess = { refresh() }, onFailure = { _message.value = it.message ?: "Unable to rename item" }) }
    }
    fun copySelected(destination: File, move: Boolean) {
        val paths = _selectedPaths.value.toList()
        viewModelScope.launch {
            paths.forEach { path ->
                val source = File(path)
                val result = if (move) fileOperations.move(source, File(destination, source.name)) else fileOperations.copy(source, File(destination, source.name))
                result.onFailure { _message.value = it.message ?: "Unable to complete file operation" }
            }
            clearSelection()
            refresh()
        }
    }
    fun openHistory(target: File) { load(target, addHistory = false) }
    fun clearMessage() { _message.value = null }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createDirectory(_directory.value, name).fold(
                onSuccess = { load(addHistory = false) },
                onFailure = { _message.value = it.message ?: "Unable to create folder" }
            )
        }
    }

    private fun sortEntries(source: List<FileEntry>): List<FileEntry> {
        val filtered = source.filter { it.name.contains(_query.value, ignoreCase = true) && matchesFilter(it) }
        val sorted = filtered.sortedWith(compareByDescending<FileEntry> { it.isDirectory }.thenBy {
            when (_sort.value) {
                SortField.NAME -> it.name.lowercase()
                SortField.TYPE -> it.extension
                SortField.SIZE -> it.size.toString().padStart(20, '0')
                SortField.MODIFIED -> it.modified.toString()
            }
        })
        return if (_sortDirection.value == SortDirection.ASCENDING) sorted else sorted.reversed()
    }

    private fun loadEntriesFromCurrent() {
        viewModelScope.launch { _entries.value = sortEntries(repository.list(_directory.value)) }
    }

    private fun matchesFilter(entry: FileEntry): Boolean = when (_filter.value) {
        FileFilter.ALL -> true
        FileFilter.FOLDERS -> entry.isDirectory
        FileFilter.IMAGES -> entry.extension in setOf("jpg", "jpeg", "png", "webp", "gif")
        FileFilter.VIDEOS -> entry.extension in setOf("mp4", "mkv", "avi", "mov", "webm")
        FileFilter.AUDIO -> entry.extension in setOf("mp3", "wav", "flac", "m4a", "ogg", "aac")
        FileFilter.DOCUMENTS -> entry.extension in setOf("pdf", "doc", "docx", "txt", "json", "xml", "html")
    }

    companion object {
        fun factory(repository: FileRepository, mediaEngine: MediaEngine) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MustfaViewModel(repository, mediaEngine) as T
        }
    }
}
