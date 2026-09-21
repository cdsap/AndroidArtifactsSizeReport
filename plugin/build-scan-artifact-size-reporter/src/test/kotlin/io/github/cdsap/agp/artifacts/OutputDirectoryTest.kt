package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the single output-directory boundary introduced for
 * https://github.com/cdsap/AndroidArtifactsSizeReport/issues/58.
 */
class OutputDirectoryTest {
    @Test
    fun directoryForPreservesApkAabAndAarConventions() {
        assertEquals("outputs/size/apk/debug", Output.directoryFor(ArtifactKind.APK, "debug"))
        assertEquals("outputs/size/aab/release", Output.directoryFor(ArtifactKind.AAB, "release"))
        assertEquals("outputs/size/aar/debug", Output.directoryFor(ArtifactKind.AAR, "debug"))
    }

    @Test
    fun androidExtensionsDelegatePathLayoutToOutputHelper() {
        val applicationSource =
            readRepoFile(
                "plugin/build-scan-artifact-size-reporter/src/main/kotlin/io/github/cdsap/agp/artifacts/AndroidApplicationExtension.kt",
            )
        val librarySource =
            readRepoFile(
                "plugin/build-scan-artifact-size-reporter/src/main/kotlin/io/github/cdsap/agp/artifacts/AndroidLibraryExtension.kt",
            )

        assertTrue(
            "application extension should use Output.directoryFor for APK paths",
            applicationSource.contains("Output.directoryFor(ArtifactKind.APK, variant.name)"),
        )
        assertTrue(
            "application extension should use Output.directoryFor for AAB paths",
            applicationSource.contains("Output.directoryFor(ArtifactKind.AAB, variant.name)"),
        )
        assertTrue(
            "library extension should use Output.directoryFor for AAR paths",
            librarySource.contains("Output.directoryFor(ArtifactKind.AAR, variant.name)"),
        )
        assertFalse(
            "application extension must not hardcode outputs/size path segments",
            applicationSource.contains("Output.Constants.OUTPUT"),
        )
        assertFalse(
            "library extension must not hardcode outputs/size path segments",
            librarySource.contains("Output.Constants.OUTPUT"),
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
