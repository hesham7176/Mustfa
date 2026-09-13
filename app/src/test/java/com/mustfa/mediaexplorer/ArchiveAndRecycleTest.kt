package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.archive.ZipArchiveService
import com.mustfa.mediaexplorer.recyclebin.RecycleBinRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class ArchiveAndRecycleTest {
    @Test fun zipRoundTripPreservesFile() = runTest {
        val root = Files.createTempDirectory("zip").toFile()
        val source = root.resolve("note.txt").apply { writeText("hello") }
        val archive = root.resolve("notes.zip")
        val extracted = root.resolve("out")
        val service = ZipArchiveService()
        service.createArchive(listOf(source), archive).getOrThrow()
        assertEquals(listOf("note.txt"), service.list(archive).getOrThrow())
        service.extract(archive, extracted).getOrThrow()
        assertEquals("hello", extracted.resolve("note.txt").readText())
        root.deleteRecursively()
    }

    @Test fun recycleBinRestoresOriginalPath() = runTest {
        val root = Files.createTempDirectory("bin").toFile()
        val source = root.resolve("note.txt").apply { writeText("hello") }
        val repository = RecycleBinRepository(root)
        repository.moveToBin(source).getOrThrow()
        val item = repository.list().single()
        assertTrue(!source.exists())
        repository.restore(item).getOrThrow()
        assertEquals("hello", source.readText())
        root.deleteRecursively()
    }
}
