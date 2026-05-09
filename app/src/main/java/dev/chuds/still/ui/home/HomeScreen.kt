package dev.chuds.still.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.chuds.still.data.ClockFormat
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * The home surface.
 *
 * No wordmark. Clock + optional date, then only the slots the user has filled (within the
 * configured count). No `add app` placeholders — to add a slot, open settings.
 *
 * Long-press background → all apps. Tap date → intents journal.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onLaunchSlot: (Int) -> Unit,
    onEditSlot: (Int) -> Unit,
    onOpenAllApps: () -> Unit,
    onOpenIntents: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val backgroundInteractionSource = remember { MutableInteractionSource() }
    val dateInteractionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000)
        }
    }

    val use24h = when (uiState.settings.clockFormat) {
        ClockFormat.Hours24 -> true
        ClockFormat.Hours12 -> false
        ClockFormat.Auto -> DateFormat.is24HourFormat(context)
    }
    val clockPattern = if (use24h) "HH:mm" else "h:mm"
    val clockFormatter = remember(clockPattern) {
        DateTimeFormatter.ofPattern(clockPattern, Locale.getDefault())
    }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .combinedClickable(
                interactionSource = backgroundInteractionSource,
                indication = null,
                onClick = {},
                onLongClick = onOpenAllApps,
            )
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        Text(
            text = now.format(clockFormatter),
            style = StillTypography.Clock,
            color = StillColors.SoftWhite,
        )

        if (uiState.settings.showDate) {
            Text(
                text = now.format(dateFormatter),
                style = StillTypography.Date,
                color = StillColors.Gray,
                modifier = Modifier.clickable(
                    interactionSource = dateInteractionSource,
                    indication = null,
                    onClick = onOpenIntents,
                ),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        uiState.homeSlots.forEach { resolved ->
            StillMenuItem(
                title = resolved.displayLabel.orEmpty(),
                subtitle = if (resolved.slot.useFriction) "use intentionally" else null,
                onClick = { onLaunchSlot(resolved.slot.index) },
                onLongClick = { onEditSlot(resolved.slot.index) },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (uiState.settings.showHomeHint) {
            Text(
                text = "long press for all apps",
                style = StillTypography.Caption,
                color = StillColors.DimGray,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
