package io.github.cdsap.android.conventions

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Shared Android module conventions for this repository's sample app and library.
 *
 * Keeps compileSdk, Java/Kotlin targets, release build type, instrumentation runner,
 * and common dependencies in one place so modules cannot drift apart.
 */
class AndroidConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.android")
        project.pluginManager.apply("io.github.cdsap.android-artifacts-size-report")

        listOf("com.android.application", "com.android.library").forEach { pluginId ->
            project.pluginManager.withPlugin(pluginId) {
                project.configureAndroidCommon()
            }
        }
    }

    private fun Project.configureAndroidCommon() {
        extensions.configure<BaseExtension>("android") {
            compileSdkVersion(COMPILE_SDK)

            defaultConfig {
                testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
            }

            buildTypes.getByName("release").apply {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        dependencies {
            add("implementation", libs.findLibrary("androidx-core-ktx").get())
            add("implementation", libs.findLibrary("androidx-appcompat").get())
            add("implementation", libs.findLibrary("material").get())
            add("testImplementation", libs.findLibrary("junit").get())
            add("androidTestImplementation", libs.findLibrary("androidx-junit").get())
            add("androidTestImplementation", libs.findLibrary("androidx-espresso-core").get())
        }
    }

    companion object {
        const val COMPILE_SDK = 35
        const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
    }
}
