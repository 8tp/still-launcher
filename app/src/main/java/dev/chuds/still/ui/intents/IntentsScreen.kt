package dev.chuds.still.ui.intents

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.chuds.still.data.IntentEntry
import dev.chuds.still.data.IntentJournalExporter
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * The intent journal — a reverse-chronological list of intents the user typed when launching
 * friction-gated slots. Reading it is the reflection mechanism. Still does not block; it only
 * remembers what you said you'd do.
 */
@Composable
fun IntentsScreen(
    entries: List<IntentEntry>,
    onClear: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingClear by remember { mutableStateOf(false) }

    LaunchedEffect(pendingClear) {
        if (pendingClear) {
            delay(5_000)
            pendingClear = false
        }
    }

    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zone) }
    val locale = LocalConfiguration.current.locales[0]
    val timeFormatter = remember(locale) { DateTimeFormatter.ofPattern("h:mma", locale) }
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMM d", locale) }

    val grouped = remember(entries, today) { groupEntries(entries, zone, today) }

    val context = LocalContext.current
    val currentEntries by rememberUpdatedState(entries)
    val markdownLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val content = IntentJournalExporter.formatMarkdown(currentEntries, zone, Instant.now())
        IntentJournalExporter.writeToUri(context, uri, content)
    }
    val textLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val content = IntentJournalExporter.formatPlainText(currentEntries, zone, Instant.now())
        IntentJournalExporter.writeToUri(context, uri, content)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        item {
            Text(
                text = "intents",
                style = StillTypography.Kicker,
                color = StillColors.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entriesSummary(entries.size),
                style = StillTypography.Caption,
                color = StillColors.DimGray,
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        if (entries.isEmpty()) {
            item {
                Text(
                    text = "No intents yet.",
                    style = StillTypography.Date,
                    color = StillColors.MutedWhite,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Long-press a slot, turn on \"use intentionally\", and the next time you tap it you can write what you're opening it for.",
                    style = StillTypography.Caption,
                    color = StillColors.DimGray,
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        grouped.forEach { (heading, group) ->
            item {
                Text(
                    text = heading,
                    style = StillTypography.Kicker,
                    color = StillColors.Gray,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            items(
                items = group,
                key = { entry -> "${entry.timestamp}_${entry.packageName}" },
            ) { entry ->
                IntentRow(
                    entry = entry,
                    timeFormatter = timeFormatter,
                    dateFormatter = dateFormatter,
                    locale = locale,
                    zone = zone,
                    showDate = heading == EARLIER_HEADING,
                )
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        item {
            StillDivider()
            Spacer(modifier = Modifier.height(8.dp))
            StillMenuItem(
                title = "export markdown",
                style = StillTypography.SecondaryMenu,
                enabled = entries.isNotEmpty(),
                bordered = true,
                onClick = { markdownLauncher.launch("still-intents.md") },
            )
            Spacer(modifier = Modifier.height(8.dp))
            StillMenuItem(
                title = "export text",
                style = StillTypography.SecondaryMenu,
                enabled = entries.isNotEmpty(),
                bordered = true,
                onClick = { textLauncher.launch("still-intents.txt") },
            )
            Spacer(modifier = Modifier.height(8.dp))
            StillMenuItem(
                title = if (pendingClear) "tap again to confirm" else "clear",
                subtitle = if (pendingClear) "this clears every entry" else null,
                style = StillTypography.SecondaryMenu,
                titleColor = if (pendingClear) StillColors.SoftWhite else StillColors.MutedWhite,
                enabled = entries.isNotEmpty(),
                bordered = true,
                onClick = {
                    if (pendingClear) {
                        onClear()
                        pendingClear = false
                    } else {
                        pendingClear = true
                    }
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            StillMenuItem(
                title = "back",
                style = StillTypography.SecondaryMenu,
                bordered = true,
                onClick = onBack,
            )
        }
    }
}

@Composable
private fun IntentRow(
    entry: IntentEntry,
    timeFormatter: DateTimeFormatter,
    dateFormatter: DateTimeFormatter,
    locale: Locale,
    zone: ZoneId,
    showDate: Boolean,
) {
    val instant = remember(entry.timestamp) { Instant.ofEpochMilli(entry.timestamp).atZone(zone) }
    val timestampText = if (showDate) {
        instant.format(dateFormatter)
    } else {
        instant.format(timeFormatter).lowercase(locale)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row {
            Text(
                text = timestampText,
                style = StillTypography.Caption,
                color = StillColors.DimGray,
                modifier = Modifier.width(80.dp),
            )
            Text(
                text = entry.slotLabel.ifBlank { entry.packageName },
                style = StillTypography.Small,
                color = StillColors.MutedWhite,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.intent,
            style = StillTypography.SecondaryMenu,
            color = StillColors.SoftWhite,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private const val TODAY_HEADING = "today"
private const val YESTERDAY_HEADING = "yesterday"
private const val EARLIER_HEADING = "earlier"

private fun groupEntries(
    entries: List<IntentEntry>,
    zone: ZoneId,
    today: LocalDate,
): List<Pair<String, List<IntentEntry>>> {
    val yesterday = today.minusDays(1)
    val groups = linkedMapOf<String, MutableList<IntentEntry>>(
        TODAY_HEADING to mutableListOf(),
        YESTERDAY_HEADING to mutableListOf(),
        EARLIER_HEADING to mutableListOf(),
    )
    entries.forEach { entry ->
        val date = Instant.ofEpochMilli(entry.timestamp).atZone(zone).toLocalDate()
        val bucket = when (date) {
            today -> TODAY_HEADING
            yesterday -> YESTERDAY_HEADING
            else -> EARLIER_HEADING
        }
        groups.getValue(bucket).add(entry)
    }
    return groups.filterValues { it.isNotEmpty() }.toList()
}

private fun entriesSummary(count: Int): String = when (count) {
    0 -> "nothing recorded"
    1 -> "1 intent"
    else -> "$count intents"
}
