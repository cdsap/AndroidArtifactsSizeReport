package io.github.cdsap.agp.artifacts

import io.github.cdsap.agp.artifacts.tasks.ArtifactSizeOutputReader
import java.io.File

internal object OutputValuesPublisher {
    fun publish(
        outputDirectory: File,
        publishValue: (name: String, value: String) -> Unit,
    ) {
        ArtifactSizeOutputReader.read(outputDirectory).forEach { (name, value) ->
            publishValue(name, value)
        }
    }
}
