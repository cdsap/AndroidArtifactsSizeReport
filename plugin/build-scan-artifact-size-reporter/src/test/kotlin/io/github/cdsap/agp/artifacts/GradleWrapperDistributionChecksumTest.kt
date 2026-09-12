package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Properties

/**
 * Ensures the Gradle wrapper verifies the distribution ZIP against Gradle's published
 * SHA-256 checksum (see https://docs.gradle.org/current/userguide/best_practices_security.html).
 */
class GradleWrapperDistributionChecksumTest {
    @Test
    fun wrapperPropertiesDeclareOfficialDistributionSha256Sum() {
        val propertiesFile = locateWrapperProperties()
        val properties = Properties().apply {
            propertiesFile.inputStream().use { load(it) }
        }

        val distributionUrl = properties.getProperty("distributionUrl")
        assertTrue(
            "distributionUrl must point at the Gradle 9.7.1 binary distribution",
            distributionUrl != null &&
                distributionUrl.contains("gradle-9.7.1-bin.zip"),
        )

        val checksum = properties.getProperty("distributionSha256Sum")
        assertTrue(
            "distributionSha256Sum must be set so the wrapper verifies the downloaded distribution",
            !checksum.isNullOrBlank(),
        )
        assertEquals(
            "distributionSha256Sum must match the official Gradle 9.7.1 -bin checksum " +
                "from https://gradle.org/release-checksums/",
            OFFICIAL_GRADLE_9_7_1_BIN_SHA256,
            checksum,
        )
    }

    private fun locateWrapperProperties(): File {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        repeat(6) {
            val candidate = File(dir, "gradle/wrapper/gradle-wrapper.properties")
            if (candidate.isFile) {
                return candidate
            }
            dir = dir.parentFile ?: return@repeat
        }
        error(
            "Could not locate gradle/wrapper/gradle-wrapper.properties from " +
                System.getProperty("user.dir"),
        )
    }

    companion object {
        // Official Binary-only (-bin) ZIP checksum for Gradle 9.7.1
        // https://gradle.org/release-checksums/
        private const val OFFICIAL_GRADLE_9_7_1_BIN_SHA256 =
            "acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a"
    }
}
