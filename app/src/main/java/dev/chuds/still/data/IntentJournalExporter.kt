package dev.chuds.still.data

import android.content.Context
import android.net.Uri
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Formats and writes the intent journal to a user-picked file URI. Two formats:
 * markdown for journaling apps that render headings, and plain text for raw archives.
 *
 * Stays local — writes via the content resolver to a SAF URI the user chose. No network.
 */
object IntentJournalExporter {

    fun formatMarkdown(entries: List<IntentEntry>, zone: ZoneId, exportedAt: Instant): String {
        val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zone)
        val exportFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zone)

        return buildString {
            appendLine("# still — intents")
            appendLine()
            appendLine("Exported ${exportFormatter.format(exportedAt)}.")
            appendLine("${entries.size} ${if (entries.size == 1) "entry" else "entries"}.")
            appendLine()
            entries.forEach { entry ->
                val instant = Instant.ofEpochMilli(entry.timestamp)
                val heading = entry.slotLabel.ifBlank { entry.packageName }
                appendLine("## ${timestampFormatter.format(instant)} — $heading")
                appendLine()
                appendLine("**App:** ${entry.packageName}")
                appendLine()
                appendLine(entry.intent)
                appendLine()
            }
        }
    }

    fun formatPlainText(entries: List<IntentEntry>, zone: ZoneId, exportedAt: Instant): String {
        val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zone)
        val exportFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zone)

        return buildString {
            appendLine("still — intents")
            appendLine("Exported ${exportFormatter.format(exportedAt)}.")
            appendLine("${entries.size} ${if (entries.size == 1) "entry" else "entries"}.")
            appendLine()
            entries.forEach { entry ->
                val instant = Instant.ofEpochMilli(entry.timestamp)
                val heading = entry.slotLabel.ifBlank { entry.packageName }
                appendLine("${timestampFormatter.format(instant)}  $heading  (${entry.packageName})")
                appendLine("  ${entry.intent}")
                appendLine()
            }
        }
    }

    fun writeToUri(context: Context, uri: Uri, content: String): Boolean = try {
        context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        }
        true
    } catch (_: IOException) {
        false
    }
}
