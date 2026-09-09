package io.github.cdsap.agp.artifacts

import java.io.File

internal object ArtifactSizeBuildScanValuePublisher {
    fun publish(
        outputDirectory: File,
        publishValue: (name: String, value: String) -> Unit,
    ) {
        if (outputDirectory.exists()) {
            outputDirectory.walkTopDown()
                .filter { it.isFile }
                .forEach { marker ->
                    publishValue(marker.name, marker.readText())
                }
        }
        outputDirectory.deleteRecursively()
    }
}
