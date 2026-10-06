package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PluginReleaseConfigurationTest {
    private val repoRoot =
        generateSequence(File(".").canonicalFile) { it.parentFile }
            .first { File(it, "gradle/libs.versions.toml").isFile }

    private val workflow = File(repoRoot, ".github/workflows/release-plugin.yaml").readText()
    private val pluginBuild =
        File(repoRoot, "plugin/build-scan-artifact-size-reporter/build.gradle.kts").readText()

    @Test
    fun releasePublishesOnlyThePluginAfterValidation() {
        assertTrue(workflow.contains("types: [published]"))
        assertTrue(workflow.contains("ref: \${{ github.event.release.tag_name }}"))
        assertTrue(workflow.contains("java-version: '21'"))
        assertTrue(
            workflow.contains(
                ":build-scan-artifact-size-reporter:publishPlugins --validate-only",
            ),
        )
        assertTrue(
            workflow.contains(
                ":build-scan-artifact-size-reporter:publishPlugins --no-daemon",
            ),
        )
        assertTrue(workflow.contains("GRADLE_PUBLISH_KEY"))
        assertTrue(workflow.contains("GRADLE_PUBLISH_SECRET"))
        assertTrue(workflow.contains("RELEASE_TAG"))
        assertTrue(workflow.contains("*SNAPSHOT*"))
        assertTrue(workflow.contains("does not match plugin version"))
    }

    @Test
    fun pluginUsesPluginPortalPublicationConfiguration() {
        assertTrue(pluginBuild.contains("alias(libs.plugins.gradle.publish)"))
        assertTrue(
            pluginBuild.contains("id = \"io.github.cdsap.android-artifacts-size-report\""),
        )
        assertTrue(pluginBuild.contains("group = \"io.github.cdsap\""))
    }
}
