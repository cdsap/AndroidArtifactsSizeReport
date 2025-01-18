plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.tools)
    compileOnly(libs.develocity)
}
gradlePlugin {
    plugins {
        create("Android Application Report") {
            id = "io.github.cdsap.android.application.report"
            implementationClass = "io.github.cdsap.agp.artifacts.AndroidApplicationSizePlugin"
        }
    }

    plugins {
        create("Android Library Report") {
            id = "io.github.cdsap.android.library.report"
            implementationClass = "io.github.cdsap.agp.artifacts.AndroidLibrarySizePlugin"
        }
    }
}
