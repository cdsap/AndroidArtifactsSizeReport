package io.github.cdsap.agp.artifacts

import java.io.File

internal object ArtifactSizeBuildScanReporter {
    fun report(
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
    }
}
