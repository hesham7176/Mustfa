package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.data.FileEntry
import com.mustfa.mediaexplorer.media.MediaSelection
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class MediaSelectionTest {
    @Test fun separatesVideoAndAudioQueues() {
        val root = Files.createTempDirectory("media").toFile()
        val entries = listOf("episode.mp4", "song.flac", "notes.txt").map { FileEntry(root.resolve(it).apply { createNewFile() }) }
        assertEquals(listOf("episode.mp4"), MediaSelection.videos(entries).map { it.name })
        assertEquals(listOf("song.flac"), MediaSelection.audio(entries).map { it.name })
        root.deleteRecursively()
    }
}
