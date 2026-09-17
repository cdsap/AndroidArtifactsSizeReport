package io.github.cdsap.agp.artifacts.tasks

import java.io.File

internal object ArtifactSizeOutputReader {
    fun read(outputDirectory: File): List<Pair<String, String>> {
        if (!outputDirectory.exists()) {
            return emptyList()
        }
        return outputDirectory.walkTopDown()
            .filter { it.isFile }
            .map { marker -> marker.name to marker.readText() }
            .toList()
    }
}
