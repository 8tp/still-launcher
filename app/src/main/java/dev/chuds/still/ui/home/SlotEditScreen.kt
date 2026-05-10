package dev.chuds.still.ui.home

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
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

/**
 * Long-press edit surface for a filled slot. Text-only, no bottom-sheet chrome.
 */
@Composable
fun SlotEditScreen(
    resolved: ResolvedSlot,
    isSystemSettings: Boolean,
    onRename: () -> Unit,
    onReplaceApp: () -> Unit,
    onToggleFriction: () -> Unit,
    onRemove: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = resolved.displayLabel ?: "slot ${resolved.slot.index + 1}"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        Text(
            text = "edit",
            style = StillTypography.Kicker,
            color = StillColors.Gray,
        )

        Spacer(modifier = Modifier.height(34.dp))

        Text(
            text = label,
            style = StillTypography.Menu,
            color = StillColors.SoftWhite,
        )
        resolved.app?.let { app ->
            if (resolved.slot.customLabel?.isNotBlank() == true) {
                Text(
                    text = app.label,
                    style = StillTypography.Caption,
                    color = StillColors.DimGray,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        StillDivider()
        Spacer(modifier = Modifier.height(12.dp))

        StillMenuItem(
            title = "rename",
            subtitle = resolved.slot.customLabel?.takeIf { it.isNotBlank() } ?: "uses app name",
            style = StillTypography.SecondaryMenu,
            onClick = onRename,
        )

        StillMenuItem(
            title = "replace app",
            style = StillTypography.SecondaryMenu,
            onClick = onReplaceApp,
        )

        if (!isSystemSettings) {
            StillMenuItem(
                title = "use intentionally",
                subtitle = if (resolved.slot.useFriction) "on" else "off",
                style = StillTypography.SecondaryMenu,
                onClick = onToggleFriction,
            )
        }

        StillMenuItem(
            title = "remove",
            style = StillTypography.SecondaryMenu,
            onClick = onRemove,
        )

        Spacer(modifier = Modifier.height(12.dp))
        StillDivider()
        Spacer(modifier = Modifier.height(12.dp))

        StillMenuItem(
            title = "back",
            style = StillTypography.SecondaryMenu,
            bordered = true,
            onClick = onBack,
        )
    }
}
