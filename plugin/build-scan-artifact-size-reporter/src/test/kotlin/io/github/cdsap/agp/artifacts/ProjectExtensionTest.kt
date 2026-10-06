package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProjectExtensionTest {
    @Test
    fun onBuildFinishedReadsPublishesAndCleansUpOutput() {
        val extensionSource = readRepoFile(
            "plugin/build-scan-artifact-size-reporter/src/main/kotlin/io/github/cdsap/agp/artifacts/ProjectExtension.kt",
        )

        assertTrue(
            "expected ArtifactSizeOutputReader wiring",
            extensionSource.contains("ArtifactSizeOutputReader.read"),
        )
        assertFalse(
            "build-finished callback must not read marker files directly",
            extensionSource.contains("readText"),
        )
        assertTrue(
            "build-finished callback must delete the output directory after publishing",
            extensionSource.contains("outputDirectory.deleteRecursively()"),
        )
    }

    private fun readRepoFile(relativePath: String): String {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, relativePath)
            if (candidate.isFile) {
                return candidate.readText()
            }
            dir = dir.parentFile ?: return@repeat
        }
        error("Could not locate $relativePath from ${System.getProperty("user.dir")}")
    }
}
