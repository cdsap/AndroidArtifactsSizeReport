package io.github.cdsap.agp.artifacts.tasks

import java.io.File

internal object ArtifactSizeOutputReader {
    fun read(outputFiles: Iterable<File>): List<Pair<String, String>> =
        outputFiles
            .filter { it.isFile }
            .map { marker -> marker.name to marker.readText() }
}
