package dev.chuds.still.launcher

import dev.chuds.still.data.DrawerFrictionMode
import dev.chuds.still.data.LaunchableApp
import dev.chuds.still.data.LauncherSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerFrictionPolicyTest {
    private val notes = LaunchableApp(
        label = "notes",
        packageName = "dev.chuds.notes",
        className = "dev.chuds.notes.MainActivity",
    )
    private val notesKey = "${notes.packageName}/${notes.className}"

    @Test
    fun off_mode_never_requires_friction_with_empty_exceptions() {
        val settings = LauncherSettings(drawerFrictionMode = DrawerFrictionMode.Off)
        assertFalse(requiresDrawerFriction(notes, settings))
    }

    @Test
    fun off_mode_never_requires_friction_with_populated_exceptions() {
        val settings = LauncherSettings(
            drawerFrictionMode = DrawerFrictionMode.Off,
            drawerFrictionExceptions = setOf(notesKey),
        )
        assertFalse(requiresDrawerFriction(notes, settings))
    }

    @Test
    fun allowlist_mode_with_empty_exceptions_requires_friction() {
        val settings = LauncherSettings(drawerFrictionMode = DrawerFrictionMode.Allowlist)
        assertTrue(requiresDrawerFriction(notes, settings))
    }

    @Test
    fun allowlist_mode_with_app_in_exceptions_skips_friction() {
        val settings = LauncherSettings(
            drawerFrictionMode = DrawerFrictionMode.Allowlist,
            drawerFrictionExceptions = setOf(notesKey),
        )
        assertFalse(requiresDrawerFriction(notes, settings))
    }

    @Test
    fun blocklist_mode_with_empty_exceptions_skips_friction() {
        val settings = LauncherSettings(drawerFrictionMode = DrawerFrictionMode.Blocklist)
        assertFalse(requiresDrawerFriction(notes, settings))
    }

    @Test
    fun blocklist_mode_with_app_in_exceptions_requires_friction() {
        val settings = LauncherSettings(
            drawerFrictionMode = DrawerFrictionMode.Blocklist,
            drawerFrictionExceptions = setOf(notesKey),
        )
        assertTrue(requiresDrawerFriction(notes, settings))
    }

    @Test
    fun component_key_matching_is_exact_on_class_name() {
        val differentClass = notes.copy(className = "dev.chuds.notes.OtherActivity")
        val settings = LauncherSettings(
            drawerFrictionMode = DrawerFrictionMode.Allowlist,
            drawerFrictionExceptions = setOf(notesKey),
        )
        assertTrue(requiresDrawerFriction(differentClass, settings))
    }
}
