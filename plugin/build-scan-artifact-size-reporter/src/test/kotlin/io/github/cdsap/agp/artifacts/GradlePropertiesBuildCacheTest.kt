package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GradlePropertiesBuildCacheTest {
    @Test
    fun gradlePropertiesEnablesBuildCache() {
        val properties = readGradleProperties()
        val cachingAssignments =
            Regex("""(?m)^\s*org\.gradle\.caching\s*=\s*(\S+)\s*$""")
                .findAll(properties)
                .map { it.groupValues[1] }
                .toList()

        assertEquals(
            "Expected exactly one org.gradle.caching assignment in gradle.properties",
            1,
            cachingAssignments.size,
        )
        assertEquals("true", cachingAssignments.single())
    }

    @Test
    fun ciJobsUseSetupGradleSoBuildCacheCanBePersisted() {
        val workflow = readBuildWorkflow()
        val setupGradleUses =
            Regex("""uses:\s*gradle/actions/setup-gradle@v\d+""")
                .findAll(workflow)
                .toList()

        assertTrue(
            "Expected gradle/actions/setup-gradle in CI so build-cache entries can be persisted",
            setupGradleUses.isNotEmpty(),
        )
        assertEquals(
            "Expected setup-gradle in plugin_tests, build_check, and e2e_matrix",
            3,
            setupGradleUses.size,
        )
    }

    private fun readGradleProperties(): String {
        return locateRepoFile("gradle.properties").readText()
    }

    private fun readBuildWorkflow(): String {
        return locateRepoFile(".github/workflows/build.yaml").readText()
    }

    private fun locateRepoFile(relativePath: String): File {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, relativePath)
            if (candidate.isFile) {
                return candidate
            }
            dir = dir.parentFile ?: return@repeat
        }
        error("Could not locate $relativePath from ${System.getProperty("user.dir")}")
    }
}
