package io.github.cdsap.agp.artifacts

import junit.framework.TestCase.assertTrue
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ProjectIsolationE2ETest(private val develocityVersion: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "develocityVersion={0}")
        fun versions() = listOf("4.2.2", "4.1.0", "3.19.2")
    }

    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    @Test
    fun testPluginIsCompatibleWithConfigurationCacheWithoutGradleEnterprise() {
        createKotlinClass()
        createAppModule()
        createBuildGradle(develocityVersion)

        val firstBuild =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir.root)
                .withArguments(
                    ":app:assembleDebug",
                    "-Dkotlin.internal.collectFUSMetrics=false",
                    "-Dorg.gradle.unsafe.isolated-projects=true",
                )
                .withPluginClasspath()
                .withGradleVersion("9.2.1")
                .withDebug(false)
                .build()
        println(firstBuild.output)
        val secondBuild =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir.root)
                .withArguments(
                    ":app:assembleDebug",
                    "-Dorg.gradle.unsafe.isolated-projects=true",
                )
                .withPluginClasspath()
                .withGradleVersion("9.2.1")
                .build()
        println(secondBuild.output)
        assertTrue(firstBuild.output.contains("Configuration cache entry stored"))
        assertTrue(secondBuild.output.contains("Reusing configuration cache."))
    }

    private fun createBuildGradle(develocityVersion: String) {
        testProjectDir.newFile("build.gradle.kts").appendText(
            """
            plugins {
                id("org.jetbrains.kotlin.android") version "2.2.20" apply false
            }
            println("alo")
            repositories {
                mavenCentral()

            }

            """.trimIndent(),
        )

        testProjectDir.newFile("gradle.properties").appendText(
            """
            android.useAndroidX=true
            kotlin.internal.collectFUSMetrics=false
            android.experimental.enableSourceSetPathsMap=true
            android.experimental.cacheCompileLibResources=true
            android.defaults.buildfeatures.renderscript=false
            """.trimIndent(),
        )

        testProjectDir.newFile("settings.gradle.kts").appendText(
            """

            pluginManagement {
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
                        classpath ("com.android.tools.build:gradle:8.13.1")

                    }
                }
            plugins {
                id ("com.gradle.develocity") version "$develocityVersion"
            }
            develocity {
                server = "https://ge.solutions-team.gradle.com/"

            }

            include(":app")
            """.trimIndent(),
        )
    }

    private fun createAppModule() {
        testProjectDir.newFile("app/build.gradle.kts").appendText(
            """
            plugins {
                id("com.android.application")
                id("org.jetbrains.kotlin.android")
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

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
            }

            dependencies {

            }
        """.trimIndent()
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
            """.trimIndent()
        )

    }

    private fun createKotlinClass() {
        testProjectDir.newFolder("app/src/main/kotlin/com/example")
        testProjectDir.newFile("app/src/main/kotlin/com/example/Hello.kt").appendText(
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
