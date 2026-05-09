package dev.chuds.still.data

const val SLOT_COUNT = 7

/**
 * One home-screen slot. Slots are anonymous indices; the user assigns an app and an optional
 * label. `useFriction` gates the launch through `FrictionScreen`.
 */
data class HomeSlot(
    val index: Int,
    val packageName: String = "",
    val className: String = "",
    val customLabel: String? = null,
    val useFriction: Boolean = false,
) {
    val isSet: Boolean
        get() = packageName.isNotBlank() && className.isNotBlank()

    fun matches(app: LaunchableApp): Boolean =
        packageName == app.packageName && className == app.className

    fun resolvedLabel(app: LaunchableApp?): String? {
        val custom = customLabel?.trim()?.takeIf { it.isNotEmpty() }
        return custom ?: app?.label
    }
}

/**
 * Local launcher configuration. Exactly `SLOT_COUNT` slots, indexed 0..SLOT_COUNT-1.
 */
data class LauncherSettings(
    val slots: List<HomeSlot> = (0 until SLOT_COUNT).map { HomeSlot(it) },
) {
    fun slotAt(index: Int): HomeSlot = slots.getOrElse(index) { HomeSlot(index) }
}
