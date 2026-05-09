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
import dev.chuds.still.data.LaunchableApp
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

/**
 * App picker for assigning an installed app to a slot. Slot is anonymous (just an index);
 * the picker doesn't refer to specific app types.
 */
@Composable
fun AppPickerScreen(
    slotIndex: Int,
    apps: List<LaunchableApp>,
    selectedApp: LaunchableApp?,
    onAppSelected: (LaunchableApp) -> Unit,
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
                text = "choose app",
                style = StillTypography.Kicker,
                color = StillColors.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "for slot ${slotIndex + 1}",
                style = StillTypography.Caption,
                color = StillColors.DimGray,
            )
            Spacer(modifier = Modifier.height(24.dp))

            selectedApp?.let { app ->
                Text(
                    text = "current: ${app.label}",
                    style = StillTypography.Date,
                    color = StillColors.MutedWhite,
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            StillMenuItem(
                title = "back",
                style = StillTypography.SecondaryMenu,
                onClick = onBack,
            )
            Spacer(modifier = Modifier.height(12.dp))
            StillDivider()
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (apps.isEmpty()) {
            item {
                Text(
                    text = "No launchable apps found.",
                    style = StillTypography.Date,
                    color = StillColors.MutedWhite,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Still scopes package visibility to launchable apps via <queries> — no QUERY_ALL_PACKAGES. Install an app and refresh.",
                    style = StillTypography.Caption,
                    color = StillColors.DimGray,
                )
            }
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
 * Hidden tools / all-apps screen. Long-press the home background to reach this.
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
                text = "all apps",
                style = StillTypography.Kicker,
                color = StillColors.Gray,
            )
            Spacer(modifier = Modifier.height(30.dp))

            Column {
                StillMenuItem(
                    title = "still settings",
                    subtitle = "configure home slots",
                    style = StillTypography.SecondaryMenu,
                    onClick = onOpenStillSettings,
                )
                StillMenuItem(
                    title = "refresh apps",
                    style = StillTypography.SecondaryMenu,
                    onClick = onRefreshApps,
                )
                StillMenuItem(
                    title = "back",
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
