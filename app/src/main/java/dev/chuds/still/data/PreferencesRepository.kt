package dev.chuds.still.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.stillPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "still_settings",
)

/**
 * Reads and writes local slot state. No remote sync, no telemetry, no analytics, no network.
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
                slots = (0 until SLOT_COUNT).map { index ->
                    HomeSlot(
                        index = index,
                        packageName = preferences[packageKey(index)] ?: "",
                        className = preferences[classKey(index)] ?: "",
                        customLabel = preferences[labelKey(index)],
                        useFriction = preferences[frictionKey(index)] ?: false,
                    )
                },
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

    private fun packageKey(index: Int): Preferences.Key<String> =
        stringPreferencesKey("slot_${index}_package")

    private fun classKey(index: Int): Preferences.Key<String> =
        stringPreferencesKey("slot_${index}_class")

    private fun labelKey(index: Int): Preferences.Key<String> =
        stringPreferencesKey("slot_${index}_label")

    private fun frictionKey(index: Int): Preferences.Key<Boolean> =
        booleanPreferencesKey("slot_${index}_friction")
}
