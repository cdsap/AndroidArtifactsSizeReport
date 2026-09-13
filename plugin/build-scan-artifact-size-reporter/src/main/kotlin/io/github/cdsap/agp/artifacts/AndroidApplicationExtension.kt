package io.github.cdsap.agp.artifacts

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import io.github.cdsap.agp.artifacts.tasks.SizeApkTask
import io.github.cdsap.agp.artifacts.tasks.SizeFileTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

internal fun Project.configureAndroidApplication() {
    val androidComponents =
        extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->

        val sizeApk =
            tasks.register<SizeApkTask>("sizeApk${variant.name.capitalize()}") {
                output.set(layout.buildDirectory.dir("${Output.Constants.OUTPUT}/apk/${variant.name}"))
                builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
            }

        variant.artifacts.use(sizeApk).wiredWith {
            it.input
        }.toListenTo(SingleArtifact.APK)

        val sizeBundle =
            tasks.register<SizeFileTask>("sizeBundle${variant.name.capitalize()}") {
                output.set(layout.buildDirectory.dir("${Output.Constants.OUTPUT}/aab/${variant.name}"))
            }

        variant.artifacts.use(sizeBundle).wiredWith {
            it.input
        }.toListenTo(SingleArtifact.BUNDLE)
    }
}
