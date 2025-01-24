package io.github.cdsap.agp.artifacts

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidArtifactsInfoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val rootProject = project.gradle.rootProject
        if (rootProject.extensions.findByType(DevelocityConfiguration::class.java) != null) {
            with(project) {
                plugins.withType(AppPlugin::class.java) {
                    configureAndroidApplication()
                }
                plugins.withType(LibraryPlugin::class.java) {
                    configureAndroidLibrary()
                }
                onBuildFinished(Output.Constants.OUTPUT)
            }
        }
    }
}
