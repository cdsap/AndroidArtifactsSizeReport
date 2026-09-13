package io.github.cdsap.agp.artifacts

import org.gradle.api.UnknownDomainObjectException
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DevelocityDetectionTest {
    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    @Test
    fun onBuildFinishedFailsLoudlyWhenDevelocityExtensionIsMissing() {
        val project = ProjectBuilder.builder().build()

        try {
            project.onBuildFinished(Output.Constants.OUTPUT)
            fail("Expected UnknownDomainObjectException when Develocity extension is absent")
        } catch (_: UnknownDomainObjectException) {
            // getByType must fail loudly instead of silently no-opping
        }
    }

    @Test
    fun pluginApplySucceedsWithoutDevelocityEvenWhenClassIsOnClasspath() {
        val project = ProjectBuilder.builder().build()

        // DevelocityConfiguration is on the test classpath (a Class.forName probe would
        // return true), but the extension is absent — detection must require apply.
        project.plugins.apply(AndroidArtifactsInfoPlugin::class.java)

        assertNull(project.extensions.findByName(AndroidArtifactsInfoPlugin.DEVELOCITY_EXTENSION_NAME))
    }

    @Test
    fun sizeTasksAreRegisteredWhenDevelocitySettingsPluginIsPresent() {
        createProjectWithOptionalDevelocity(includeDevelocity = true)

        val result =
            gradleRunner()
                .withArguments(":app:tasks", "--all", "-q")
                .build()

        assertTrue(
            "expected sizeApkDebug when Develocity extension is present:\n${result.output}",
            result.output.contains("sizeApkDebug"),
        )
    }

    @Test
    fun sizeTasksAreNotRegisteredWhenDevelocityIsAbsent() {
        createProjectWithOptionalDevelocity(includeDevelocity = false)

        val result =
            gradleRunner()
                .withArguments(":app:tasks", "--all", "-q")
                .build()

        assertFalse(
            "sizeApkDebug must not be registered without Develocity:\n${result.output}",
            result.output.contains("sizeApkDebug"),
        )
    }

    @Test
    fun detectionUsesExtensionPresenceNotClasspathReflection() {
        val pluginSource =
            readRepoFile(
                "plugin/build-scan-artifact-size-reporter/src/main/kotlin/io/github/cdsap/agp/artifacts/AndroidArtifactsInfoPlugin.kt",
            )
        val extensionSource =
            readRepoFile(
                "plugin/build-scan-artifact-size-reporter/src/main/kotlin/io/github/cdsap/agp/artifacts/ProjectExtension.kt",
            )

        assertTrue(
            "expected named develocity extension detection",
            pluginSource.contains("DEVELOCITY_EXTENSION_NAME") ||
                pluginSource.contains("""findByName("develocity")"""),
        )
        assertTrue(
            "classpath reflection probe must be removed",
            !pluginSource.contains("Class.forName("),
        )
        assertTrue(
            "findByType silent miss must not remain for DevelocityConfiguration",
            !extensionSource.contains("findByType(DevelocityConfiguration"),
        )
        assertTrue(
            "expected getByType(DevelocityConfiguration) for loud failure",
            extensionSource.contains("getByType(DevelocityConfiguration"),
        )
    }

    private fun gradleRunner(): GradleRunner =
        GradleRunner
            .create()
            .withProjectDir(testProjectDir.root)
            .withGradleVersion("9.7.1")

    private fun createProjectWithOptionalDevelocity(includeDevelocity: Boolean) {
        val pluginDir = findRepoDir("plugin").absolutePath

        testProjectDir.newFile("gradle.properties").writeText(
            """
            android.useAndroidX=true
            kotlin.internal.collectFUSMetrics=false
            org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
            """.trimIndent(),
        )
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            repositories {
                google()
                mavenCentral()
            }
            """.trimIndent(),
        )

        val develocityBlock =
            if (includeDevelocity) {
                """
                plugins {
                    id("com.gradle.develocity") version "4.2.2"
                }
                develocity {
                    buildScan {
                        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
                        termsOfUseAgree.set("yes")
                        publishing.onlyIf { false }
                    }
                }
                """.trimIndent()
            } else {
                ""
            }

        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    includeBuild("$pluginDir")
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
            $develocityBlock
            include(":app")
            """.trimIndent(),
        )

        testProjectDir.newFolder("app/src/main/kotlin/com/example")
        testProjectDir.newFile("app/src/main/kotlin/com/example/Hello.kt").writeText(
            """
            package com.example
            class Hello
            """.trimIndent(),
        )
        testProjectDir.newFile("app/src/main/AndroidManifest.xml").writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <application android:label="probe" />
            </manifest>
            """.trimIndent(),
        )
        testProjectDir.newFile("app/build.gradle.kts").writeText(
            """
            plugins {
                id("com.android.application")
                id("io.github.cdsap.android-artifacts-size-report")
            }
            repositories {
                google()
                mavenCentral()
            }
            android {
                namespace = "com.example.probe"
                compileSdk = 35
                defaultConfig {
                    applicationId = "com.example.probe"
                    minSdk = 24
                    targetSdk = 35
                    versionCode = 1
                    versionName = "1.0"
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
            """.trimIndent(),
        )
    }

    private fun findRepoDir(relativePath: String): File {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, relativePath)
            if (candidate.isDirectory) {
                return candidate
            }
            dir = dir.parentFile ?: return@repeat
        }
        error("Could not locate $relativePath from ${System.getProperty("user.dir")}")
    }

    private fun readRepoFile(relativePath: String): String {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, relativePath)
            if (candidate.isFile) {
                return candidate.readText()
            }
            dir = dir.parentFile ?: return@repeat
        }
        error("Could not locate $relativePath from ${System.getProperty("user.dir")}")
    }
}
