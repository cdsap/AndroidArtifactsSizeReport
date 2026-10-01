package io.github.cdsap.agp.artifacts

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

/**
 * Proves the published plugin ID works with Isolated Projects so the Plugin Portal
 * compatibility declaration (`isolatedProjects = true`) matches reality.
 *
 * The plugin is applied to both an Android application and an Android library in the
 * same build, covering every code path registered by the plugin.
 */
@RunWith(Parameterized::class)
class ProjectIsolationE2ETest(
    private val develocityVersion: String,
    private val gradleVersion: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "develocityVersion={0}, gradleVersion={1}")
        fun parameters(): List<Array<String>> =
            listOf("4.1", "4.2.2", "4.5.0").flatMap { develocity ->
                listOf("9.7.1", "9.8.0").map { gradle -> arrayOf(develocity, gradle) }
            }
    }

    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    @Test
    fun publishedPluginIdIsCompatibleWithIsolatedProjects() {
        createKotlinClass("app")
        createAppModule()
        createKotlinClass("mylibrary")
        createLibraryModule()
        createBuildFiles()

        val runner =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir.root)
                .withGradleVersion(gradleVersion)
                .withDebug(false)
                .withArguments(
                    ":app:assembleDebug",
                    ":mylibrary:assembleDebug",
                    "-Dorg.gradle.unsafe.isolated-projects=true",
                )

        val firstBuild = runner.build()
        println(firstBuild.output)
        assertTrue(
            "Isolated Projects should be enabled",
            firstBuild.output.contains("Isolated Projects is an incubating feature."),
        )
        assertTrue(
            "first run should store a configuration cache entry",
            firstBuild.output.contains("Configuration cache entry stored"),
        )
        assertSizeReported("app/build/outputs/size/apk/debug", ".apk.size")
        assertSizeReported("mylibrary/build/outputs/size/aar/debug", ".aar.size")

        val secondBuild = runner.build()
        println(secondBuild.output)
        assertTrue(
            "second run should be a configuration cache HIT",
            secondBuild.output.contains("Reusing configuration cache."),
        )
    }

    private fun assertSizeReported(
        directory: String,
        suffix: String,
    ) {
        val markers =
            File(testProjectDir.root, directory)
                .listFiles { file -> file.name.endsWith(suffix) }
                .orEmpty()
        assertEquals("expected one $suffix marker in $directory", 1, markers.size)
        assertTrue(
            "${markers.single().name} should record a positive size",
            markers.single().readText().toLong() > 0,
        )
    }

    private fun createBuildFiles() {
        testProjectDir.newFile("build.gradle.kts").appendText(
            """
            repositories {
                mavenCentral()
            }
            """.trimIndent(),
        )

        testProjectDir.newFile("gradle.properties").appendText(
            """
            android.useAndroidX=true
            kotlin.internal.collectFUSMetrics=false
            org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
            """.trimIndent(),
        )

        testProjectDir.newFile("settings.gradle.kts").appendText(
            """
            pluginManagement {
                repositories {
                    includeBuild("${pluginDir().absolutePath}")
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
                id("com.gradle.develocity") version "$develocityVersion"
            }
            develocity {
                server = "https://ge.solutions-team.gradle.com/"
            }

            include(":app")
            include(":mylibrary")
            """.trimIndent(),
        )
    }

    private fun pluginDir(): File {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, "plugin")
            if (candidate.isDirectory) {
                return candidate
            }
            dir = dir.parentFile ?: return@repeat
        }
        error("Could not locate plugin/ from ${System.getProperty("user.dir")}")
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
                compileSdk = 35

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
                compileSdk = 35

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
