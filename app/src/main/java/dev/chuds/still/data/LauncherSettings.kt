package dev.chuds.still.data

/**
 * Home-screen slots Still exposes by default.
 *
 * The display name is intentionally plain because the launcher is text-first.
 */
enum class AppSlot(
    val displayName: String,
    val preferencePrefix: String,
    val isPrimary: Boolean,
) {
    PHONE("Phone", "phone", true),
    MESSAGES("Messages", "messages", true),
    SIGNAL("Signal", "signal", true),
    MAPS("Maps", "maps", true),
    BROWSER("Browser", "browser", true),
    CAMERA("Camera", "camera", false),
    SETTINGS("Settings", "settings", false),
}

/**
 * Persisted reference to a launchable activity.
 */
data class AppSelection(
    val packageName: String = "",
    val className: String = "",
) {
    val isSet: Boolean
        get() = packageName.isNotBlank() && className.isNotBlank()

    fun matches(app: LaunchableApp): Boolean =
        packageName == app.packageName && className == app.className

    companion object {
        val Empty = AppSelection()
    }
}

/**
 * Complete local launcher configuration.
 *
 * Preferences DataStore converts key-value pairs into this typed in-memory shape.
 */
data class LauncherSettings(
    val selections: Map<AppSlot, AppSelection> = AppSlot.entries.associateWith { AppSelection.Empty },
) {
    fun selectionFor(slot: AppSlot): AppSelection = selections[slot] ?: AppSelection.Empty
}
