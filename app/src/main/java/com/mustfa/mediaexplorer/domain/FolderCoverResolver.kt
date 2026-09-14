package com.mustfa.mediaexplorer.domain

import java.io.File

class FolderCoverResolver {
    private val preferredNames = listOf("cover", "poster", "folder")
    private val imageExtensions = setOf("jpg", "jpeg", "png", "webp")

    fun resolve(directory: File): File? {
        val children = directory.listFiles().orEmpty().filter { it.isFile && !it.isHidden }
        preferredNames.forEach { name ->
            children.firstOrNull { it.nameWithoutExtension.equals(name, ignoreCase = true) && it.extension.lowercase() in imageExtensions }
                ?.let { return it }
        }
        return children.firstOrNull { it.extension.lowercase() in imageExtensions }
    }
}
