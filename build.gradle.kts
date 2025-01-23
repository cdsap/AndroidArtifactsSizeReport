plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktlint)
}

rootProject.develocity.buildScan {
    buildScanPublished {
        File("temp_build_scan_id.txt").writeText(buildScanId)
    }
}
