package io.github.cdsap.agp.artifacts

internal enum class ArtifactKind(val directoryName: String) {
    APK("apk"),
    AAB("aab"),
    AAR("aar"),
}

class Output {
    companion object Constants {
        const val OUTPUT = "outputs/size"

        internal fun directoryFor(kind: ArtifactKind, variantName: String): String =
            "$OUTPUT/${kind.directoryName}/$variantName"
    }
}
