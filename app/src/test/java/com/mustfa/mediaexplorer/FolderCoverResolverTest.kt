package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.domain.FolderCoverResolver
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class FolderCoverResolverTest {
    @Test fun prefersCoverOverPosterAndFallback() {
        val directory = Files.createTempDirectory("covers").toFile()
        val fallback = directory.resolve("image.jpg").apply { createNewFile() }
        directory.resolve("poster.png").createNewFile()
        directory.resolve("cover.webp").createNewFile()
        assertEquals("cover.webp", FolderCoverResolver().resolve(directory)?.name)
        fallback.delete()
        directory.deleteRecursively()
    }
}
