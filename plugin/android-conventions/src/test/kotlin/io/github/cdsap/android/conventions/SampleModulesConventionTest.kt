package io.github.cdsap.android.conventions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Prevents the sample modules from re-introducing duplicated Android config that
 * previously drifted (compileSdk 35 vs 34).
 */
class SampleModulesConventionTest {
    @Test
    fun appAndMylibraryApplySharedConventionsWithoutLocalCompileSdk() {
        val root = findRepoRoot()
        val app = File(root, "app/build.gradle.kts").readText()
        val library = File(root, "mylibrary/build.gradle.kts").readText()

        assertTrue(
            "app must apply android-conventions",
            app.contains("""id("android-conventions")"""),
        )
        assertTrue(
            "mylibrary must apply android-conventions",
            library.contains("""id("android-conventions")"""),
        )
        assertFalse(
            "app must not set compileSdk locally (belongs in android-conventions)",
            app.contains("compileSdk"),
        )
        assertFalse(
            "mylibrary must not set compileSdk locally (belongs in android-conventions)",
            library.contains("compileSdk"),
        )
        assertFalse(
            "app must not redeclare shared androidx.core dependency",
            app.contains("libs.androidx.core"),
        )
        assertFalse(
            "mylibrary must not redeclare shared androidx.core dependency",
            library.contains("libs.androidx.core"),
        )
        assertFalse(
            "app must not re-apply kotlin.android (owned by android-conventions)",
            app.contains("kotlin.android"),
        )
        assertFalse(
            "mylibrary must not re-apply kotlin.android (owned by android-conventions)",
            library.contains("kotlin.android"),
        )
    }

    @Test
    fun conventionPluginPinsSingleCompileSdk() {
        val root = findRepoRoot()
        val plugin =
            File(
                root,
                "plugin/android-conventions/src/main/kotlin/io/github/cdsap/android/conventions/AndroidConventionsPlugin.kt",
            ).readText()

        assertTrue(
            "convention plugin must define COMPILE_SDK = 35",
            plugin.contains("COMPILE_SDK = 35"),
        )
        assertTrue(
            "convention plugin must apply compileSdkVersion from the shared constant",
            plugin.contains("compileSdkVersion(COMPILE_SDK)"),
        )
    }

    private fun findRepoRoot(): File {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            if (File(dir, "app/build.gradle.kts").isFile &&
                File(dir, "mylibrary/build.gradle.kts").isFile
            ) {
                return dir
            }
            dir = dir.parentFile ?: error("Could not locate repo root from ${System.getProperty("user.dir")}")
        }
        error("Could not locate repo root from ${System.getProperty("user.dir")}")
    }
}
