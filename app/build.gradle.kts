plugins {
    alias(libs.plugins.android.application)
    id("android-conventions")
}

android {
    namespace = "com.example.myapplication"

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":mylibrary"))
}
