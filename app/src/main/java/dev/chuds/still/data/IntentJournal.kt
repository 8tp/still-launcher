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
import org.json.JSONArray
import org.json.JSONObject

/**
 * One record in the intent journal. Captures the moment a user typed an intent and pressed Open
 * on a friction-gated slot.
 */
data class IntentEntry(
    val timestamp: Long,
    val slotLabel: String,
    val packageName: String,
    val intent: String,
)

private const val MAX_JOURNAL_ENTRIES = 500

private val Context.intentJournalDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "still_journal",
)

private val JOURNAL_KEY = stringPreferencesKey("entries_v1")

/**
 * DataStore-backed local journal. JSON-encoded list of entries, most recent first, capped at
 * MAX_JOURNAL_ENTRIES with FIFO eviction. No network, no telemetry.
 */
class IntentJournalRepository(
    private val context: Context,
) {
    val entries: Flow<List<IntentEntry>> = context.intentJournalDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> decode(preferences[JOURNAL_KEY]) }

    suspend fun add(entry: IntentEntry) {
        context.intentJournalDataStore.edit { preferences ->
            val current = decode(preferences[JOURNAL_KEY])
            val updated = (listOf(entry) + current).take(MAX_JOURNAL_ENTRIES)
            preferences[JOURNAL_KEY] = encode(updated)
        }
    }

    suspend fun clear() {
        context.intentJournalDataStore.edit { preferences ->
            preferences.remove(JOURNAL_KEY)
        }
    }

    private fun decode(serialized: String?): List<IntentEntry> {
        if (serialized.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(serialized)
            buildList(array.length()) {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    add(
                        IntentEntry(
                            timestamp = obj.optLong("ts"),
                            slotLabel = obj.optString("label"),
                            packageName = obj.optString("pkg"),
                            intent = obj.optString("intent"),
                        ),
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encode(entries: List<IntentEntry>): String {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("ts", entry.timestamp)
                    put("label", entry.slotLabel)
                    put("pkg", entry.packageName)
                    put("intent", entry.intent)
                },
            )
        }
        return array.toString()
    }
}
