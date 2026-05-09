package dev.chuds.still.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.chuds.still.data.AppRepository
import dev.chuds.still.data.AppSlot
import dev.chuds.still.data.LaunchableApp
import dev.chuds.still.data.LauncherSettings
import dev.chuds.still.launcher.AppLauncher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for the home, settings, picker, and hidden all-apps screens.
 */
data class HomeUiState(
    val apps: List<LaunchableApp> = emptyList(),
    val settings: LauncherSettings = LauncherSettings(),
    val resolvedSlots: Map<AppSlot, LaunchableApp> = emptyMap(),
) {
    fun appFor(slot: AppSlot): LaunchableApp? = resolvedSlots[slot]
}

/**
 * ViewModel for launcher state and app launching actions.
 *
 * It deliberately contains no network work and no analytics calls.
 */
class HomeViewModel(
    private val appRepository: AppRepository,
    private val appLauncher: AppLauncher,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        appRepository.launchableApps,
        appRepository.settings,
    ) { apps, settings ->
        val resolvedSlots = buildMap {
            AppSlot.entries.forEach { slot ->
                appRepository.resolveSlot(slot = slot, settings = settings, apps = apps)?.let { app ->
                    put(slot, app)
                }
            }
        }

        HomeUiState(
            apps = apps,
            settings = settings,
            resolvedSlots = resolvedSlots,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HomeUiState(),
    )

    fun refreshApps() {
        appRepository.refreshApps()
    }

    fun launchSlot(slot: AppSlot): Boolean {
        val app = uiState.value.appFor(slot) ?: return false
        return appLauncher.launch(app)
    }

    fun launchApp(app: LaunchableApp): Boolean = appLauncher.launch(app)

    fun setAppForSlot(slot: AppSlot, app: LaunchableApp) {
        viewModelScope.launch {
            appRepository.setAppForSlot(slot, app)
        }
    }

    fun clearAppForSlot(slot: AppSlot) {
        viewModelScope.launch {
            appRepository.clearAppForSlot(slot)
        }
    }

    companion object {
        fun factory(
            appRepository: AppRepository,
            appLauncher: AppLauncher,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(
                        appRepository = appRepository,
                        appLauncher = appLauncher,
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
