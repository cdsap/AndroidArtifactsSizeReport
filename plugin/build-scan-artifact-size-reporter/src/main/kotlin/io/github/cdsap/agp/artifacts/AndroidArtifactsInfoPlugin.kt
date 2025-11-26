package io.github.cdsap.agp.artifacts

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidArtifactsInfoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val hasDevelocity =
            try {
                Class.forName("com.gradle.develocity.agent.gradle.DevelocityConfiguration")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        if (hasDevelocity) {
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
