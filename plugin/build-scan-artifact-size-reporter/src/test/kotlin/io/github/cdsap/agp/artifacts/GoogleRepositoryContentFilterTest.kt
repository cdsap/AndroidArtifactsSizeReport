package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GoogleRepositoryContentFilterTest {
    @Test
    fun rootAndPluginSettingsFilterEveryGoogleRepository() {
        val rootSettings = locate("settings.gradle.kts")
        val pluginSettings = locate("plugin/settings.gradle.kts")

        assertGoogleRepositoriesAreFiltered(rootSettings)
        assertGoogleRepositoriesAreFiltered(pluginSettings)
    }

    private fun assertGoogleRepositoriesAreFiltered(settingsFile: File) {
        val text = settingsFile.readText()
        val relative = settingsFile.name
            .let { if (settingsFile.parentFile?.name == "plugin") "plugin/$it" else it }

        assertFalse(
            "$relative must not declare unfiltered google()",
            Regex("""google\s*\(\s*\)""").containsMatchIn(text),
        )

        val googleBlocks =
            Regex("""google\s*\{([^{}]*(?:\{[^{}]*\}[^{}]*)*)\}""", RegexOption.DOT_MATCHES_ALL)
                .findAll(text)
                .map { it.groupValues[1] }
                .toList()

        assertTrue(
            "$relative must declare at least one google { } repository block",
            googleBlocks.isNotEmpty(),
        )

        googleBlocks.forEachIndexed { index, block ->
            assertTrue(
                "$relative google block #$index must use content filtering",
                block.contains("content"),
            )
            for (group in EXPECTED_GROUPS) {
                assertTrue(
                    "$relative google block #$index must includeGroupByRegex(\"$group\")",
                    block.contains("""includeGroupByRegex("$group")"""),
                )
            }
        }
    }

    private fun locate(relativePath: String): File {
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

    companion object {
        private val EXPECTED_GROUPS =
            listOf(
                """com\\.android.*""",
                """com\\.google.*""",
                """androidx.*""",
            )
    }
}
