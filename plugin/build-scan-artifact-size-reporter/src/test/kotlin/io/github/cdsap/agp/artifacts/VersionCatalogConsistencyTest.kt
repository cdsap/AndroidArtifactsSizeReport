package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the two version-catalog escapes called out in
 * https://github.com/cdsap/AndroidArtifactsSizeReport/issues/31:
 * hardcoded JUnit in the plugin build, and Develocity settings/catalog drift.
 */
class VersionCatalogConsistencyTest {
    private val repoRoot: File =
        generateSequence(File(".").canonicalFile) { it.parentFile }
            .first { File(it, "gradle/libs.versions.toml").isFile }

    private val versionCatalog = File(repoRoot, "gradle/libs.versions.toml").readText()
    private val rootSettings = File(repoRoot, "settings.gradle.kts").readText()
    private val pluginBuild =
        File(repoRoot, "plugin/build-scan-artifact-size-reporter/build.gradle.kts").readText()

    @Test
    fun pluginUsesCatalogJunitInsteadOfHardcodedCoordinate() {
        assertTrue(
            "plugin build should depend on libs.junit from the version catalog",
            pluginBuild.contains("testImplementation(libs.junit)"),
        )
        assertFalse(
            "plugin build must not hardcode junit:junit coordinates",
            HARDCODED_JUNIT_COORDINATE.containsMatchIn(pluginBuild),
        )
    }

    @Test
    fun settingsDevelocityVersionMatchesVersionCatalog() {
        val catalogVersion =
            CATALOG_DEVELOCITY_VERSION.find(versionCatalog)?.groupValues?.get(1)
                ?: error("develocity version missing from gradle/libs.versions.toml")
        val settingsVersion =
            SETTINGS_DEVELOCITY_VERSION.find(rootSettings)?.groupValues?.get(1)
                ?: error("com.gradle.develocity plugin version missing from settings.gradle.kts")

        assertEquals(
            "settings.gradle.kts Develocity plugin version must stay in step with " +
                "versions.develocity in gradle/libs.versions.toml " +
                "(libs. is unavailable in the settings plugins {} block)",
            catalogVersion,
            settingsVersion,
        )
    }

    companion object {
        private val HARDCODED_JUNIT_COORDINATE =
            Regex("""testImplementation\("junit:junit:[^"]+"\)""")
        private val CATALOG_DEVELOCITY_VERSION =
            Regex("""(?m)^develocity\s*=\s*"([^"]+)"""")
        private val SETTINGS_DEVELOCITY_VERSION =
            Regex("""id\("com\.gradle\.develocity"\)\s+version\s+"([^"]+)"""")
    }
}
