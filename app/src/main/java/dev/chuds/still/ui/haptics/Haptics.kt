package dev.chuds.still.ui.haptics

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether subtle taps should fire haptic feedback. Provided at the app root from the user's
 * preference; consumed by StillMenuItem (every verb tap) and HomeScreen (slot launches).
 */
val LocalHapticsEnabled = staticCompositionLocalOf { true }
