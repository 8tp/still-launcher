package dev.chuds.still.ui.friction

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
 * Intentional-friction screen.
 *
 * MVP uses this for Browser only. Later versions can add per-slot friction rules.
 */
@Composable
fun FrictionScreen(
    title: String,
    prompt: String,
    onOpen: () -> Unit,
    onCancel: () -> Unit,
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
            text = title,
            style = StillTypography.Kicker,
            color = StillColors.Gray,
        )

        Spacer(modifier = Modifier.height(70.dp))

        Text(
            text = prompt,
            style = StillTypography.Menu,
            color = StillColors.SoftWhite,
        )

        Spacer(modifier = Modifier.height(34.dp))
        StillDivider()
        Spacer(modifier = Modifier.height(18.dp))

        StillMenuItem(
            title = "Open",
            style = StillTypography.SecondaryMenu,
            onClick = onOpen,
        )

        StillMenuItem(
            title = "Cancel",
            style = StillTypography.SecondaryMenu,
            onClick = onCancel,
        )
    }
}
