package io.github.cdsap.agp.artifacts.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class SizeFileTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val input: RegularFileProperty

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @TaskAction
    fun taskAction() {
        val outputDirectory = output.get()
        val outputFile = outputDirectory.asFile
        outputFile.deleteRecursively()
        outputFile.mkdirs()
        val file = input.get().asFile
        if (file.exists()) {
            val fileName = "${file.name.split("/").last()}.size"
            outputDirectory.file(fileName).asFile.writeText(
                file.length().toString()
            )
        }
    }
}
