pluginManagement {
    repositories {
        includeBuild("plugin")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    // Keep in step with versions.develocity in gradle/libs.versions.toml.
    // Version catalogs are wired in dependencyResolutionManagement, which is
    // evaluated after this plugins {} block, so libs. is not reachable here.
    id("com.gradle.develocity") version "4.0"
}
develocity {
    server = "https://ge.solutions-team.gradle.com/"
    allowUntrustedServer = true
    buildScan {
        uploadInBackground.set(false)
        publishing { true }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BuildScanArtifactSizeReporter"
include(":app")
include(":mylibrary")
