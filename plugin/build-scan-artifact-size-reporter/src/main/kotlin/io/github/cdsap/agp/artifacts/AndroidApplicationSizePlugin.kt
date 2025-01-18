package io.github.cdsap.agp.artifacts

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import io.github.cdsap.agp.artifacts.tasks.SizeApkTask
import io.github.cdsap.agp.artifacts.tasks.SizeFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class AndroidApplicationSizePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.plugins.withType(AppPlugin::class.java) {

            val androidComponents =
                target.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->

                val getApk= target.tasks.register<SizeApkTask>("getApk${variant.name}") {
                    output.set(target.layout.buildDirectory.dir("${Output.Constants.OUTPUT}/apk/${variant.name}"))
                    builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
                }

                variant.artifacts.use(getApk).wiredWith {
                    it.input
                }.toListenTo(SingleArtifact.APK)

                val getBundle = target.tasks.register<SizeFileTask>("getBundle${variant.name}") {
                    output.set(target.layout.buildDirectory.dir("${Output.Constants.OUTPUT}/aab/${variant.name}"))
                }

                variant.artifacts.use(getBundle).wiredWith {
                    it.input
                }.toListenTo(SingleArtifact.BUNDLE)
            }
        }
        target.onBuildFinished(Output.Constants.OUTPUT)
    }
}
