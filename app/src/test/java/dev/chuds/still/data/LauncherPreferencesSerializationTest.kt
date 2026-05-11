package dev.chuds.still.data

import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherPreferencesSerializationTest {
    @Test
    fun slot_list_round_trips_through_preferences_shape() {
        val preferences = mutablePreferencesOf()
        val slots = listOf(
            HomeSlot(
                index = 0,
                packageName = "dev.chuds.notes",
                className = "dev.chuds.notes.MainActivity",
                customLabel = " notes ",
                useFriction = true,
            ),
            HomeSlot(
                index = 1,
                packageName = "dev.chuds.clock",
                className = "dev.chuds.clock.MainActivity",
                customLabel = "",
                useFriction = false,
            ),
            HomeSlot(index = 2),
        )

        LauncherPreferencesCodec.writeSlots(preferences, slots)

        val settings = LauncherPreferencesCodec.readSettings(preferences)
        val first = settings.slotAt(0)
        val second = settings.slotAt(1)
        val third = settings.slotAt(2)

        assertEquals("dev.chuds.notes", first.packageName)
        assertEquals("dev.chuds.notes.MainActivity", first.className)
        assertEquals("notes", first.customLabel)
        assertTrue(first.useFriction)

        assertEquals("dev.chuds.clock", second.packageName)
        assertEquals("dev.chuds.clock.MainActivity", second.className)
        assertNull(second.customLabel)
        assertFalse(second.useFriction)

        assertFalse(third.isSet)
        assertNull(third.customLabel)
        assertFalse(third.useFriction)
    }

    @Test
    fun write_slots_preserves_existing_label_and_friction_for_unset_slots() {
        val preferences = mutablePreferencesOf()

        LauncherPreferencesCodec.writeSlotApp(
            preferences = preferences,
            index = 1,
            packageName = "dev.chuds.kept",
            className = "dev.chuds.kept.MainActivity",
        )
        LauncherPreferencesCodec.writeSlotLabel(preferences, 1, "kept")
        LauncherPreferencesCodec.writeSlotFriction(preferences, 1, true)

        LauncherPreferencesCodec.writeSlots(
            preferences,
            listOf(
                HomeSlot(
                    index = 0,
                    packageName = "dev.chuds.new",
                    className = "dev.chuds.new.MainActivity",
                ),
                HomeSlot(index = 1),
            ),
        )

        val settings = LauncherPreferencesCodec.readSettings(preferences)
        val preserved = settings.slotAt(1)
        assertEquals("dev.chuds.kept", preserved.packageName)
        assertEquals("kept", preserved.customLabel)
        assertTrue(preserved.useFriction)
    }

    @Test
    fun repository_style_slot_writers_are_read_by_the_same_codec() {
        val preferences = mutablePreferencesOf()

        LauncherPreferencesCodec.writeSlotApp(
            preferences = preferences,
            index = 4,
            packageName = "dev.chuds.sms",
            className = "dev.chuds.sms.MainActivity",
        )
        LauncherPreferencesCodec.writeSlotLabel(preferences, 4, " sms ")
        LauncherPreferencesCodec.writeSlotFriction(preferences, 4, true)

        val slot = LauncherPreferencesCodec.readSettings(preferences).slotAt(4)

        assertEquals("dev.chuds.sms", slot.packageName)
        assertEquals("dev.chuds.sms.MainActivity", slot.className)
        assertEquals("sms", slot.customLabel)
        assertTrue(slot.useFriction)

        LauncherPreferencesCodec.clearSlot(preferences, 4)

        val cleared = LauncherPreferencesCodec.readSettings(preferences).slotAt(4)
        assertFalse(cleared.isSet)
        assertNull(cleared.customLabel)
        assertFalse(cleared.useFriction)
    }
}
