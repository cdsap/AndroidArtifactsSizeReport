package io.github.cdsap.agp.artifacts

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidArtifactsInfoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Develocity is applied as a settings plugin, so it never appears on
        // project.pluginManager — withPlugin("com.gradle.develocity") would never run.
        // The settings plugin registers a "develocity" extension on each project before
        // project plugins apply. Detect that extension (is it applied?) rather than a
        // classpath reflection probe (is the class present?).
        if (project.extensions.findByName(DEVELOCITY_EXTENSION_NAME) == null) {
            return
        }
        with(project) {
            pluginManager.withPlugin("com.android.application") {
                configureAndroidApplication()
            }
            pluginManager.withPlugin("com.android.library") {
                configureAndroidLibrary()
            }
            onBuildFinished(Output.Constants.OUTPUT)
        }
    }

    internal companion object {
        const val DEVELOCITY_EXTENSION_NAME = "develocity"
    }
}
