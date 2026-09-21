package io.github.cdsap.agp.artifacts

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import io.github.cdsap.agp.artifacts.tasks.SizeFileTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

internal fun Project.configureAndroidLibrary() {
    val androidComponents =
        extensions.getByType(LibraryAndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->

        val getAar =
            tasks.register<SizeFileTask>("sizeAar${variant.name.capitalize()}") {
                group = "reporting"
                description =
                    "Records the size of the ${variant.name} AAR for publication as a Build Scan custom value."
                output.set(layout.buildDirectory.dir(Output.directoryFor(ArtifactKind.AAR, variant.name)))
            }
        variant.artifacts.use(getAar).wiredWith { it.input }.toListenTo(SingleArtifact.AAR)
    }
}
