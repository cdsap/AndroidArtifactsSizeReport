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
    id("com.gradle.develocity") version "3.19"
}
develocity {
    server = "https://ge.solutions-team.gradle.com/"
    allowUntrustedServer = true
    buildScan {
        uploadInBackground.set(true)
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
