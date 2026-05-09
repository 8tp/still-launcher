package dev.chuds.still.data

const val MAX_SLOT_COUNT = 10
const val DEFAULT_SLOT_COUNT = 6

/**
 * One home-screen slot. Slots are anonymous indices; the user assigns an app and an optional
 * label. `useFriction` gates the launch through the intent prompt.
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
 * Clock-format preference. `Auto` follows the system's 12/24-hour setting.
 */
enum class ClockFormat { Auto, Hours12, Hours24 }

/**
 * Font preset. `System` uses platform fallbacks; the others use bundled OFL faces.
 */
enum class FontPreset { System, Editorial, Terminal, Grotesk }

/**
 * Local launcher configuration. The slot list always contains MAX_SLOT_COUNT entries (so we
 * preserve assignments when the user temporarily lowers slotCount); UI surfaces should clip to
 * `slotCount` when rendering.
 */
data class LauncherSettings(
    val slots: List<HomeSlot> = (0 until MAX_SLOT_COUNT).map { HomeSlot(it) },
    val slotCount: Int = DEFAULT_SLOT_COUNT,
    val clockFormat: ClockFormat = ClockFormat.Auto,
    val showDate: Boolean = true,
    val showHomeHint: Boolean = true,
    val showAppIcons: Boolean = false,
    val fontPreset: FontPreset = FontPreset.System,
    val firstLaunchCompleted: Boolean = false,
) {
    val visibleSlots: List<HomeSlot>
        get() = slots.take(slotCount.coerceIn(1, MAX_SLOT_COUNT))

    fun slotAt(index: Int): HomeSlot = slots.getOrElse(index) { HomeSlot(index) }
}
