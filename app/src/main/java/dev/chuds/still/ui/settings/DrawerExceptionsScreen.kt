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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chuds.still.data.DrawerFrictionMode
import dev.chuds.still.data.LaunchableApp
import dev.chuds.still.launcher.drawerFrictionKey
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

@Composable
fun DrawerExceptionsScreen(
    apps: List<LaunchableApp>,
    mode: DrawerFrictionMode,
    exceptions: Set<String>,
    onToggleException: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorted = remember(apps) { apps.sortedBy { it.label.lowercase() } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        item {
            Text(
                text = "app exceptions",
                style = StillTypography.Kicker,
                color = StillColors.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exceptionsSubtitle(mode),
                style = StillTypography.Caption,
                color = StillColors.DimGray,
            )
            Spacer(modifier = Modifier.height(20.dp))
            StillMenuItem(
                title = "back",
                style = StillTypography.SecondaryMenu,
                bordered = true,
                onClick = onBack,
            )
            Spacer(modifier = Modifier.height(12.dp))
            StillDivider()
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(
            items = sorted,
            key = { app -> app.componentKey },
        ) { app ->
            val key = drawerFrictionKey(app)
            val listed = key in exceptions
            StillMenuItem(
                title = app.label,
                subtitle = if (listed) "on" else "off",
                style = StillTypography.SecondaryMenu,
                onClick = { onToggleException(key) },
            )
        }
    }
}

private fun exceptionsSubtitle(mode: DrawerFrictionMode): String = when (mode) {
    DrawerFrictionMode.Off -> "intent prompt is off  —  toggles have no effect"
    DrawerFrictionMode.Allowlist -> "listed apps skip the prompt"
    DrawerFrictionMode.Blocklist -> "listed apps require the prompt"
}
