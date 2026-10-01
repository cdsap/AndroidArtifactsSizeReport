package io.github.cdsap.agp.artifacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Properties

/**
 * Guards the Plugin Portal compatibility metadata written into the plugin descriptor.
 * Each declared feature must be backed by an end-to-end test:
 * configuration cache by [ConfigurationCacheE2ETest], Isolated Projects by [ProjectIsolationE2ETest].
 */
class PluginPortalCompatibilityMetadataTest {
    @Test
    fun publishedPluginDescriptorDeclaresSupportedFeatures() {
        val descriptor = loadDescriptor("io.github.cdsap.android-artifacts-size-report")

        assertEquals(
            "io.github.cdsap.agp.artifacts.AndroidArtifactsInfoPlugin",
            descriptor.getProperty("implementation-class"),
        )
        assertEquals(
            "DECLARED_SUPPORTED",
            descriptor.getProperty("compatibility.feature.configuration-cache"),
        )
        assertEquals(
            "DECLARED_SUPPORTED",
            descriptor.getProperty("compatibility.feature.isolated-projects"),
        )
    }

    private fun loadDescriptor(pluginId: String): Properties {
        val resource = "META-INF/gradle-plugins/$pluginId.properties"
        val stream = javaClass.classLoader.getResourceAsStream(resource)
        assertNotNull("plugin descriptor $resource should be on the classpath", stream)
        return Properties().apply { stream!!.use { load(it) } }
    }
}
