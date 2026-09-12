package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Locks configuration cache on for the sample `app` + `mylibrary` build used by
 * `build_check`, so the published plugin's `configurationCache = true` claim is
 * exercised outside TestKit as well.
 */
class SampleBuildConfigurationCachePropertyTest {
    @Test
    fun sampleBuildEnablesConfigurationCache() {
        val properties = readGradleProperties()
        assertTrue(
            "Expected org.gradle.configuration-cache=true in sample build gradle.properties",
            properties.lines().any { line ->
                line.trim() == "org.gradle.configuration-cache=true"
            },
        )
    }

    private fun readGradleProperties(): String {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, "gradle.properties")
            if (candidate.isFile) {
                return candidate.readText()
            }
            dir = dir.parentFile ?: return@repeat
        }
        error("Could not locate gradle.properties from ${System.getProperty("user.dir")}")
    }
}
