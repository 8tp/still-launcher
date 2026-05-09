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
 * No wordmark. Clock + date, then up to seven user-defined slots. Empty slots show a dim
 * `add app` placeholder. Tap empty → app picker. Long-press filled → edit sheet. Long-press
 * background → all apps.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onLaunchSlot: (Int) -> Unit,
    onAddSlotApp: (Int) -> Unit,
    onEditSlot: (Int) -> Unit,
    onOpenAllApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val backgroundInteractionSource = remember { MutableInteractionSource() }

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

        Text(
            text = now.format(dateFormatter),
            style = StillTypography.Date,
            color = StillColors.Gray,
        )

        Spacer(modifier = Modifier.weight(1f))

        uiState.resolvedSlots.forEach { resolved ->
            val isFilled = resolved.isLaunchable
            val title = resolved.displayLabel ?: "add app"
            val titleColor = if (isFilled) StillColors.SoftWhite else StillColors.DimGray
            val subtitle = when {
                resolved.slot.isSet && !isFilled -> "missing"
                resolved.slot.useFriction && isFilled -> "use intentionally"
                else -> null
            }

            StillMenuItem(
                title = title,
                subtitle = subtitle,
                titleColor = titleColor,
                onClick = {
                    if (isFilled) {
                        onLaunchSlot(resolved.slot.index)
                    } else {
                        onAddSlotApp(resolved.slot.index)
                    }
                },
                onLongClick = if (isFilled) {
                    { onEditSlot(resolved.slot.index) }
                } else {
                    null
                },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "long press for all apps",
            style = StillTypography.Caption,
            color = StillColors.DimGray,
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}
