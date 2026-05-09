package dev.chuds.still

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.chuds.still.data.AppRepository
import dev.chuds.still.data.ClockFormat
import dev.chuds.still.data.FontPreset
import dev.chuds.still.data.IntentJournalRepository
import dev.chuds.still.data.PreferencesRepository
import dev.chuds.still.ui.theme.LocalStillTypography
import dev.chuds.still.ui.theme.stillTypographyFor
import dev.chuds.still.launcher.AppLauncher
import dev.chuds.still.launcher.DefaultSlotResolver
import dev.chuds.still.launcher.PackageScanner
import dev.chuds.still.launcher.SystemSettingsLocator
import dev.chuds.still.ui.home.HomeScreen
import dev.chuds.still.ui.home.HomeViewModel
import dev.chuds.still.ui.home.SlotEditScreen
import dev.chuds.still.ui.home.SlotRenameScreen
import dev.chuds.still.ui.intents.IntentPromptScreen
import dev.chuds.still.ui.intents.IntentsScreen
import dev.chuds.still.ui.settings.AllAppsScreen
import dev.chuds.still.ui.settings.AppPickerScreen
import dev.chuds.still.ui.settings.SettingsScreen

/**
 * Top-level Compose application shell.
 *
 * Hand-rolled router with a `returnTo` hint so flows started from Settings unwind to Settings
 * rather than Home. Intent prompt journals the typed text on Open before launching.
 */
