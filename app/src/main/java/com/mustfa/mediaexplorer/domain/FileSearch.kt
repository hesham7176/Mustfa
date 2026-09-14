package com.mustfa.mediaexplorer.domain

import com.mustfa.mediaexplorer.data.FileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import kotlin.coroutines.coroutineContext

class FileSearch {
    fun search(root: File, query: String): Flow<FileEntry> = flow {
        if (query.isBlank()) return@flow
        walk(root, query.trim().lowercase()) { emit(it) }
    }.flowOn(Dispatchers.IO)

    private suspend fun walk(directory: File, query: String, emit: suspend (FileEntry) -> Unit) {
        coroutineContext.ensureActive()
        directory.listFiles().orEmpty().filterNot { it.isHidden }.forEach { child ->
            if (child.name.lowercase().contains(query)) emit(FileEntry(child))
            if (child.isDirectory) walk(child, query, emit)
        }
    }
}
