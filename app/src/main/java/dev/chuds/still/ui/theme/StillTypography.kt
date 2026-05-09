package dev.chuds.still.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Still typography tokens.
 *
 * MVP uses platform font fallbacks so the project builds immediately. For the premium version,
 * add open-source fonts under app/src/main/res/font and swap these FontFamily values:
 * - Clock: Cormorant Garamond or Instrument Serif
 * - Menus: Space Grotesk, IBM Plex Mono, or Instrument Sans
 */
object StillTypography {
    private val ClockFont = FontFamily.Serif
    private val MenuFont = FontFamily.SansSerif
    private val MonoFont = FontFamily.Monospace

    val Kicker = TextStyle(
        fontFamily = MonoFont,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.8.sp,
        fontWeight = FontWeight.Normal,
    )

    val Clock = TextStyle(
        fontFamily = ClockFont,
        fontSize = 76.sp,
        lineHeight = 82.sp,
        letterSpacing = (-1.5).sp,
        fontWeight = FontWeight.Light,
    )

    val Date = TextStyle(
        fontFamily = MenuFont,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.3.sp,
        fontWeight = FontWeight.Light,
    )

    val Menu = TextStyle(
        fontFamily = MenuFont,
        fontSize = 24.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.2.sp,
        fontWeight = FontWeight.Light,
    )

    val SecondaryMenu = TextStyle(
        fontFamily = MenuFont,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp,
        fontWeight = FontWeight.Light,
    )

    val Caption = TextStyle(
        fontFamily = MonoFont,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.7.sp,
        fontWeight = FontWeight.Normal,
    )

    val Small = TextStyle(
        fontFamily = MenuFont,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.2.sp,
        fontWeight = FontWeight.Light,
    )
}
