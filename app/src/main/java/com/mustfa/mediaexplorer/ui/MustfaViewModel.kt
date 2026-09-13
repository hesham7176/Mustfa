package com.mustfa.mediaexplorer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mustfa.mediaexplorer.data.FileEntry
import com.mustfa.mediaexplorer.data.FileRepository
import com.mustfa.mediaexplorer.data.SortField
import com.mustfa.mediaexplorer.data.StorageSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MustfaViewModel(private val repository: FileRepository) : ViewModel() {
    private val _directory = MutableStateFlow(File(System.getProperty("user.home") ?: "/"))
    val directory: StateFlow<File> = _directory.asStateFlow()
    private val _entries = MutableStateFlow<List<FileEntry>>(emptyList())
    val entries: StateFlow<List<FileEntry>> = _entries.asStateFlow()
    private val _storage = MutableStateFlow<StorageSummary?>(null)
    val storage: StateFlow<StorageSummary?> = _storage.asStateFlow()
    private val _sort = MutableStateFlow(SortField.NAME)
    val sort: StateFlow<SortField> = _sort.asStateFlow()
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init { load(_directory.value) }

    fun load(target: File = _directory.value) {
        if (!target.isDirectory) return
        _directory.value = target
        viewModelScope.launch {
            _entries.value = sortEntries(repository.list(target))
            _storage.value = repository.storageSummary(target)
        }
    }

    fun open(entry: FileEntry) { if (entry.isDirectory) load(entry.file) }
    fun back() { _directory.value.parentFile?.let(::load) }
    fun setSort(field: SortField) { _sort.value = field; _entries.value = sortEntries(_entries.value) }
    fun setQuery(value: String) { _query.value = value }
    fun clearMessage() { _message.value = null }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createDirectory(_directory.value, name).fold(
                onSuccess = { load() },
                onFailure = { _message.value = it.message ?: "Unable to create folder" }
            )
        }
    }

    private fun sortEntries(source: List<FileEntry>): List<FileEntry> {
        val filtered = source.filter { it.name.contains(_query.value, ignoreCase = true) }
        return filtered.sortedWith(compareByDescending<FileEntry> { it.isDirectory }.thenBy {
            when (_sort.value) {
                SortField.NAME -> it.name.lowercase()
                SortField.TYPE -> it.extension
                SortField.SIZE -> it.size.toString().padStart(20, '0')
                SortField.MODIFIED -> it.modified.toString()
            }
        })
    }

    companion object {
        fun factory(repository: FileRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MustfaViewModel(repository) as T
        }
    }
}
