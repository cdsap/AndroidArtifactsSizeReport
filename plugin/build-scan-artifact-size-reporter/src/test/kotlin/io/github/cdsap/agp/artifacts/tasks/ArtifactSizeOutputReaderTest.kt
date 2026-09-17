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
    fun readsMultipleApkAabAndAarMarkersAsNameValuePairs() {
        val outputDir = tempFolder.newFolder("markers")
        File(outputDir, "app-debug.apk.size").writeText("42")
        File(outputDir, "app-release.aab.size").writeText("100")
        File(outputDir, "mylibrary-release.aar.size").writeText("7")

        val markers = ArtifactSizeOutputReader.read(outputDir)

        assertEquals(
            setOf(
                "app-debug.apk.size" to "42",
                "app-release.aab.size" to "100",
                "mylibrary-release.aar.size" to "7",
            ),
            markers.toSet(),
        )
        assertTrue(outputDir.exists())
    }

    @Test
    fun emptyOutputDirectoryReturnsNoMarkersAndLeavesDirectoryIntact() {
        val outputDir = tempFolder.newFolder("markers")

        val markers = ArtifactSizeOutputReader.read(outputDir)

        assertTrue(markers.isEmpty())
        assertTrue(outputDir.exists())
    }

    @Test
    fun missingOutputDirectoryReturnsNoMarkers() {
        val missing = File(tempFolder.root, "does-not-exist")

        val markers = ArtifactSizeOutputReader.read(missing)

        assertTrue(markers.isEmpty())
        assertFalse(missing.exists())
    }

    @Test
    fun readsNestedMarkerUsingFileNameOnlyWithoutDeletingTree() {
        val outputDir = tempFolder.newFolder("markers")
        val nested = File(outputDir, "nested").also { it.mkdirs() }
        val marker = File(nested, "module.apk.size").also { it.writeText("5") }

        val markers = ArtifactSizeOutputReader.read(outputDir)

        assertEquals(listOf("module.apk.size" to "5"), markers)
        assertTrue(outputDir.exists())
        assertTrue(nested.exists())
        assertEquals("5", marker.readText())
    }
}
