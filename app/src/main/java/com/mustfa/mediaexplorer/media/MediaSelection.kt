package com.mustfa.mediaexplorer.media

import com.mustfa.mediaexplorer.data.FileEntry
import java.io.File

object MediaSelection {
    private val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "webm")
    private val audioExtensions = setOf("mp3", "wav", "flac", "m4a", "ogg", "aac")
    private val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif")

    fun videos(entries: List<FileEntry>): List<FileEntry> = entries.filter { it.extension in videoExtensions }
    fun audio(entries: List<FileEntry>): List<FileEntry> = entries.filter { it.extension in audioExtensions }
    fun images(entries: List<FileEntry>): List<FileEntry> = entries.filter { it.extension in imageExtensions }
    fun supported(file: File): Boolean = file.extension.lowercase() in videoExtensions + audioExtensions + imageExtensions
}
