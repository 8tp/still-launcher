package dev.chuds.still.data

import dev.chuds.still.launcher.DefaultSlotResolver
import dev.chuds.still.launcher.PackageScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Combines installed-app scanning with the user's slot configuration and launcher prefs.
 *
 * On first launch, [populateDefaultSlots] seeds slots from [DefaultSlotResolver]. Once the
 * first-launch flag flips, the resolver is never run again.
 */
class AppRepository(
    private val packageScanner: PackageScanner,
    private val preferencesRepository: PreferencesRepository,
    private val defaultSlotResolver: DefaultSlotResolver,
) {
    val settings: Flow<LauncherSettings> = preferencesRepository.settings

    private val _launchableApps = MutableStateFlow(packageScanner.loadLaunchableApps())
    val launchableApps: StateFlow<List<LaunchableApp>> = _launchableApps.asStateFlow()

    fun refreshApps() {
        _launchableApps.value = packageScanner.loadLaunchableApps()
    }

    fun appForSlot(slot: HomeSlot, apps: List<LaunchableApp>): LaunchableApp? =
        if (slot.isSet) apps.firstOrNull(slot::matches) else null

    suspend fun setSlotApp(index: Int, app: LaunchableApp) =
        preferencesRepository.setSlotApp(index, app)

    suspend fun setSlotLabel(index: Int, label: String?) =
        preferencesRepository.setSlotLabel(index, label)

    suspend fun setSlotFriction(index: Int, useFriction: Boolean) =
        preferencesRepository.setSlotFriction(index, useFriction)

    suspend fun clearSlot(index: Int) =
        preferencesRepository.clearSlot(index)

    suspend fun setSlotCount(count: Int) =
        preferencesRepository.setSlotCount(count)

    suspend fun setClockFormat(format: ClockFormat) =
        preferencesRepository.setClockFormat(format)

    suspend fun setShowDate(show: Boolean) =
        preferencesRepository.setShowDate(show)

    suspend fun setShowHomeHint(show: Boolean) =
        preferencesRepository.setShowHomeHint(show)

    suspend fun setShowAppIcons(show: Boolean) =
        preferencesRepository.setShowAppIcons(show)

    suspend fun populateDefaultSlots(apps: List<LaunchableApp>) {
        val defaults = defaultSlotResolver.resolve(apps)
        if (defaults.isEmpty()) {
            preferencesRepository.markFirstLaunchCompleted()
        } else {
            preferencesRepository.applyDefaultSlots(defaults)
        }
    }
}
