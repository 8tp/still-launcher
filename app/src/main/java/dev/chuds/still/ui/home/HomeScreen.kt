package dev.chuds.still.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import dev.chuds.still.data.AppSlot
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val PrimarySlots = listOf(
    AppSlot.PHONE,
    AppSlot.MESSAGES,
    AppSlot.SIGNAL,
    AppSlot.MAPS,
    AppSlot.BROWSER,
)

private val SecondarySlots = listOf(
    AppSlot.CAMERA,
    AppSlot.SETTINGS,
)

/**
 * Main Still home screen.
 *
 * The screen is intentionally sparse: title, live clock/date, seven text actions, and one hint.
 * Long-pressing the background opens hidden tools/all apps.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onLaunchSlot: (AppSlot) -> Unit,
    onOpenHiddenTools: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000)
        }
    }

    val clockFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {},
                onLongClick = onOpenHiddenTools,
            )
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        Text(
            text = "Still",
            style = StillTypography.Kicker,
            color = StillColors.Gray,
        )

        Spacer(modifier = Modifier.height(52.dp))

        Text(
            text = now.format(clockFormatter),
            style = StillTypography.Clock,
            color = StillColors.SoftWhite,
        )

        Text(
            text = now.format(dateFormatter),
            style = StillTypography.Date,
            color = StillColors.Gray,
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimarySlots.forEach { slot ->
            StillMenuItem(
                title = slot.displayName,
                subtitle = if (uiState.appFor(slot) == null) "not set" else null,
                onClick = { onLaunchSlot(slot) },
            )
        }

        Spacer(modifier = Modifier.height(26.dp))
        StillDivider()
        Spacer(modifier = Modifier.height(18.dp))

        SecondarySlots.forEach { slot ->
            StillMenuItem(
                title = slot.displayName,
                subtitle = if (uiState.appFor(slot) == null) "not set" else null,
                style = StillTypography.SecondaryMenu,
                onClick = { onLaunchSlot(slot) },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Long press for all apps",
            style = StillTypography.Caption,
            color = StillColors.DimGray,
        )
    }
}
