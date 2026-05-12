package dev.chuds.still.data

import dev.chuds.still.launcher.DefaultSlotResolver
import dev.chuds.still.launcher.PackageScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Combines installed-app scanning with the user's slot configuration and launcher prefs.
 *
 * On first launch, [seedFirstLaunchIfNeeded] seeds slots from [DefaultSlotResolver]. Once the
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

    suspend fun setFontPreset(preset: FontPreset) =
        preferencesRepository.setFontPreset(preset)

    suspend fun setHapticsEnabled(enabled: Boolean) =
        preferencesRepository.setHapticsEnabled(enabled)

    suspend fun setDrawerFrictionMode(mode: DrawerFrictionMode) =
        preferencesRepository.setDrawerFrictionMode(mode)

    suspend fun toggleDrawerFrictionException(key: String) =
        preferencesRepository.toggleDrawerFrictionException(key)

    suspend fun seedFirstLaunchIfNeeded() {
        seedFirstLaunchDefaults(
            settings = settings,
            apps = launchableApps,
            resolve = defaultSlotResolver::resolve,
            applyDefaults = preferencesRepository::applyDefaultSlots,
            markComplete = preferencesRepository::markFirstLaunchCompleted,
        )
    }
}

/**
 * Awaits a non-empty launchable-app list before deciding whether to seed defaults. An initial
 * empty emission (early invocation, restricted profile, or a swallowed scan exception) re-arms
 * until a real scan arrives, so the first-launch flag never latches on a transient empty state.
 */
internal suspend fun seedFirstLaunchDefaults(
    settings: Flow<LauncherSettings>,
    apps: Flow<List<LaunchableApp>>,
    resolve: (List<LaunchableApp>) -> List<HomeSlot>,
    applyDefaults: suspend (List<HomeSlot>) -> Unit,
    markComplete: suspend () -> Unit,
) {
    if (settings.first().firstLaunchCompleted) return
    val readyApps = apps.first { it.isNotEmpty() }
    val defaults = resolve(readyApps)
    if (defaults.isNotEmpty()) {
        applyDefaults(defaults)
    } else {
        markComplete()
    }
}
