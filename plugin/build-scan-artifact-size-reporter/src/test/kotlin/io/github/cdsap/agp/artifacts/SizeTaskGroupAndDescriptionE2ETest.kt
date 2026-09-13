package io.github.cdsap.agp.artifacts

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Ensures sizeApk/sizeBundle/sizeAar tasks are discoverable via `./gradlew tasks`
 * under the Reporting group with descriptions (Gradle hides ungrouped tasks).
 *
 * Uses includeBuild against the plugin project (same resolution path as the sample
 * app) so Develocity/AGP peers resolve as they do for consumers. TestKit's
 * withPluginClasspath() omits compileOnly peers and skips task registration.
 */
class SizeTaskGroupAndDescriptionE2ETest {
    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    @Test
    fun sizeApkAndSizeBundleTasksAppearUnderReportingGroup() {
        createKotlinClass("app")
        createAppModule()
        createBuildFiles(includeLibrary = false)

        val output = runTasks(":app:tasks")

        assertTrue(
            "sizeApkDebug should appear under Reporting tasks",
            output.contains("Reporting tasks") &&
                output.contains(
                    "sizeApkDebug - Records the size of the debug APK for publication as a Build Scan custom value.",
                ),
        )
        assertTrue(
            "sizeBundleDebug should appear under Reporting tasks",
            output.contains(
                "sizeBundleDebug - Records the size of the debug Bundle for publication as a Build Scan custom value.",
            ),
        )
    }

    @Test
    fun sizeAarTaskAppearsUnderReportingGroup() {
        createKotlinClass("mylibrary")
        createLibraryModule()
        createBuildFiles(includeLibrary = true)

        val output = runTasks(":mylibrary:tasks")

        assertTrue(
            "sizeAarDebug should appear under Reporting tasks",
            output.contains("Reporting tasks") &&
                output.contains(
                    "sizeAarDebug - Records the size of the debug AAR for publication as a Build Scan custom value.",
                ),
        )
    }

    private fun runTasks(taskPath: String): String {
        val result =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir.root)
                .withGradleVersion("9.7.1")
                .withDebug(false)
                .withArguments(taskPath)
                .forwardOutput()
                .build()
        return result.output
    }

    private fun pluginProjectDir(): File {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, "plugin")
            if (File(candidate, "build-scan-artifact-size-reporter").isDirectory) {
                return candidate
            }
            dir = dir.parentFile ?: return@repeat
        }
        error("Could not locate plugin/ from ${System.getProperty("user.dir")}")
    }

    private fun createBuildFiles(includeLibrary: Boolean) {
        testProjectDir.newFile("build.gradle.kts").appendText(
            """
            repositories {
                mavenCentral()
            }
            """.trimIndent(),
        )

        testProjectDir.newFile("gradle.properties").appendText(
            """
            org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
            android.useAndroidX=true
            """.trimIndent(),
        )

        val includes =
            if (includeLibrary) {
                """include(":mylibrary")"""
            } else {
                """include(":app")"""
            }
        val pluginDir = pluginProjectDir().absolutePath.replace("\\", "/")

        testProjectDir.newFile("settings.gradle.kts").appendText(
            """
            pluginManagement {
                includeBuild("$pluginDir")
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            buildscript {
                repositories {
                    google()
                    mavenCentral()
                }
                dependencies {
                    classpath("com.android.tools.build:gradle:9.4.0")
                }
            }
            plugins {
                id("com.gradle.develocity") version "4.2.2"
            }
            develocity {
                server = "https://ge.solutions-team.gradle.com/"
            }

            $includes
            """.trimIndent(),
        )
    }

    private fun createAppModule() {
        testProjectDir.newFile("app/build.gradle.kts").appendText(
            """
            plugins {
                id("com.android.application")
                id("io.github.cdsap.android-artifacts-size-report")
            }

            repositories {
                mavenCentral()
                google()
            }

            android {
                namespace = "com.example.myapplication"
                compileSdk = 37

                defaultConfig {
                    applicationId = "com.example.myapplication"
                    minSdk = 24
                    targetSdk = 35
                    versionCode = 1
                    versionName = "1.0"
                }
            }
            """.trimIndent(),
        )

        testProjectDir.newFile("app/src/main/AndroidManifest.xml").appendText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:tools="http://schemas.android.com/tools">

                <application
                    android:allowBackup="true"
                    android:label="2"
                    android:supportsRtl="true"
                    tools:targetApi="31" />

            </manifest>
            """.trimIndent(),
        )
    }

    private fun createLibraryModule() {
        testProjectDir.newFile("mylibrary/build.gradle.kts").appendText(
            """
            plugins {
                id("com.android.library")
                id("io.github.cdsap.android-artifacts-size-report")
            }

            repositories {
                mavenCentral()
                google()
            }

            android {
                namespace = "com.example.mylibrary"
                compileSdk = 37

                defaultConfig {
                    minSdk = 24
                }
            }
            """.trimIndent(),
        )

        testProjectDir.newFile("mylibrary/src/main/AndroidManifest.xml").appendText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" />
            """.trimIndent(),
        )
    }

    private fun createKotlinClass(module: String) {
        testProjectDir.newFolder("$module/src/main/kotlin/com/example")
        testProjectDir.newFile("$module/src/main/kotlin/com/example/Hello.kt").appendText(
            """
            package com.example
            class Hello() {
                fun print() {
                    println("hello")
                }
            }
            """.trimIndent(),
        )
    }
}
