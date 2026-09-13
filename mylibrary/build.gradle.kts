plugins {
    alias(libs.plugins.android.library)
    id("android-conventions")
}

android {
    namespace = "com.example.mylibrary"

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }
}
