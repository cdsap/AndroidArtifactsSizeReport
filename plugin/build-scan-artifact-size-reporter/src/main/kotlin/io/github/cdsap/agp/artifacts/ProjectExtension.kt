package io.github.cdsap.agp.artifacts

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.Project
import java.io.File

internal fun Project.onBuildFinished(output: String) {
    val projectBuildLayout = this.layout.buildDirectory
    this.gradle.rootProject {
        val develocityConfiguration = extensions.findByType(DevelocityConfiguration::class.java)
        develocityConfiguration?.buildScan?.buildFinished {
            projectBuildLayout.get()
                .dir(output).asFileTree.files.filterIsInstance<File>()
                .forEach {
                    develocityConfiguration.buildScan.value(it.name, it.readText())
                }
            projectBuildLayout.get()
                .dir(output).asFile.deleteRecursively()
        }
    }
}

internal fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
