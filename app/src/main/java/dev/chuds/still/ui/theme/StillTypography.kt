package dev.chuds.still.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.chuds.still.data.FontPreset

/**
 * Concrete typography values for the active font preset. Read via [StillTypography] inside
 * a Composable; provide via [LocalStillTypography] at the composition root.
 */
data class StillTypographyValues(
    val Kicker: TextStyle,
    val Clock: TextStyle,
    val Date: TextStyle,
    val Menu: TextStyle,
    val SecondaryMenu: TextStyle,
    val Caption: TextStyle,
    val Small: TextStyle,
)

/**
 * Build typography values from three role-specific [FontFamily] choices. The numeric metrics
 * (sizes, line heights, letter spacing) are fixed across presets — only the families change.
 */
fun stillTypographyValues(
    clockFont: FontFamily,
    menuFont: FontFamily,
    monoFont: FontFamily,
): StillTypographyValues = StillTypographyValues(
    Kicker = TextStyle(
        fontFamily = monoFont,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.8.sp,
        fontWeight = FontWeight.Normal,
    ),
    Clock = TextStyle(
        fontFamily = clockFont,
        fontSize = 76.sp,
        lineHeight = 82.sp,
        letterSpacing = (-1.5).sp,
        fontWeight = FontWeight.Light,
    ),
    Date = TextStyle(
        fontFamily = menuFont,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.3.sp,
        fontWeight = FontWeight.Light,
    ),
    Menu = TextStyle(
        fontFamily = menuFont,
        fontSize = 24.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.2.sp,
        fontWeight = FontWeight.Light,
    ),
    SecondaryMenu = TextStyle(
        fontFamily = menuFont,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp,
        fontWeight = FontWeight.Light,
    ),
    Caption = TextStyle(
        fontFamily = monoFont,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.7.sp,
        fontWeight = FontWeight.Normal,
    ),
    Small = TextStyle(
        fontFamily = menuFont,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.2.sp,
        fontWeight = FontWeight.Light,
    ),
)

/**
 * Pick concrete font families for the requested preset and return the typography it implies.
 * Defaults to platform fallbacks (no bundled fonts loaded) for [FontPreset.System].
 */
fun stillTypographyFor(preset: FontPreset): StillTypographyValues = when (preset) {
    FontPreset.System -> stillTypographyValues(
        clockFont = FontFamily.Serif,
        menuFont = FontFamily.SansSerif,
        monoFont = FontFamily.Monospace,
    )
    FontPreset.Editorial -> stillTypographyValues(
        clockFont = StillFontFamilies.CormorantGaramond,
        menuFont = StillFontFamilies.Inter,
        monoFont = StillFontFamilies.IbmPlexMono,
    )
    FontPreset.Terminal -> stillTypographyValues(
        clockFont = StillFontFamilies.IbmPlexMono,
        menuFont = StillFontFamilies.IbmPlexMono,
        monoFont = StillFontFamilies.IbmPlexMono,
    )
    FontPreset.Grotesk -> stillTypographyValues(
        clockFont = StillFontFamilies.InstrumentSerif,
        menuFont = StillFontFamilies.SpaceGrotesk,
        monoFont = StillFontFamilies.IbmPlexMono,
    )
}

val LocalStillTypography = staticCompositionLocalOf {
    stillTypographyFor(FontPreset.System)
}

/**
 * Composable accessor for typography roles. Identifies as `StillTypography.X` at every call site
 * so screens don't need to thread typography through their parameter lists; the values come from
 * [LocalStillTypography] which the app sets based on the user's preset preference.
 */
object StillTypography {
    val Kicker: TextStyle
        @Composable @ReadOnlyComposable
        get() = LocalStillTypography.current.Kicker

    val Clock: TextStyle
        @Composable @ReadOnlyComposable
        get() = LocalStillTypography.current.Clock

    val Date: TextStyle
        @Composable @ReadOnlyComposable
        get() = LocalStillTypography.current.Date

    val Menu: TextStyle
        @Composable @ReadOnlyComposable
        get() = LocalStillTypography.current.Menu

    val SecondaryMenu: TextStyle
        @Composable @ReadOnlyComposable
        get() = LocalStillTypography.current.SecondaryMenu

    val Caption: TextStyle
        @Composable @ReadOnlyComposable
        get() = LocalStillTypography.current.Caption

    val Small: TextStyle
        @Composable @ReadOnlyComposable
        get() = LocalStillTypography.current.Small
}
