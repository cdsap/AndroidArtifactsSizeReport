package io.github.cdsap.agp.artifacts.tasks

import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class SizeApkTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val input: DirectoryProperty

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @TaskAction
    fun taskAction() {
        val outputDirectory = output.get()
        val outputFile = outputDirectory.asFile
        outputFile.deleteRecursively()
        outputFile.mkdirs()

        val builtArtifacts = builtArtifactsLoader.get().load(input.get())
            ?: throw RuntimeException("Cannot load APKs")

        builtArtifacts.elements.forEach { artifact ->
            if (File(artifact.outputFile).exists()) {
                val fileName = "${artifact.outputFile.split("/").last()}.size"
                outputDirectory.file(fileName).asFile.writeText(
                    File(artifact.outputFile).length().toString()
                )
            }
        }
    }
}
