package dev.chuds.still.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Compose theme for Still.
 *
 * Material3 is used only as a Compose foundation for text and accessibility semantics; colors
 * are pinned to monochrome values rather than dynamic Material You colors.
 */
private val StillColorScheme = darkColorScheme(
    background = StillColors.OledBlack,
    surface = StillColors.OledBlack,
    surfaceVariant = StillColors.OledBlack,
    primary = StillColors.SoftWhite,
    secondary = StillColors.Gray,
    onBackground = StillColors.SoftWhite,
    onSurface = StillColors.SoftWhite,
    onPrimary = StillColors.OledBlack,
    onSecondary = StillColors.OledBlack,
    outline = StillColors.Hairline,
)

@Composable
fun StillTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StillColorScheme,
        content = content,
    )
}
