package dev.chuds.still.launcher

import android.content.Intent
import dev.chuds.still.data.LaunchableApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultSlotResolverTest {
    @Test
    fun browserDefaultUsesAppBrowserCategory() {
        val spec = DefaultCategory.Browser.intentSpec()

        assertEquals(Intent.ACTION_MAIN, spec.action)
        assertEquals(Intent.CATEGORY_APP_BROWSER, spec.category)
    }

    @Test
    fun phoneDefaultDoesNotAddPackageVisibilityCategory() {
        val spec = DefaultCategory.Phone.intentSpec()

        assertEquals(Intent.ACTION_DIAL, spec.action)
        assertNull(spec.category)
    }

    @Test
    fun resolvedDefaultMatchesPackageAndClassName() {
        val picked = matchResolvedDefault(
            resolved = ResolvedDefaultComponent(
                packageName = "com.example.browser",
                className = "com.example.browser.DefaultBrowserActivity",
            ),
            apps = listOf(
                LaunchableApp(
                    label = "browser settings",
                    packageName = "com.example.browser",
                    className = "com.example.browser.SettingsActivity",
                ),
                LaunchableApp(
                    label = "browser",
                    packageName = "com.example.browser",
                    className = "com.example.browser.DefaultBrowserActivity",
                ),
            ),
        )

        assertEquals("com.example.browser.DefaultBrowserActivity", picked?.className)
    }

    @Test
    fun resolvedDefaultSkipsPackageWhenClassIsNotLaunchable() {
        val picked = matchResolvedDefault(
            resolved = ResolvedDefaultComponent(
                packageName = "com.example.browser",
                className = "com.example.browser.HiddenActivity",
            ),
            apps = listOf(
                LaunchableApp(
                    label = "browser",
                    packageName = "com.example.browser",
                    className = "com.example.browser.MainActivity",
                ),
            ),
        )

        assertNull(picked)
    }
}
