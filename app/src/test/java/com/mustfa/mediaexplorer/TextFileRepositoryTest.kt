package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.editor.TextFileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class TextFileRepositoryTest {
    @Test fun readsAndSavesText() = runTest {
        val root = Files.createTempDirectory("editor").toFile()
        val file = root.resolve("note.txt")
        val repository = TextFileRepository()
        repository.save(file, "hello").getOrThrow()
        assertEquals("hello", repository.read(file).getOrThrow())
        root.deleteRecursively()
    }

    @Test fun rejectsOversizedText() = runTest {
        val root = Files.createTempDirectory("editor-large").toFile()
        val file = root.resolve("large.txt").apply { writeBytes(ByteArray(12)) }
        assert(TextFileRepository(maxEditableBytes = 4).read(file).isFailure)
        root.deleteRecursively()
    }
}
