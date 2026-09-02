package io.github.cdsap.agp.artifacts.tasks

import java.io.File

internal object ArtifactSizeOutputWriter {
    fun write(
        outputDirectory: File,
        artifacts: Iterable<File>,
    ) {
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()
        artifacts.forEach { artifact ->
            if (artifact.exists()) {
                val markerName = "${artifact.name}.size"
                File(outputDirectory, markerName).writeText(artifact.length().toString())
            }
        }
    }
}
