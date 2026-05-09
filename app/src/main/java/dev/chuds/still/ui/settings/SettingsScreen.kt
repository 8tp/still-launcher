package dev.chuds.still.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.home.HomeUiState
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

/**
 * Lists every slot. Tapping a slot opens its edit surface (if filled) or app picker (if empty).
 */
@Composable
fun SettingsScreen(
    uiState: HomeUiState,
    onChooseSlot: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        Text(
            text = "still settings",
            style = StillTypography.Kicker,
            color = StillColors.Gray,
        )

        Spacer(modifier = Modifier.height(34.dp))

        Text(
            text = "Each slot opens an app you choose.",
            style = StillTypography.Date,
            color = StillColors.MutedWhite,
        )

        Spacer(modifier = Modifier.height(28.dp))

        uiState.resolvedSlots.forEach { resolved ->
            val title = resolved.displayLabel ?: "slot ${resolved.slot.index + 1}"
            val isFilled = resolved.isLaunchable
            val titleColor = if (isFilled) StillColors.SoftWhite else StillColors.DimGray
            val subtitle = when {
                !resolved.slot.isSet -> "not set"
                !isFilled -> "missing"
                resolved.slot.useFriction -> "use intentionally — ${resolved.app?.label.orEmpty()}"
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

        Spacer(modifier = Modifier.height(20.dp))
        StillDivider()
        Spacer(modifier = Modifier.height(14.dp))

        StillMenuItem(
            title = "back",
            style = StillTypography.SecondaryMenu,
            onClick = onBack,
        )
    }
}
