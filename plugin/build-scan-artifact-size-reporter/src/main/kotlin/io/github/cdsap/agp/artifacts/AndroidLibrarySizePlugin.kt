package io.github.cdsap.agp.artifacts

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryPlugin
import io.github.cdsap.agp.artifacts.tasks.SizeFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class AndroidLibrarySizePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.plugins.withType(LibraryPlugin::class.java) {

            val androidComponents =
                target.extensions.getByType(LibraryAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->

                val getAar = target.tasks.register<SizeFileTask>("getAarInfo${variant.name}") {
                    output.set(target.layout.buildDirectory.dir("${Output.Constants.OUTPUT}/aar/${variant.name}"))
                }
               variant.artifacts.use(getAar).wiredWith { it.input }.toListenTo(SingleArtifact.AAR)

            }
        }
        target.onBuildFinished("${Output.Constants.OUTPUT}/aar/")
    }
}
