plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

group = "io.github.cdsap"

dependencies {
    implementation(libs.android.tools)
    implementation(libs.kotlin.gradle.plugin)
    implementation(project(":build-scan-artifact-size-reporter"))
    testImplementation(libs.junit)
    testImplementation(gradleTestKit())
}

tasks.test {
    maxHeapSize = "1g"
    jvmArgs("-XX:MaxMetaspaceSize=512m")
}

gradlePlugin {
    plugins {
        create("androidConventions") {
            id = "android-conventions"
            implementationClass = "io.github.cdsap.android.conventions.AndroidConventionsPlugin"
            displayName = "Android Conventions"
            description =
                "Shared Android/Kotlin JVM targets, build types, test runner, and dependencies for sample modules"
        }
    }
}
