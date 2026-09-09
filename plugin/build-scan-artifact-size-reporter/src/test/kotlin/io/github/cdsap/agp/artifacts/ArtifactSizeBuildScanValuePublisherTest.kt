package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ArtifactSizeBuildScanValuePublisherTest {
    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun publishesMarkerFilesAsNameValuePairsAndDeletesDirectory() {
        val outputDir = tempFolder.newFolder("markers")
        File(outputDir, "app-debug.apk.size").writeText("42")
        File(outputDir, "app-release.aab.size").writeText("100")
        val published = mutableListOf<Pair<String, String>>()

        ArtifactSizeBuildScanValuePublisher.publish(outputDir) { name, value ->
            published += name to value
        }

        assertEquals(
            setOf("app-debug.apk.size" to "42", "app-release.aab.size" to "100"),
            published.toSet(),
        )
        assertFalse(outputDir.exists())
    }

    @Test
    fun publishesNestedMarkerUsingFileNameOnly() {
        val outputDir = tempFolder.newFolder("markers")
        val nested = File(outputDir, "nested").also { it.mkdirs() }
        File(nested, "module.apk.size").writeText("5")
        val published = mutableListOf<Pair<String, String>>()

        ArtifactSizeBuildScanValuePublisher.publish(outputDir) { name, value ->
            published += name to value
        }

        assertEquals(listOf("module.apk.size" to "5"), published)
        assertFalse(outputDir.exists())
    }

    @Test
    fun emptyMarkerDirectoryPublishesNothingAndIsDeleted() {
        val outputDir = tempFolder.newFolder("markers")
        val published = mutableListOf<Pair<String, String>>()

        ArtifactSizeBuildScanValuePublisher.publish(outputDir) { name, value ->
            published += name to value
        }

        assertTrue(published.isEmpty())
        assertFalse(outputDir.exists())
    }

    @Test
    fun missingMarkerDirectoryPublishesNothing() {
        val missing = File(tempFolder.root, "does-not-exist")
        val published = mutableListOf<Pair<String, String>>()

        ArtifactSizeBuildScanValuePublisher.publish(missing) { name, value ->
            published += name to value
        }

        assertTrue(published.isEmpty())
        assertFalse(missing.exists())
    }
}
