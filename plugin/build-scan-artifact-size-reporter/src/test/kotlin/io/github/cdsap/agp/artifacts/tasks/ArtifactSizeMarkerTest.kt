package io.github.cdsap.agp.artifacts.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ArtifactSizeMarkerTest {
    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun namesMarkerAfterArtifactFileNameWithSizeSuffix() {
        val artifact = tempFolder.newFile("app-debug.apk").also { it.writeBytes(ByteArray(1)) }

        val marker = ArtifactSizeMarker.from(artifact)

        assertEquals("app-debug.apk.size", marker!!.fileName)
    }

    @Test
    fun calculatesContentAsByteLengthString() {
        val artifact = tempFolder.newFile("app-release.aab").also { it.writeBytes(ByteArray(42) { 1 }) }

        val marker = ArtifactSizeMarker.from(artifact)

        assertEquals("42", marker!!.content)
    }

    @Test
    fun usesOnlyFileNameWhenArtifactPathContainsDirectories() {
        val nested = tempFolder.newFolder("build", "outputs")
        val artifact = File(nested, "module.apk").also { it.writeBytes(ByteArray(5)) }

        val marker = ArtifactSizeMarker.from(artifact)

        assertEquals("module.apk.size", marker!!.fileName)
        assertEquals("5", marker.content)
    }

    @Test
    fun returnsNullForMissingArtifactFile() {
        val missing = File(tempFolder.root, "absent.apk")

        assertNull(ArtifactSizeMarker.from(missing))
    }

    @Test
    fun fromAllSkipsMissingArtifactsAndPreservesOrder() {
        val first = tempFolder.newFile("first.apk").also { it.writeBytes(ByteArray(3)) }
        val missing = File(tempFolder.root, "missing.apk")
        val second = tempFolder.newFile("second.aab").also { it.writeBytes(ByteArray(9)) }

        val markers = ArtifactSizeMarker.fromAll(listOf(first, missing, second))

        assertEquals(
            listOf(
                ArtifactSizeMarker("first.apk.size", "3"),
                ArtifactSizeMarker("second.aab.size", "9"),
            ),
            markers,
        )
    }
}
