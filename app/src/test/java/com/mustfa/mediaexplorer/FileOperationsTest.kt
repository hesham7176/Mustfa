package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.data.FileOperations
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class FileOperationsTest {
    @Test fun copyCreatesConflictFreeTarget() = runTest {
        val root = Files.createTempDirectory("operations").toFile()
        val source = root.resolve("source.txt").apply { writeText("hello") }
        val destination = root.resolve("copy.txt")
        destination.writeText("existing")
        val copied = FileOperations().copy(source, destination).getOrThrow()
        assertEquals("copy (1).txt", copied.name)
        assertTrue(copied.readText() == "hello")
        root.deleteRecursively()
    }
}
