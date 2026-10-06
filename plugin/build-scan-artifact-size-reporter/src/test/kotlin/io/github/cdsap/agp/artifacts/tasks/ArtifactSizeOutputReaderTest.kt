package io.github.cdsap.agp.artifacts.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ArtifactSizeOutputReaderTest {
    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun readsMultipleMarkersInDiscoveredOrderWithTheirContents() {
        val outputDir = tempFolder.newFolder("markers")
        val markers = listOf(
            File(outputDir, "app-debug.apk.size").also { it.writeText("42") },
            File(outputDir, "app-release.aab.size").also { it.writeText("100") },
            File(outputDir, "mylibrary-release.aar.size").also { it.writeText("7") },
        )

        val values = ArtifactSizeOutputReader.read(markers)

        assertEquals(
            listOf(
                "app-debug.apk.size" to "42",
                "app-release.aab.size" to "100",
                "mylibrary-release.aar.size" to "7",
            ),
            values,
        )
        assertTrue(outputDir.exists())
    }

    @Test
    fun emptyFileProducesAnEmptyValue() {
        val outputDir = tempFolder.newFolder("markers")
        val marker = File(outputDir, "empty.apk.size").also { it.writeText("") }

        val values = ArtifactSizeOutputReader.read(listOf(marker))

        assertEquals(listOf("empty.apk.size" to ""), values)
    }

    @Test
    fun missingFilesAreIgnored() {
        val missing = File(tempFolder.root, "does-not-exist.apk.size")

        val values = ArtifactSizeOutputReader.read(listOf(missing))

        assertTrue(values.isEmpty())
        assertFalse(missing.exists())
    }

    @Test
    fun preservesDuplicateNamesFromDiscoveredFiles() {
        val outputDir = tempFolder.newFolder("markers")
        val first = File(File(outputDir, "first"), "module.apk.size").also {
            it.parentFile.mkdirs()
            it.writeText("5")
        }
        val second = File(File(outputDir, "second"), "module.apk.size").also {
            it.parentFile.mkdirs()
            it.writeText("9")
        }

        val values = ArtifactSizeOutputReader.read(listOf(first, second))

        assertEquals(
            listOf(
                "module.apk.size" to "5",
                "module.apk.size" to "9",
            ),
            values,
        )
    }
}
