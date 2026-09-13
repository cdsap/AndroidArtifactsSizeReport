package io.github.cdsap.android.conventions

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Behavioral coverage: application and library modules that apply android-conventions
 * both receive the same compileSdk from the shared plugin.
 */
class AndroidConventionsPluginTest {
    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    @Test
    fun applicationAndLibraryShareCompileSdkFromConventions() {
        createProject()

        val result =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(":app:printCompileSdk", ":mylibrary:printCompileSdk")
                .withGradleVersion("9.7.1")
                .build()

        assertTrue(
            "app should report shared compileSdk",
            result.output.contains("app=android-35"),
        )
        assertTrue(
            "mylibrary should report shared compileSdk",
            result.output.contains("mylibrary=android-35"),
        )
    }

    private fun createProject() {
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                }
            }
            include(":app")
            include(":mylibrary")
            """.trimIndent(),
        )

        testProjectDir.newFolder("gradle")
        File(findRepoRoot(), "gradle/libs.versions.toml")
            .copyTo(File(testProjectDir.root, "gradle/libs.versions.toml"))

        testProjectDir.newFile("gradle.properties").writeText(
            """
            org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
            android.useAndroidX=true
            """.trimIndent(),
        )

        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                id("com.android.application") version "8.8.0" apply false
                id("com.android.library") version "8.8.0" apply false
            }
            """.trimIndent(),
        )

        createAndroidModule(
            name = "app",
            pluginId = "com.android.application",
            androidBlock =
                """
                namespace = "com.example.app"
                defaultConfig {
                    applicationId = "com.example.app"
                    minSdk = 24
                    targetSdk = 35
                }
                """.trimIndent(),
        )
        createAndroidModule(
            name = "mylibrary",
            pluginId = "com.android.library",
            androidBlock =
                """
                namespace = "com.example.lib"
                defaultConfig {
                    minSdk = 24
                }
                """.trimIndent(),
        )
    }

    private fun createAndroidModule(
        name: String,
        pluginId: String,
        androidBlock: String,
    ) {
        val moduleDir = testProjectDir.newFolder(name)
        File(moduleDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("$pluginId")
                id("android-conventions")
            }

            android {
                $androidBlock
            }

            tasks.register("printCompileSdk") {
                doLast {
                    val android =
                        project.extensions.getByName("android") as com.android.build.gradle.BaseExtension
                    println("$name=" + android.compileSdkVersion)
                }
            }
            """.trimIndent(),
        )

        val manifestDir = File(moduleDir, "src/main").also { it.mkdirs() }
        File(manifestDir, "AndroidManifest.xml").writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" />
            """.trimIndent(),
        )
    }

    private fun findRepoRoot(): File {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, "gradle/libs.versions.toml")
            if (candidate.isFile) {
                return dir
            }
            dir = dir.parentFile ?: error("Could not locate repo root from ${System.getProperty("user.dir")}")
        }
        error("Could not locate repo root from ${System.getProperty("user.dir")}")
    }
}
