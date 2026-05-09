package dev.chuds.still

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.chuds.still.data.AppRepository
import dev.chuds.still.data.PreferencesRepository
import dev.chuds.still.launcher.AppLauncher
import dev.chuds.still.launcher.PackageScanner
import dev.chuds.still.ui.friction.FrictionScreen
import dev.chuds.still.ui.home.HomeScreen
import dev.chuds.still.ui.home.HomeViewModel
import dev.chuds.still.ui.home.SlotEditScreen
import dev.chuds.still.ui.home.SlotRenameScreen
import dev.chuds.still.ui.settings.AllAppsScreen
import dev.chuds.still.ui.settings.AppPickerScreen
import dev.chuds.still.ui.settings.SettingsScreen

/**
 * Top-level Compose application shell with a hand-rolled router.
 *
 * Slots are anonymous indices (0..SLOT_COUNT-1). Each slot stores its app, an optional custom
 * label, and a friction flag. The router carries a `returnTo` hint so flows started from
 * Settings unwind to Settings rather than Home.
 */
@Composable
fun StillApp() {
    val appContext = LocalContext.current.applicationContext

    val preferencesRepository = remember(appContext) { PreferencesRepository(appContext) }
    val appRepository = remember(appContext) {
        AppRepository(
            packageScanner = PackageScanner(appContext.packageManager),
            preferencesRepository = preferencesRepository,
        )
    }
    val appLauncher = remember(appContext) { AppLauncher(appContext) }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            appRepository = appRepository,
            appLauncher = appLauncher,
        ),
    )

    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var route by remember { mutableStateOf<StillRoute>(StillRoute.Home) }

    BackHandler(enabled = route != StillRoute.Home) {
        route = route.backTarget()
    }

    fun launchSlot(slotIndex: Int) {
        val resolved = uiState.resolvedSlots.getOrNull(slotIndex)
        when {
            resolved == null -> Unit
            resolved.slot.useFriction && resolved.isLaunchable -> {
                route = StillRoute.Friction(slotIndex)
            }
            !homeViewModel.launchSlot(slotIndex) -> {
                route = StillRoute.AppPicker(slotIndex, ReturnTo.Home)
            }
        }
    }

    when (val currentRoute = route) {
        StillRoute.Home -> HomeScreen(
            uiState = uiState,
            onLaunchSlot = ::launchSlot,
            onAddSlotApp = { index -> route = StillRoute.AppPicker(index, ReturnTo.Home) },
            onEditSlot = { index -> route = StillRoute.SlotEdit(index, ReturnTo.Home) },
            onOpenAllApps = {
                homeViewModel.refreshApps()
                route = StillRoute.AllApps
            },
        )

        StillRoute.AllApps -> AllAppsScreen(
            apps = uiState.apps,
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
            onBack = { route = StillRoute.AllApps },
        )

        is StillRoute.AppPicker -> AppPickerScreen(
            slotIndex = currentRoute.slotIndex,
            apps = uiState.apps,
            selectedApp = uiState.resolvedSlots.getOrNull(currentRoute.slotIndex)?.app,
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

        is StillRoute.Friction -> {
            val resolved = uiState.resolvedSlots.getOrNull(currentRoute.slotIndex)
            FrictionScreen(
                title = resolved?.displayLabel ?: "open",
                prompt = "Use this intentionally.",
                onOpen = {
                    val launched = resolved?.app?.let { homeViewModel.launchApp(it) } ?: false
                    route = if (launched) StillRoute.Home else StillRoute.Home
                },
                onCancel = { route = StillRoute.Home },
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
    data class Friction(val slotIndex: Int) : StillRoute
}

private fun StillRoute.backTarget(): StillRoute = when (this) {
    StillRoute.Home -> StillRoute.Home
    StillRoute.AllApps -> StillRoute.Home
    StillRoute.Settings -> StillRoute.AllApps
    is StillRoute.AppPicker -> returnTo.asRoute()
    is StillRoute.SlotEdit -> returnTo.asRoute()
    is StillRoute.SlotRename -> StillRoute.SlotEdit(slotIndex, returnTo)
    is StillRoute.Friction -> StillRoute.Home
}
