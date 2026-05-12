package dev.chuds.still.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.chuds.still.data.AppRepository
import dev.chuds.still.data.ClockFormat
import dev.chuds.still.data.DrawerFrictionMode
import dev.chuds.still.data.HomeSlot
import dev.chuds.still.data.IntentEntry
import dev.chuds.still.data.FontPreset
import dev.chuds.still.data.IntentJournalRepository
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
    val systemSettingsPackage: String? = null,
) {
    /** All slots within the user's configured count (filled or empty). Used by Settings. */
    val configuredSlots: List<ResolvedSlot>
        get() = resolvedSlots.take(settings.slotCount)

    /** Only filled slots within the configured count. Used by Home. */
    val homeSlots: List<ResolvedSlot>
        get() = configuredSlots.filter { it.isLaunchable }

    /** True when this slot points at the system Settings app — friction is forbidden there. */
    fun isSystemSettings(slot: ResolvedSlot): Boolean {
        val pkg = systemSettingsPackage ?: return false
        return slot.app?.packageName == pkg
    }
}

class HomeViewModel(
    private val appRepository: AppRepository,
    private val appLauncher: AppLauncher,
    private val intentJournalRepository: IntentJournalRepository,
    private val systemSettingsPackage: String?,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        appRepository.launchableApps,
        appRepository.settings,
    ) { apps, settings ->
        HomeUiState(
            apps = apps,
            settings = settings,
            systemSettingsPackage = systemSettingsPackage,
            resolvedSlots = settings.slots.map { slot ->
                ResolvedSlot(slot = slot, app = appRepository.appForSlot(slot, apps))
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HomeUiState(),
    )

    init {
        viewModelScope.launch {
            appRepository.seedFirstLaunchIfNeeded()
        }
    }

    val intentEntries: StateFlow<List<IntentEntry>> = intentJournalRepository.entries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = emptyList(),
    )

    fun refreshApps() = appRepository.refreshApps()

    fun launchSlot(index: Int): Boolean {
        val resolved = uiState.value.resolvedSlots.getOrNull(index) ?: return false
        val app = resolved.app ?: return false
        return appLauncher.launch(app)
    }

    fun launchApp(app: LaunchableApp): Boolean = appLauncher.launch(app)

    /**
     * Launch a drawer-selected app, journaling the typed intent first if non-blank.
     */
    fun launchAppWithIntent(app: LaunchableApp, intent: String?): Boolean {
        val trimmed = intent?.trim().orEmpty()
        if (trimmed.isNotEmpty()) {
            viewModelScope.launch {
                intentJournalRepository.add(
                    IntentEntry(
                        timestamp = System.currentTimeMillis(),
                        slotLabel = app.label,
                        packageName = app.packageName,
                        intent = trimmed,
                    ),
                )
            }
        }
        return appLauncher.launch(app)
    }

    /**
     * Launch the slot's app, journaling the typed intent first if non-blank.
     */
    fun launchSlotWithIntent(index: Int, intent: String?): Boolean {
        val resolved = uiState.value.resolvedSlots.getOrNull(index) ?: return false
        val app = resolved.app ?: return false
        val trimmed = intent?.trim().orEmpty()
        if (trimmed.isNotEmpty()) {
            viewModelScope.launch {
                intentJournalRepository.add(
                    IntentEntry(
                        timestamp = System.currentTimeMillis(),
                        slotLabel = resolved.displayLabel.orEmpty(),
                        packageName = app.packageName,
                        intent = trimmed,
                    ),
                )
            }
        }
        return appLauncher.launch(app)
    }

    fun clearJournal() {
        viewModelScope.launch { intentJournalRepository.clear() }
    }

    fun setSlotApp(index: Int, app: LaunchableApp) {
        viewModelScope.launch { appRepository.setSlotApp(index, app) }
    }

    fun setSlotLabel(index: Int, label: String?) {
        viewModelScope.launch { appRepository.setSlotLabel(index, label) }
    }

    fun setSlotFriction(index: Int, useFriction: Boolean) {
        val resolved = uiState.value.resolvedSlots.getOrNull(index) ?: return
        if (uiState.value.isSystemSettings(resolved) && useFriction) return
        viewModelScope.launch { appRepository.setSlotFriction(index, useFriction) }
    }

    fun clearSlot(index: Int) {
        viewModelScope.launch { appRepository.clearSlot(index) }
    }

    fun setSlotCount(count: Int) {
        viewModelScope.launch { appRepository.setSlotCount(count) }
    }

    fun setClockFormat(format: ClockFormat) {
        viewModelScope.launch { appRepository.setClockFormat(format) }
    }

    fun setShowDate(show: Boolean) {
        viewModelScope.launch { appRepository.setShowDate(show) }
    }

    fun setShowHomeHint(show: Boolean) {
        viewModelScope.launch { appRepository.setShowHomeHint(show) }
    }

    fun setShowAppIcons(show: Boolean) {
        viewModelScope.launch { appRepository.setShowAppIcons(show) }
    }

    fun setFontPreset(preset: FontPreset) {
        viewModelScope.launch { appRepository.setFontPreset(preset) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { appRepository.setHapticsEnabled(enabled) }
    }

    fun cycleDrawerFrictionMode() {
        val next = when (uiState.value.settings.drawerFrictionMode) {
            DrawerFrictionMode.Off -> DrawerFrictionMode.Allowlist
            DrawerFrictionMode.Allowlist -> DrawerFrictionMode.Blocklist
            DrawerFrictionMode.Blocklist -> DrawerFrictionMode.Off
        }
        viewModelScope.launch { appRepository.setDrawerFrictionMode(next) }
    }

    fun toggleDrawerFrictionException(key: String) {
        viewModelScope.launch { appRepository.toggleDrawerFrictionException(key) }
    }

    companion object {
        fun factory(
            appRepository: AppRepository,
            appLauncher: AppLauncher,
            intentJournalRepository: IntentJournalRepository,
            systemSettingsPackage: String?,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(
                        appRepository = appRepository,
                        appLauncher = appLauncher,
                        intentJournalRepository = intentJournalRepository,
                        systemSettingsPackage = systemSettingsPackage,
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
