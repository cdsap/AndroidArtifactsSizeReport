package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BuildWorkflowProcessWatcherTest {
    @Test
    fun gradleBuildJobsUseBuildProcessWatcher() {
        val workflow = readBuildWorkflow()
        val watcherUses =
            Regex("""uses:\s*cdsap/build-process-watcher@v[\d.]+""")
                .findAll(workflow)
                .toList()

        assertEquals(
            "Expected build-process-watcher in plugin_tests, build_check, and e2e_matrix",
            3,
            watcherUses.size,
        )
        assertTrue(
            "Expected build-process-watcher action in GHA gradle build workflow",
            workflow.contains("uses: cdsap/build-process-watcher@v0.6.2"),
        )
        assertTrue(
            "Expected remote_monitoring enabled for build-process-watcher",
            workflow.contains("remote_monitoring: 'true'"),
        )
        assertTrue(
            "Expected BigQuery export enabled for build-process-watcher",
            workflow.contains("export_to_bigquery: 'true'"),
        )

        val pluginTestsWatcher =
            workflow.indexOf("uses: cdsap/build-process-watcher@v0.6.2")
        val pluginTestsGradle =
            workflow.indexOf("./gradlew -p plugin :build-scan-artifact-size-reporter:test")
        val buildCheckGradle = workflow.indexOf("./gradlew build")
        val e2eGradle = workflow.lastIndexOf("./gradlew build")
        val lastWatcher = workflow.lastIndexOf("uses: cdsap/build-process-watcher@v0.6.2")

        assertTrue("build-process-watcher step missing", pluginTestsWatcher >= 0)
        assertTrue("plugin tests gradle step missing", pluginTestsGradle >= 0)
        assertTrue("build_check gradle step missing", buildCheckGradle >= 0)
        assertTrue("e2e_matrix gradle step missing", e2eGradle >= 0)
        assertTrue(
            "build-process-watcher must run before plugin tests",
            pluginTestsWatcher < pluginTestsGradle,
        )
        assertTrue(
            "build-process-watcher must run before ./gradlew build in build_check",
            workflow.indexOf("uses: cdsap/build-process-watcher@v0.6.2", pluginTestsWatcher + 1) <
                buildCheckGradle,
        )
        assertTrue(
            "build-process-watcher must run before ./gradlew build in e2e_matrix",
            lastWatcher < e2eGradle,
        )
    }

    private fun readBuildWorkflow(): String {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, ".github/workflows/build.yaml")
            if (candidate.isFile) {
                return candidate.readText()
            }
            dir = dir.parentFile ?: return@repeat
        }
        error("Could not locate .github/workflows/build.yaml from ${System.getProperty("user.dir")}")
    }
}
