package dev.chuds.still.launcher

import android.content.Intent
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
}
