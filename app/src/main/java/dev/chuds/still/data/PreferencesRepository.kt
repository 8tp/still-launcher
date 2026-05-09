package dev.chuds.still.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * DataStore singleton for Still settings.
 *
 * The top-level delegate is the recommended way to keep Preferences DataStore as a singleton
 * for the process. Everything remains local to the device.
 */
private val Context.stillPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "still_settings",
)

/**
 * Reads and writes local slot mappings.
 *
 * No remote sync, no telemetry, no analytics, no network.
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
                selections = AppSlot.entries.associateWith { slot ->
                    AppSelection(
                        packageName = preferences[packageKey(slot)] ?: "",
                        className = preferences[classKey(slot)] ?: "",
                    )
                },
            )
        }

    suspend fun setAppForSlot(slot: AppSlot, app: LaunchableApp) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences[packageKey(slot)] = app.packageName
            preferences[classKey(slot)] = app.className
        }
    }

    suspend fun clearAppForSlot(slot: AppSlot) {
        context.stillPreferencesDataStore.edit { preferences ->
            preferences.remove(packageKey(slot))
            preferences.remove(classKey(slot))
        }
    }

    private fun packageKey(slot: AppSlot): Preferences.Key<String> =
        stringPreferencesKey("${slot.preferencePrefix}_package")

    private fun classKey(slot: AppSlot): Preferences.Key<String> =
        stringPreferencesKey("${slot.preferencePrefix}_class")
}
