package io.github.cdsap.agp.artifacts.tasks

import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.DisableCachingByDefault
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.Method

class SizeTaskCachingAnnotationTest {
    @Test
    fun sizeApkTaskDisablesCachingByDefaultWithDocumentedReason() {
        assertDisableCachingBecause(SizeApkTask::class.java)
    }

    @Test
    fun sizeFileTaskDisablesCachingByDefaultWithDocumentedReason() {
        assertDisableCachingBecause(SizeFileTask::class.java)
    }

    @Test
    fun sizeFileTaskUsesNonePathSensitivityForFileInput() {
        val input = inputGetter(SizeFileTask::class.java)
        assertNotNull("$input should declare @InputFile", input.getAnnotation(InputFile::class.java))
        assertNull("$input should not declare @InputDirectory", input.getAnnotation(InputDirectory::class.java))
        assertEquals(PathSensitivity.NONE, pathSensitivity(input))
    }

    @Test
    fun sizeApkTaskUsesRelativePathSensitivityForDirectoryInput() {
        val input = inputGetter(SizeApkTask::class.java)
        assertNotNull("$input should declare @InputDirectory", input.getAnnotation(InputDirectory::class.java))
        assertNull("$input should not declare @InputFile", input.getAnnotation(InputFile::class.java))
        assertEquals(PathSensitivity.RELATIVE, pathSensitivity(input))
    }

    private fun assertDisableCachingBecause(taskType: Class<*>) {
        val annotation = taskType.getAnnotation(DisableCachingByDefault::class.java)
        assertNotNull("$taskType should declare @DisableCachingByDefault", annotation)
        assertEquals(
            "Trivially cheap; only writes a file length",
            annotation!!.because,
        )
    }

    private fun inputGetter(taskType: Class<*>): Method = taskType.getMethod("getInput")

    private fun pathSensitivity(getter: Method): PathSensitivity {
        val annotation = getter.getAnnotation(PathSensitive::class.java)
        assertNotNull("$getter should declare @PathSensitive", annotation)
        return annotation!!.value
    }
}
