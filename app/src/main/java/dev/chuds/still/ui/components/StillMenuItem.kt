package dev.chuds.still.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

/**
 * Text-first menu row with no icons and no ripple.
 */
@Composable
fun StillMenuItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: TextStyle = StillTypography.Menu,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val primaryColor = if (enabled) StillColors.SoftWhite else StillColors.DimGray

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = style,
            color = primaryColor,
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = StillTypography.Caption,
                color = StillColors.DimGray,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}
