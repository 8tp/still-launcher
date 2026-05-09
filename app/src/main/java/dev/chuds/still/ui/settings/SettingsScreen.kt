package dev.chuds.still.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chuds.still.data.ClockFormat
import dev.chuds.still.data.MAX_SLOT_COUNT
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.home.HomeUiState
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

@Composable
fun SettingsScreen(
    uiState: HomeUiState,
    onChooseSlot: (Int) -> Unit,
    onChangeSlotCount: (Int) -> Unit,
    onCycleClockFormat: () -> Unit,
    onToggleShowDate: () -> Unit,
    onToggleShowHomeHint: () -> Unit,
    onOpenIntents: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = uiState.settings

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        item {
            Text(
                text = "still settings",
                style = StillTypography.Kicker,
                color = StillColors.Gray,
            )
            Spacer(modifier = Modifier.height(34.dp))
        }

        item {
            SectionHeading("launcher")
            StillMenuItem(
                title = "slot count",
                subtitle = "${settings.slotCount}  —  tap +1, long-press -1",
                style = StillTypography.SecondaryMenu,
                onClick = {
                    val next = if (settings.slotCount >= MAX_SLOT_COUNT) 1 else settings.slotCount + 1
                    onChangeSlotCount(next)
                },
                onLongClick = {
                    val next = if (settings.slotCount <= 1) MAX_SLOT_COUNT else settings.slotCount - 1
                    onChangeSlotCount(next)
                },
            )
            StillMenuItem(
                title = "clock format",
                subtitle = clockFormatLabel(settings.clockFormat),
                style = StillTypography.SecondaryMenu,
                onClick = onCycleClockFormat,
            )
            StillMenuItem(
                title = "show date",
                subtitle = if (settings.showDate) "on  —  taps open intents" else "off",
                style = StillTypography.SecondaryMenu,
                onClick = onToggleShowDate,
            )
            StillMenuItem(
                title = "show home hint",
                subtitle = if (settings.showHomeHint) "on  —  \"long press for all apps\"" else "off",
                style = StillTypography.SecondaryMenu,
                onClick = onToggleShowHomeHint,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            SectionHeading("slots")
        }

        items(
            items = uiState.configuredSlots,
            key = { resolved -> resolved.slot.index },
        ) { resolved ->
            val isFilled = resolved.isLaunchable
            val title = resolved.displayLabel ?: "slot ${resolved.slot.index + 1}"
            val titleColor = if (isFilled) StillColors.SoftWhite else StillColors.DimGray
            val subtitle = when {
                !resolved.slot.isSet -> "empty"
                !isFilled -> "missing"
                resolved.slot.useFriction -> "use intentionally  —  ${resolved.app?.label.orEmpty()}"
                else -> resolved.app?.label
            }
            StillMenuItem(
                title = title,
                subtitle = subtitle,
                style = StillTypography.SecondaryMenu,
                titleColor = titleColor,
                onClick = { onChooseSlot(resolved.slot.index) },
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeading("journal")
            StillMenuItem(
                title = "intents",
                style = StillTypography.SecondaryMenu,
                onClick = onOpenIntents,
            )
            Spacer(modifier = Modifier.height(14.dp))
            StillDivider()
            Spacer(modifier = Modifier.height(14.dp))

            StillMenuItem(
                title = "back",
                style = StillTypography.SecondaryMenu,
                onClick = onBack,
            )
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        style = StillTypography.Kicker,
        color = StillColors.Gray,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

private fun clockFormatLabel(format: ClockFormat): String = when (format) {
    ClockFormat.Auto -> "auto  —  follows system"
    ClockFormat.Hours12 -> "12-hour"
    ClockFormat.Hours24 -> "24-hour"
}
