package dev.chuds.still.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.stillPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "still_settings",
)

private val SLOT_COUNT_KEY = intPreferencesKey("slot_count")
private val CLOCK_FORMAT_KEY = stringPreferencesKey("clock_format")
private val SHOW_DATE_KEY = booleanPreferencesKey("show_date")
private val SHOW_HOME_HINT_KEY = booleanPreferencesKey("show_home_hint")
private val SHOW_APP_ICONS_KEY = booleanPreferencesKey("show_app_icons")
private val FONT_PRESET_KEY = stringPreferencesKey("font_preset")
private val HAPTICS_ENABLED_KEY = booleanPreferencesKey("haptics_enabled")
private val FIRST_LAUNCH_COMPLETED_KEY = booleanPreferencesKey("first_launch_completed")

/**
 * Reads and writes local slot state and launcher-wide preferences. No remote sync, no telemetry,
 * no analytics, no network.
 */
class PreferencesRepository(
    private val context: Context,
) {
    val settings: Flow<LauncherSettings> = context.stillPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            LauncherSettings(
                slots = (0 until MAX_SLOT_COUNT).map { index ->
                    HomeSlot(
                        index = index,
                        packageName = preferences[packageKey(index)] ?: "",
                        className = preferences[classKey(index)] ?: "",
                        customLabel = preferences[labelKey(index)],
                        useFriction = preferences[frictionKey(index)] ?: false,
                    )
                },
                slotCount = (preferences[SLOT_COUNT_KEY] ?: DEFAULT_SLOT_COUNT)
                    .coerceIn(1, MAX_SLOT_COUNT),
                clockFormat = (preferences[CLOCK_FORMAT_KEY])
                    ?.let { runCatching { ClockFormat.valueOf(it) }.getOrNull() }
                    ?: ClockFormat.Auto,
                showDate = preferences[SHOW_DATE_KEY] ?: true,
                showHomeHint = preferences[SHOW_HOME_HINT_KEY] ?: true,
                showAppIcons = preferences[SHOW_APP_ICONS_KEY] ?: false,
                fontPreset = (preferences[FONT_PRESET_KEY])
                    ?.let { runCatching { FontPreset.valueOf(it) }.getOrNull() }
                    ?: FontPreset.System,
                hapticsEnabled = preferences[HAPTICS_ENABLED_KEY] ?: true,
                firstLaunchCompleted = preferences[FIRST_LAUNCH_COMPLETED_KEY] ?: false,
            )
        }

    suspend fun setSlotApp(index: Int, app: LaunchableApp) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[packageKey(index)] = app.packageName
            preferences[classKey(index)] = app.className
        }
    }

    suspend fun setSlotLabel(index: Int, label: String?) {
        context.stillPreferencesDataStore.edit { preferences ->
            val trimmed = label?.trim()
            if (trimmed.isNullOrEmpty()) {
                preferences.remove(labelKey(index))
            } else {
                preferences[labelKey(index)] = trimmed
            }
        }
    }

    suspend fun setSlotFriction(index: Int, useFriction: Boolean) {
        context.stillPreferencesDataStore.edit { preferences ->
            if (useFriction) {
                preferences[frictionKey(index)] = true
            } else {
                preferences.remove(frictionKey(index))
            }
        }
    }

    suspend fun clearSlot(index: Int) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences.remove(packageKey(index))
            preferences.remove(classKey(index))
            preferences.remove(labelKey(index))
            preferences.remove(frictionKey(index))
        }
    }

    suspend fun setSlotCount(count: Int) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[SLOT_COUNT_KEY] = count.coerceIn(1, MAX_SLOT_COUNT)
        }
    }

    suspend fun setClockFormat(format: ClockFormat) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[CLOCK_FORMAT_KEY] = format.name
        }
    }

    suspend fun setShowDate(show: Boolean) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[SHOW_DATE_KEY] = show
        }
    }

    suspend fun setShowHomeHint(show: Boolean) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[SHOW_HOME_HINT_KEY] = show
        }
    }

    suspend fun setShowAppIcons(show: Boolean) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[SHOW_APP_ICONS_KEY] = show
        }
    }

    suspend fun setFontPreset(preset: FontPreset) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[FONT_PRESET_KEY] = preset.name
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED_KEY] = enabled
        }
    }

    suspend fun applyDefaultSlots(slots: List<HomeSlot>) {
        context.stillPreferencesDataStore.edit { preferences ->
            slots.forEach { slot ->
                if (slot.isSet) {
                    preferences[packageKey(slot.index)] = slot.packageName
                    preferences[classKey(slot.index)] = slot.className
                }
            }
            preferences[FIRST_LAUNCH_COMPLETED_KEY] = true
        }
    }

    suspend fun markFirstLaunchCompleted() {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_COMPLETED_KEY] = true
        }
    }

    private fun packageKey(index: Int): Preferences.Key<String> =
        stringPreferencesKey("slot_${index}_package")

    private fun classKey(index: Int): Preferences.Key<String> =
        stringPreferencesKey("slot_${index}_class")

    private fun labelKey(index: Int): Preferences.Key<String> =
        stringPreferencesKey("slot_${index}_label")

    private fun frictionKey(index: Int): Preferences.Key<Boolean> =
        booleanPreferencesKey("slot_${index}_friction")
}
