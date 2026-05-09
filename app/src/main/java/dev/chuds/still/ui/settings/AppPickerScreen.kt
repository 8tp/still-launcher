package dev.chuds.still.ui.settings

import android.content.pm.PackageManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
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
    showAppIcons: Boolean,
    onAppSelected: (LaunchableApp) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val filtered = remember(apps, query) { filterApps(apps, query) }

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
            Spacer(modifier = Modifier.height(20.dp))

            SearchField(
                value = query,
                onValueChange = { query = it },
                focusRequester = focusRequester,
                placeholder = "search",
            )

            if (query.isBlank()) {
                selectedApp?.let { app ->
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "current: ${app.label}",
                        style = StillTypography.Date,
                        color = StillColors.MutedWhite,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                StillMenuItem(
                    title = "back",
                    style = StillTypography.SecondaryMenu,
                    onClick = onBack,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            StillDivider()
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (filtered.isEmpty()) {
            item { EmptyAppsHint(query = query) }
        }

        items(
            items = filtered,
            key = { app -> app.componentKey },
        ) { app ->
            AppRow(
                app = app,
                showIcon = showAppIcons,
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
    showAppIcons: Boolean,
    onLaunchApp: (LaunchableApp) -> Unit,
    onOpenStillSettings: () -> Unit,
    onRefreshApps: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val filtered = remember(apps, query) { filterApps(apps, query) }

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
            Spacer(modifier = Modifier.height(20.dp))

            SearchField(
                value = query,
                onValueChange = { query = it },
                focusRequester = focusRequester,
                placeholder = "search",
            )

            if (query.isBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
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
            }

            Spacer(modifier = Modifier.height(14.dp))
            StillDivider()
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (filtered.isEmpty()) {
            item { EmptyAppsHint(query = query) }
        }

        items(
            items = filtered,
            key = { app -> app.componentKey },
        ) { app ->
            AppRow(
                app = app,
                showIcon = showAppIcons,
                onClick = { onLaunchApp(app) },
            )
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    placeholder: String,
) {
    Box {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = StillTypography.SecondaryMenu.copy(color = StillColors.SoftWhite),
            cursorBrush = SolidColor(StillColors.SoftWhite),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = StillTypography.SecondaryMenu,
                color = StillColors.DimGray,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(
    app: LaunchableApp,
    showIcon: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    if (!showIcon) {
        StillMenuItem(
            title = app.label,
            style = StillTypography.SecondaryMenu,
            onClick = onClick,
        )
        return
    }

    val packageManager = LocalContext.current.packageManager
    val iconBitmap = remember(app.packageName) {
        try {
            packageManager.getApplicationIcon(app.packageName)
                .toBitmap(width = 96, height = 96)
                .asImageBitmap()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        } else {
            Spacer(modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = app.label,
            style = StillTypography.SecondaryMenu,
            color = StillColors.SoftWhite,
        )
    }
}

@Composable
private fun EmptyAppsHint(query: String) {
    if (query.isBlank()) {
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
    } else {
        Text(
            text = "No matches for \"${query.trim()}\".",
            style = StillTypography.Date,
            color = StillColors.MutedWhite,
        )
    }
}

private fun filterApps(apps: List<LaunchableApp>, query: String): List<LaunchableApp> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return apps
    return apps.filter { it.label.contains(trimmed, ignoreCase = true) }
}
