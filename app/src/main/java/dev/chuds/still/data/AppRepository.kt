package dev.chuds.still.data

import dev.chuds.still.launcher.PackageScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Combines installed-app scanning with the user's slot configuration.
 *
 * No first-boot heuristics. New installs see seven empty slots; the user fills them.
 */
class AppRepository(
    private val packageScanner: PackageScanner,
    private val preferencesRepository: PreferencesRepository,
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
}
