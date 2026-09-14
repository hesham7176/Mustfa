package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.analyzer.StorageAnalyzer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class StorageAnalyzerTest {
    @Test fun calculatesMediaCategoriesAndLargestFiles() = runTest {
        val root = Files.createTempDirectory("analysis").toFile()
        root.resolve("photo.jpg").apply { writeBytes(ByteArray(10)) }
        root.resolve("movie.mp4").apply { writeBytes(ByteArray(30)) }
        val result = StorageAnalyzer().analyze(root)
        assertEquals("videos", result.categories.first().id)
        assertTrue(result.largestFiles.first().name == "movie.mp4")
        root.deleteRecursively()
    }
}
