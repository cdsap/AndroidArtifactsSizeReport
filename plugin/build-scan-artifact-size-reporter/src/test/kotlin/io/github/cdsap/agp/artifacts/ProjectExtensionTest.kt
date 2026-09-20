package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProjectExtensionTest {
    @Test
    fun onBuildFinishedDelegatesFilesystemHandlingToPublisher() {
        val extensionSource = readRepoFile(
            "plugin/build-scan-artifact-size-reporter/src/main/kotlin/io/github/cdsap/agp/artifacts/ProjectExtension.kt",
        )

        assertTrue(
            "expected OutputValuesPublisher.publish wiring",
            extensionSource.contains("OutputValuesPublisher.publish"),
        )
        assertFalse(
            "build-finished callback must not walk the output tree",
            extensionSource.contains("walkTopDown"),
        )
        assertFalse(
            "build-finished callback must not read marker files directly",
            extensionSource.contains("readText"),
        )
        assertFalse(
            "build-finished callback must not delete the output directory",
            extensionSource.contains("deleteRecursively"),
        )
        assertFalse(
            "build-finished callback must not call ArtifactSizeOutputReader directly",
            extensionSource.contains("ArtifactSizeOutputReader"),
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
