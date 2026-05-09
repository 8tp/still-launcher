package dev.chuds.still.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
 *
 * Uses combinedClickable so the row can consume long-press gestures itself — this prevents the
 * parent screen's long-press handler from firing through items.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StillMenuItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: TextStyle = StillTypography.Menu,
    titleColor: androidx.compose.ui.graphics.Color = StillColors.SoftWhite,
    subtitleColor: androidx.compose.ui.graphics.Color = StillColors.DimGray,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val resolvedTitleColor = if (enabled) titleColor else StillColors.DimGray

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = style,
            color = resolvedTitleColor,
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = StillTypography.Caption,
                color = subtitleColor,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}
