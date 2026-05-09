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
import dev.chuds.still.data.AppSlot
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.home.HomeUiState
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

/**
 * Local settings screen for assigning installed apps to Still's seven home slots.
 */
@Composable
fun SettingsScreen(
    uiState: HomeUiState,
    onChooseSlot: (AppSlot) -> Unit,
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
            text = "Still settings",
            style = StillTypography.Kicker,
            color = StillColors.Gray,
        )

        Spacer(modifier = Modifier.height(34.dp))

        Text(
            text = "Choose what each word opens.",
            style = StillTypography.Date,
            color = StillColors.MutedWhite,
        )

        Spacer(modifier = Modifier.height(28.dp))

        AppSlot.entries.forEach { slot ->
            val app = uiState.appFor(slot)
            StillMenuItem(
                title = slot.displayName,
                subtitle = app?.label ?: "not set",
                style = StillTypography.SecondaryMenu,
                onClick = { onChooseSlot(slot) },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        StillDivider()
        Spacer(modifier = Modifier.height(14.dp))

        StillMenuItem(
            title = "Back",
            style = StillTypography.SecondaryMenu,
            onClick = onBack,
        )
    }
}
