package dev.chuds.still.data

import dev.chuds.still.launcher.PackageScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Small repository that combines installed-app scanning with local user settings.
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

    suspend fun setAppForSlot(slot: AppSlot, app: LaunchableApp) {
        preferencesRepository.setAppForSlot(slot, app)
    }

    suspend fun clearAppForSlot(slot: AppSlot) {
        preferencesRepository.clearAppForSlot(slot)
    }

    /**
     * Resolves a slot to the user's explicit selection, falling back to simple local heuristics.
     *
     * The heuristics are intentionally conservative and exist only to make first boot pleasant.
     * Users can override every slot from Still settings.
     */
    fun resolveSlot(
        slot: AppSlot,
        settings: LauncherSettings,
        apps: List<LaunchableApp>,
    ): LaunchableApp? {
        val storedSelection = settings.selectionFor(slot)
        if (storedSelection.isSet) {
            apps.firstOrNull(storedSelection::matches)?.let { return it }
        }

        return suggestedApp(slot = slot, apps = apps)
    }

    private fun suggestedApp(slot: AppSlot, apps: List<LaunchableApp>): LaunchableApp? {
        val tokens = when (slot) {
            AppSlot.PHONE -> listOf("phone", "dialer", "telephone")
            AppSlot.MESSAGES -> listOf("messages", "messaging", "sms")
            AppSlot.SIGNAL -> listOf("signal")
            AppSlot.MAPS -> listOf("maps", "map", "organic maps", "osmand")
            AppSlot.BROWSER -> listOf("browser", "vanadium", "firefox", "mull", "chromium", "chrome", "brave")
            AppSlot.CAMERA -> listOf("camera")
            AppSlot.SETTINGS -> listOf("settings")
        }

        return tokens.asSequence()
            .mapNotNull { token ->
                apps.firstOrNull { app ->
                    app.label.contains(token, ignoreCase = true) ||
                        app.packageName.contains(token, ignoreCase = true)
                }
            }
            .firstOrNull()
    }
}
