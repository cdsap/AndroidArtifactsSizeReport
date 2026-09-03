package io.github.cdsap.agp.artifacts

import junit.framework.TestCase.assertTrue
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Proves the published plugin ID works with Configuration Cache so the Plugin Portal
 * compatibility declaration (`configurationCache = true`) matches reality.
 */
class ConfigurationCacheE2ETest {
    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    @Test
    fun publishedPluginIdIsCompatibleWithConfigurationCache() {
        createKotlinClass()
        createAppModule()
        createBuildFiles()

        val runner =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withGradleVersion("9.7.1")
                .withDebug(false)

        val firstBuild =
            runner
                .withArguments(
                    ":app:assembleDebug",
                    "--configuration-cache"
                )
                .build()
        assertTrue(
            "first run should store a configuration cache entry",
            firstBuild.output.contains("Configuration cache entry stored"),
        )

        val secondBuild =
            runner
                .withArguments(
                    ":app:assembleDebug",
                    "--configuration-cache"
                )
                .build()
        assertTrue(
            "second run should be a configuration cache HIT",
            secondBuild.output.contains("Reusing configuration cache."),
        )
    }

    private fun createBuildFiles() {
        testProjectDir.newFile("build.gradle.kts").appendText(
            """
            plugins {
               // id("org.jetbrains.kotlin.android") version "2.2.20" apply false
            }

            repositories {
                mavenCentral()
            }
            """.trimIndent(),
        )

        testProjectDir.newFile("gradle.properties").appendText(
            """
            org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
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
                    classpath("com.android.tools.build:gradle:9.4.0")
                }
            }
            plugins {
                id("com.gradle.develocity") version "4.2.2"
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
