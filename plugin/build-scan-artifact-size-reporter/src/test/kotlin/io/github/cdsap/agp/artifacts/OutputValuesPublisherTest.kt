package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class OutputValuesPublisherTest {
    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun publishesMarkerFilesAsNameValuePairsAndLeavesDirectoryIntact() {
        val outputDir = tempFolder.newFolder("markers")
        val apkMarker = File(outputDir, "app-debug.apk.size").also { it.writeText("42") }
        val aabMarker = File(outputDir, "app-release.aab.size").also { it.writeText("100") }
        val aarMarker = File(outputDir, "mylibrary-release.aar.size").also { it.writeText("7") }
        val published = mutableListOf<Pair<String, String>>()

        OutputValuesPublisher.publish(outputDir) { name, value ->
            published += name to value
        }

        assertEquals(
            setOf(
                "app-debug.apk.size" to "42",
                "app-release.aab.size" to "100",
                "mylibrary-release.aar.size" to "7",
            ),
            published.toSet(),
        )
        assertTrue(outputDir.exists())
        assertEquals("42", apkMarker.readText())
        assertEquals("100", aabMarker.readText())
        assertEquals("7", aarMarker.readText())
    }

    @Test
    fun publishesNestedMarkerUsingFileNameOnlyWithoutDeletingTree() {
        val outputDir = tempFolder.newFolder("markers")
        val nested = File(outputDir, "nested").also { it.mkdirs() }
        val marker = File(nested, "module.apk.size").also { it.writeText("5") }
        val published = mutableListOf<Pair<String, String>>()

        OutputValuesPublisher.publish(outputDir) { name, value ->
            published += name to value
        }

        assertEquals(listOf("module.apk.size" to "5"), published)
        assertTrue(outputDir.exists())
        assertTrue(nested.exists())
        assertEquals("5", marker.readText())
    }

    @Test
    fun emptyMarkerDirectoryPublishesNothingAndRemains() {
        val outputDir = tempFolder.newFolder("markers")
        val published = mutableListOf<Pair<String, String>>()

        OutputValuesPublisher.publish(outputDir) { name, value ->
            published += name to value
        }

        assertTrue(published.isEmpty())
        assertTrue(outputDir.exists())
    }

    @Test
    fun missingMarkerDirectoryPublishesNothing() {
        val missing = File(tempFolder.root, "does-not-exist")
        val published = mutableListOf<Pair<String, String>>()

        OutputValuesPublisher.publish(missing) { name, value ->
            published += name to value
        }

        assertTrue(published.isEmpty())
        assertFalse(missing.exists())
    }
}
