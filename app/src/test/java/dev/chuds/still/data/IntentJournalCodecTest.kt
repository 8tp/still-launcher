package dev.chuds.still.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntentJournalCodecTest {
    @Test
    fun append_adds_newest_entry_first() {
        val first = entry(1)
        val second = entry(2)

        val serialized = IntentJournalCodec.append(
            serialized = IntentJournalCodec.encode(listOf(first)),
            entry = second,
        )

        assertEquals(listOf(second, first), IntentJournalCodec.decode(serialized))
    }

    @Test
    fun append_caps_journal_at_500_entries_and_evicts_oldest() {
        var serialized = IntentJournalCodec.clearSerialized()

        for (index in 0..500) {
            serialized = IntentJournalCodec.append(serialized, entry(index))
        }

        val decoded = IntentJournalCodec.decode(serialized)
        assertEquals(500, decoded.size)
        assertEquals(500L, decoded.first().timestamp)
        assertEquals(1L, decoded.last().timestamp)
    }

    @Test
    fun clear_serialized_decodes_to_empty_journal() {
        val serialized = IntentJournalCodec.clearSerialized()

        assertTrue(IntentJournalCodec.decode(serialized).isEmpty())
    }

    @Test
    fun malformed_json_decodes_to_empty_journal() {
        assertTrue(IntentJournalCodec.decode("{not json").isEmpty())
    }

    private fun entry(index: Int): IntentEntry =
        IntentEntry(
            timestamp = index.toLong(),
            slotLabel = "slot $index",
            packageName = "dev.example.$index",
            intent = "write test $index",
        )
}
