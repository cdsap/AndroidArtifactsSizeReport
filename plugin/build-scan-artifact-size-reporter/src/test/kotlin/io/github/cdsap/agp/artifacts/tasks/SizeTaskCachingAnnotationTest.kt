package io.github.cdsap.agp.artifacts.tasks

import org.gradle.work.DisableCachingByDefault
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SizeTaskCachingAnnotationTest {
    @Test
    fun sizeApkTaskDisablesCachingByDefaultWithDocumentedReason() {
        assertDisableCachingBecause(SizeApkTask::class.java)
    }

    @Test
    fun sizeFileTaskDisablesCachingByDefaultWithDocumentedReason() {
        assertDisableCachingBecause(SizeFileTask::class.java)
    }

    private fun assertDisableCachingBecause(taskType: Class<*>) {
        val annotation = taskType.getAnnotation(DisableCachingByDefault::class.java)
        assertNotNull("$taskType should declare @DisableCachingByDefault", annotation)
        assertEquals(
            "Trivially cheap; only writes a file length",
            annotation!!.because,
        )
    }
}
