package dev.chuds.still.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.chuds.still.data.AppRepository
import dev.chuds.still.data.HomeSlot
import dev.chuds.still.data.LaunchableApp
import dev.chuds.still.data.LauncherSettings
import dev.chuds.still.launcher.AppLauncher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Resolved view of a single home slot — its persisted config and the app it currently points at.
 */
data class ResolvedSlot(
    val slot: HomeSlot,
    val app: LaunchableApp?,
) {
    val isLaunchable: Boolean get() = app != null
    val displayLabel: String? get() = slot.resolvedLabel(app)
}

data class HomeUiState(
    val apps: List<LaunchableApp> = emptyList(),
    val settings: LauncherSettings = LauncherSettings(),
    val resolvedSlots: List<ResolvedSlot> = emptyList(),
)

class HomeViewModel(
    private val appRepository: AppRepository,
    private val appLauncher: AppLauncher,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        appRepository.launchableApps,
        appRepository.settings,
    ) { apps, settings ->
        HomeUiState(
            apps = apps,
            settings = settings,
            resolvedSlots = settings.slots.map { slot ->
                ResolvedSlot(slot = slot, app = appRepository.appForSlot(slot, apps))
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HomeUiState(),
    )

    fun refreshApps() = appRepository.refreshApps()

    fun launchSlot(index: Int): Boolean {
        val resolved = uiState.value.resolvedSlots.getOrNull(index) ?: return false
        val app = resolved.app ?: return false
        return appLauncher.launch(app)
    }

    fun launchApp(app: LaunchableApp): Boolean = appLauncher.launch(app)

    fun setSlotApp(index: Int, app: LaunchableApp) {
        viewModelScope.launch { appRepository.setSlotApp(index, app) }
    }

    fun setSlotLabel(index: Int, label: String?) {
        viewModelScope.launch { appRepository.setSlotLabel(index, label) }
    }

    fun setSlotFriction(index: Int, useFriction: Boolean) {
        viewModelScope.launch { appRepository.setSlotFriction(index, useFriction) }
    }

    fun clearSlot(index: Int) {
        viewModelScope.launch { appRepository.clearSlot(index) }
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