@Composable
fun StillApp() {
    val appContext = LocalContext.current.applicationContext

    val preferencesRepository = remember(appContext) { PreferencesRepository(appContext) }
    val intentJournalRepository = remember(appContext) { IntentJournalRepository(appContext) }
    val appRepository = remember(appContext) {
        AppRepository(
            packageScanner = PackageScanner(appContext.packageManager),
            preferencesRepository = preferencesRepository,
            defaultSlotResolver = DefaultSlotResolver(appContext.packageManager),
        )
    }
    val appLauncher = remember(appContext) { AppLauncher(appContext) }
    val systemSettingsPackage = remember(appContext) {
        SystemSettingsLocator(appContext.packageManager).packageName
    }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            appRepository = appRepository,
            appLauncher = appLauncher,
            intentJournalRepository = intentJournalRepository,
            systemSettingsPackage = systemSettingsPackage,
        ),
    )

    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val intentEntries by homeViewModel.intentEntries.collectAsState()
    var route by remember { mutableStateOf<StillRoute>(StillRoute.Home) }

    BackHandler(enabled = route != StillRoute.Home) {
        route = route.backTarget()
    }

    fun launchSlot(slotIndex: Int) {
        val resolved = uiState.resolvedSlots.getOrNull(slotIndex)
        when {
            resolved == null || !resolved.isLaunchable -> Unit
            resolved.slot.useFriction -> {
                route = StillRoute.IntentPrompt(slotIndex)
            }
            else -> {
                homeViewModel.launchSlot(slotIndex)
            }
        }
    }

    val typographyValues = remember(uiState.settings.fontPreset) {
        stillTypographyFor(uiState.settings.fontPreset)
    }

    CompositionLocalProvider(LocalStillTypography provides typographyValues) {
    when (val currentRoute = route) {
        StillRoute.Home -> HomeScreen(
            uiState = uiState,
            onLaunchSlot = ::launchSlot,
            onEditSlot = { index -> route = StillRoute.SlotEdit(index, ReturnTo.Home) },
            onOpenAllApps = {
                homeViewModel.refreshApps()
                route = StillRoute.AllApps
            },
            onOpenIntents = { route = StillRoute.Intents(ReturnTo.Home) },
        )

        StillRoute.AllApps -> AllAppsScreen(
            apps = uiState.apps,
            showAppIcons = uiState.settings.showAppIcons,
            onLaunchApp = { app ->
                homeViewModel.launchApp(app)
                route = StillRoute.Home
            },
            onOpenStillSettings = { route = StillRoute.Settings },
            onRefreshApps = homeViewModel::refreshApps,
            onBack = { route = StillRoute.Home },
        )

        StillRoute.Settings -> SettingsScreen(
            uiState = uiState,
            onChooseSlot = { index ->
                val resolved = uiState.resolvedSlots.getOrNull(index)
                route = if (resolved?.isLaunchable == true) {
                    StillRoute.SlotEdit(index, ReturnTo.Settings)
                } else {
                    StillRoute.AppPicker(index, ReturnTo.Settings)
                }
            },
            onChangeSlotCount = homeViewModel::setSlotCount,
            onCycleClockFormat = {
                val next = when (uiState.settings.clockFormat) {
                    ClockFormat.Auto -> ClockFormat.Hours12
                    ClockFormat.Hours12 -> ClockFormat.Hours24
                    ClockFormat.Hours24 -> ClockFormat.Auto
                }
                homeViewModel.setClockFormat(next)
            },
            onToggleShowDate = {
                homeViewModel.setShowDate(!uiState.settings.showDate)
            },
            onToggleShowHomeHint = {
                homeViewModel.setShowHomeHint(!uiState.settings.showHomeHint)
            },
            onToggleShowAppIcons = {
                homeViewModel.setShowAppIcons(!uiState.settings.showAppIcons)
            },
            onCycleFontPreset = {
                val next = when (uiState.settings.fontPreset) {
                    FontPreset.System -> FontPreset.Editorial
                    FontPreset.Editorial -> FontPreset.Terminal
                    FontPreset.Terminal -> FontPreset.Grotesk
                    FontPreset.Grotesk -> FontPreset.System
                }
                homeViewModel.setFontPreset(next)
            },
            onOpenIntents = { route = StillRoute.Intents(ReturnTo.Settings) },
            onBack = { route = StillRoute.AllApps },
        )

        is StillRoute.AppPicker -> AppPickerScreen(
            slotIndex = currentRoute.slotIndex,
            apps = uiState.apps,
            selectedApp = uiState.resolvedSlots.getOrNull(currentRoute.slotIndex)?.app,
            showAppIcons = uiState.settings.showAppIcons,
            onAppSelected = { app ->
                homeViewModel.setSlotApp(currentRoute.slotIndex, app)
                route = currentRoute.returnTo.asRoute()
            },
            onBack = { route = currentRoute.returnTo.asRoute() },
        )

        is StillRoute.SlotEdit -> {
            val resolved = uiState.resolvedSlots.getOrNull(currentRoute.slotIndex)
            if (resolved == null || !resolved.isLaunchable) {
                route = currentRoute.returnTo.asRoute()
            } else {
                SlotEditScreen(
                    resolved = resolved,
                    isSystemSettings = uiState.isSystemSettings(resolved),
                    onRename = {
                        route = StillRoute.SlotRename(currentRoute.slotIndex, currentRoute.returnTo)
                    },
                    onReplaceApp = {
                        route = StillRoute.AppPicker(currentRoute.slotIndex, currentRoute.returnTo)
                    },
                    onToggleFriction = {
                        homeViewModel.setSlotFriction(
                            currentRoute.slotIndex,
                            !resolved.slot.useFriction,
                        )
                    },
                    onRemove = {
                        homeViewModel.clearSlot(currentRoute.slotIndex)
                        route = currentRoute.returnTo.asRoute()
                    },
                    onBack = { route = currentRoute.returnTo.asRoute() },
                )
            }
        }

        is StillRoute.SlotRename -> {
            val resolved = uiState.resolvedSlots.getOrNull(currentRoute.slotIndex)
            SlotRenameScreen(
                app = resolved?.app,
                initialLabel = resolved?.slot?.customLabel,
                onSave = { label ->
                    homeViewModel.setSlotLabel(currentRoute.slotIndex, label)
                    route = currentRoute.returnTo.asRoute()
                },
                onClearLabel = {
                    homeViewModel.setSlotLabel(currentRoute.slotIndex, null)
                    route = currentRoute.returnTo.asRoute()
                },
                onBack = {
                    route = StillRoute.SlotEdit(currentRoute.slotIndex, currentRoute.returnTo)
                },
            )
        }

        is StillRoute.IntentPrompt -> {
            val resolved = uiState.resolvedSlots.getOrNull(currentRoute.slotIndex)
            IntentPromptScreen(
                slotLabel = resolved?.displayLabel ?: "open",
                onOpen = { intent ->
                    homeViewModel.launchSlotWithIntent(currentRoute.slotIndex, intent)
                    route = StillRoute.Home
                },
                onCancel = { route = StillRoute.Home },
            )
        }

        is StillRoute.Intents -> IntentsScreen(
            entries = intentEntries,
            onClear = homeViewModel::clearJournal,
            onBack = { route = currentRoute.returnTo.asRoute() },
        )
    }
    }
}

private enum class ReturnTo {
    Home, Settings;

    fun asRoute(): StillRoute = when (this) {
        Home -> StillRoute.Home
        Settings -> StillRoute.Settings
    }
}

private sealed interface StillRoute {
    data object Home : StillRoute
    data object AllApps : StillRoute
    data object Settings : StillRoute
    data class AppPicker(val slotIndex: Int, val returnTo: ReturnTo) : StillRoute
    data class SlotEdit(val slotIndex: Int, val returnTo: ReturnTo) : StillRoute
    data class SlotRename(val slotIndex: Int, val returnTo: ReturnTo) : StillRoute
    data class IntentPrompt(val slotIndex: Int) : StillRoute
    data class Intents(val returnTo: ReturnTo) : StillRoute
}

private fun StillRoute.backTarget(): StillRoute = when (this) {
    StillRoute.Home -> StillRoute.Home
    StillRoute.AllApps -> StillRoute.Home
    StillRoute.Settings -> StillRoute.AllApps
    is StillRoute.AppPicker -> returnTo.asRoute()
    is StillRoute.SlotEdit -> returnTo.asRoute()
    is StillRoute.SlotRename -> StillRoute.SlotEdit(slotIndex, returnTo)
    is StillRoute.IntentPrompt -> StillRoute.Home
    is StillRoute.Intents -> returnTo.asRoute()
}
