package io.github.cdsap.agp.artifacts.tasks

import java.io.File

internal object ArtifactSizeOutputWriter {
    fun write(
        outputDirectory: File,
        artifacts: Iterable<File>,
    ) {
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()
        ArtifactSizeMarker.fromAll(artifacts).forEach { marker ->
            File(outputDirectory, marker.fileName).writeText(marker.content)
        }
    }
}
