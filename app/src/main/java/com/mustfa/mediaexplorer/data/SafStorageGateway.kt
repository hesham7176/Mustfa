package com.mustfa.mediaexplorer.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/** Boundary for USB/SD providers exposed through Android's Storage Access Framework. */
class SafStorageGateway(private val context: Context) {
    fun root(treeUri: Uri): DocumentFile? = DocumentFile.fromTreeUri(context, treeUri)

    fun list(treeUri: Uri): List<DocumentFile> = root(treeUri)?.listFiles()?.toList().orEmpty()

    fun createFolder(parentUri: Uri, name: String): Result<Uri> = runCatching {
        val parent = requireNotNull(DocumentFile.fromTreeUri(context, parentUri)) { "Storage is disconnected" }
        require(!name.contains('/')) { "Invalid folder name" }
        requireNotNull(parent.createDirectory(name)) { "Unable to create folder" }.uri
    }

    fun rename(uri: Uri, name: String): Result<Boolean> = runCatching {
        requireNotNull(DocumentFile.fromSingleUri(context, uri)) { "File is unavailable" }.renameTo(name)
    }

    fun delete(uri: Uri): Result<Boolean> = runCatching {
        requireNotNull(DocumentFile.fromSingleUri(context, uri)) { "File is unavailable" }.delete()
    }
}
