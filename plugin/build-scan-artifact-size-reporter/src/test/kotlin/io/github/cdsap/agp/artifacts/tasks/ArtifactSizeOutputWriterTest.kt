package io.github.cdsap.agp.artifacts.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ArtifactSizeOutputWriterTest {
    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun writesMarkerNamedAfterArtifactWithByteLength() {
        val outputDir = tempFolder.newFolder("output")
        val artifact = tempFolder.newFile("app-debug.apk")
        artifact.writeBytes(ByteArray(42) { 1 })

        ArtifactSizeOutputWriter.write(outputDir, listOf(artifact))

        val marker = File(outputDir, "app-debug.apk.size")
        assertTrue(marker.exists())
        assertEquals("42", marker.readText())
    }

    @Test
    fun writesMarkersForMultipleArtifacts() {
        val outputDir = tempFolder.newFolder("output")
        val apk = tempFolder.newFile("app-debug.apk").also { it.writeBytes(ByteArray(10)) }
        val aab = tempFolder.newFile("app-release.aab").also { it.writeBytes(ByteArray(25)) }

        ArtifactSizeOutputWriter.write(outputDir, listOf(apk, aab))

        assertEquals("10", File(outputDir, "app-debug.apk.size").readText())
        assertEquals("25", File(outputDir, "app-release.aab.size").readText())
    }

    @Test
    fun skipsMissingArtifactFiles() {
        val outputDir = tempFolder.newFolder("output")
        val existing = tempFolder.newFile("present.apk").also { it.writeBytes(ByteArray(7)) }
        val missing = File(tempFolder.root, "absent.apk")

        ArtifactSizeOutputWriter.write(outputDir, listOf(existing, missing))

        assertTrue(File(outputDir, "present.apk.size").exists())
        assertFalse(File(outputDir, "absent.apk.size").exists())
    }

    @Test
    fun deletesAndRecreatesOutputDirectoryBeforeWriting() {
        val outputDir = tempFolder.newFolder("output")
        val stale = File(outputDir, "stale.size").also { it.writeText("old") }
        val nested = File(outputDir, "nested").also { it.mkdirs() }
        File(nested, "leftover.txt").writeText("gone")
        val artifact = tempFolder.newFile("fresh.apk").also { it.writeBytes(ByteArray(3)) }

        ArtifactSizeOutputWriter.write(outputDir, listOf(artifact))

        assertFalse(stale.exists())
        assertFalse(nested.exists())
        assertEquals("3", File(outputDir, "fresh.apk.size").readText())
    }

    @Test
    fun usesArtifactFileNameForMarkerEvenWhenPathContainsDirectories() {
        val nested = tempFolder.newFolder("build", "outputs")
        val artifact = File(nested, "module.apk").also { it.writeBytes(ByteArray(5)) }
        val outputDir = tempFolder.newFolder("output")

        ArtifactSizeOutputWriter.write(outputDir, listOf(artifact))

        assertEquals("5", File(outputDir, "module.apk.size").readText())
    }
}
