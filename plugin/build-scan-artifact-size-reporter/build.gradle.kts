plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.gradle.publish)
}

version = "0.1.0"
group = "io.github.cdsap"
dependencies {
    compileOnly(libs.android.tools)
    compileOnly(libs.develocity)
}
gradlePlugin {
    website = "https://github.com/cdsap/BuildScanArtifactSizeReporter"
    vcsUrl = "https://github.com/cdsap/BuildScanArtifactSizeReporter.git"
    plugins {
        create("Android Artifacts Info") {
            id = "io.github.cdsap.android-artifacts-size-report"
            implementationClass = "io.github.cdsap.agp.artifacts.AndroidArtifactsInfoPlugin"
            displayName = "Android Artifacts Size Report"
            description = "Extends Build Scans by adding custom values with the artifacts size of Android projects"
            tags = listOf("build scans", "android")
        }
    }
}
