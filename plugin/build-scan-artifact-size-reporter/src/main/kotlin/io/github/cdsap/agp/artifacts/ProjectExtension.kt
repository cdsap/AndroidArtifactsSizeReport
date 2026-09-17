package io.github.cdsap.agp.artifacts

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import io.github.cdsap.agp.artifacts.tasks.ArtifactSizeOutputReader
import org.gradle.api.Project

internal fun Project.onBuildFinished(output: String) {
    val projectBuildLayout = this.layout.buildDirectory
    val develocityConfiguration = extensions.getByType(DevelocityConfiguration::class.java)
    develocityConfiguration.buildScan.buildFinished {
        val outputDirectory = projectBuildLayout.get().dir(output).asFile
        ArtifactSizeOutputReader.read(outputDirectory).forEach { (name, value) ->
            develocityConfiguration.buildScan.value(name, value)
        }
    }
}

internal fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
