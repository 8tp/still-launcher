package dev.chuds.still.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import dev.chuds.still.data.AppSlot
import dev.chuds.still.data.LaunchableApp
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

/**
 * App picker used by settings. It lists launchable apps only and stores a package/class pair.
 */
@Composable
fun AppPickerScreen(
    slot: AppSlot,
    apps: List<LaunchableApp>,
    selectedApp: LaunchableApp?,
    onAppSelected: (LaunchableApp) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        item {
            Text(
                text = "Choose ${slot.displayName}",
                style = StillTypography.Kicker,
                color = StillColors.Gray,
            )
            Spacer(modifier = Modifier.height(30.dp))

            selectedApp?.let { app ->
                Text(
                    text = "Current: ${app.label}",
                    style = StillTypography.Date,
                    color = StillColors.MutedWhite,
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            StillMenuItem(
                title = "Clear",
                subtitle = "Leave this slot unset",
                style = StillTypography.SecondaryMenu,
                onClick = onClear,
            )
            StillMenuItem(
                title = "Back",
                style = StillTypography.SecondaryMenu,
                onClick = onBack,
            )
            Spacer(modifier = Modifier.height(12.dp))
            StillDivider()
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(
            items = apps,
            key = { app -> app.componentKey },
        ) { app ->
            StillMenuItem(
                title = app.label,
                subtitle = app.packageName,
                style = StillTypography.SecondaryMenu,
                onClick = { onAppSelected(app) },
            )
        }
    }
}

/**
 * Hidden tools / all-apps screen opened by long-pressing the home background.
 *
 * This is intentionally hidden rather than exposed as a default app drawer.
 */
@Composable
fun AllAppsScreen(
    apps: List<LaunchableApp>,
    onLaunchApp: (LaunchableApp) -> Unit,
    onOpenStillSettings: () -> Unit,
    onRefreshApps: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        item {
            Text(
                text = "All apps",
                style = StillTypography.Kicker,
                color = StillColors.Gray,
            )
            Spacer(modifier = Modifier.height(30.dp))

            Column {
                StillMenuItem(
                    title = "Still settings",
                    subtitle = "map home words to apps",
                    style = StillTypography.SecondaryMenu,
                    onClick = onOpenStillSettings,
                )
                StillMenuItem(
                    title = "Refresh apps",
                    style = StillTypography.SecondaryMenu,
                    onClick = onRefreshApps,
                )
                StillMenuItem(
                    title = "Back",
                    style = StillTypography.SecondaryMenu,
                    onClick = onBack,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            StillDivider()
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(
            items = apps,
            key = { app -> app.componentKey },
        ) { app ->
            StillMenuItem(
                title = app.label,
                subtitle = app.packageName,
                style = StillTypography.SecondaryMenu,
                onClick = { onLaunchApp(app) },
            )
        }
    }
}
