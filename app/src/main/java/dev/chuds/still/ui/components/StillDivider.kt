package dev.chuds.still.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chuds.still.ui.theme.StillColors

/**
 * A subtle monochrome hairline divider.
 */
@Composable
fun StillDivider(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(StillColors.Hairline),
    )
}
