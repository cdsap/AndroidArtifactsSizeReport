package io.github.cdsap.agp.artifacts.tasks

import java.io.File

internal data class ArtifactSizeMarker(
    val fileName: String,
    val content: String,
) {
    companion object {
        fun from(artifact: File): ArtifactSizeMarker? {
            if (!artifact.exists()) {
                return null
            }
            return ArtifactSizeMarker(
                fileName = "${artifact.name}.size",
                content = artifact.length().toString(),
            )
        }

        fun fromAll(artifacts: Iterable<File>): List<ArtifactSizeMarker> =
            artifacts.mapNotNull { from(it) }
    }
}
