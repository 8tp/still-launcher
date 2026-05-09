package dev.chuds.still.data

import android.content.ComponentName

/**
 * Plain model for an installed app activity that can be launched from a home screen.
 *
 * Still stores package/class pairs instead of icons. This keeps the UI text-first and avoids
 * retaining extra app metadata beyond what is needed to launch a selected app.
 */
data class LaunchableApp(
    val label: String,
    val packageName: String,
    val className: String,
) {
    val componentName: ComponentName
        get() = ComponentName(packageName, className)

    val componentKey: String
        get() = "$packageName/$className"
}
