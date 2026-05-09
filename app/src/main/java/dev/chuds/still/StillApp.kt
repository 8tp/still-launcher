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
import dev.chuds.still.data.AppSlot
import dev.chuds.still.data.PreferencesRepository
import dev.chuds.still.launcher.AppLauncher
import dev.chuds.still.launcher.PackageScanner
import dev.chuds.still.ui.friction.FrictionScreen
import dev.chuds.still.ui.home.HomeScreen
import dev.chuds.still.ui.home.HomeViewModel
import dev.chuds.still.ui.settings.AllAppsScreen
import dev.chuds.still.ui.settings.AppPickerScreen
import dev.chuds.still.ui.settings.SettingsScreen

/**
 * Top-level Compose application shell.
 *
 * This file owns lightweight in-memory navigation. Navigation Compose is intentionally avoided
 * in the MVP to keep the launcher small and dependency-light.
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

    when (val currentRoute = route) {
        StillRoute.Home -> HomeScreen(
            uiState = uiState,
            onLaunchSlot = { slot ->
                if (slot == AppSlot.BROWSER) {
                    route = StillRoute.BrowserFriction
                } else if (!homeViewModel.launchSlot(slot)) {
                    route = StillRoute.AppPicker(slot = slot, returnHome = true)
                }
            },
            onOpenHiddenTools = {
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
            onChooseSlot = { slot -> route = StillRoute.AppPicker(slot = slot, returnHome = false) },
            onBack = { route = StillRoute.AllApps },
        )

        is StillRoute.AppPicker -> AppPickerScreen(
            slot = currentRoute.slot,
            apps = uiState.apps,
            selectedApp = uiState.appFor(currentRoute.slot),
            onAppSelected = { app ->
                homeViewModel.setAppForSlot(currentRoute.slot, app)
                route = if (currentRoute.returnHome) StillRoute.Home else StillRoute.Settings
            },
            onClear = {
                homeViewModel.clearAppForSlot(currentRoute.slot)
                route = if (currentRoute.returnHome) StillRoute.Home else StillRoute.Settings
            },
            onBack = { route = currentRoute.backTarget() },
        )

        StillRoute.BrowserFriction -> FrictionScreen(
            title = "Browser",
            prompt = "Use this intentionally.",
            onOpen = {
                if (homeViewModel.launchSlot(AppSlot.BROWSER)) {
                    route = StillRoute.Home
                } else {
                    route = StillRoute.AppPicker(slot = AppSlot.BROWSER, returnHome = true)
                }
            },
            onCancel = { route = StillRoute.Home },
        )
    }
}

/**
 * Tiny route model for the MVP.
 */
private sealed interface StillRoute {
    data object Home : StillRoute
    data object AllApps : StillRoute
    data object Settings : StillRoute
    data object BrowserFriction : StillRoute
    data class AppPicker(val slot: AppSlot, val returnHome: Boolean) : StillRoute
}

private fun StillRoute.backTarget(): StillRoute = when (this) {
    StillRoute.Home -> StillRoute.Home
    StillRoute.AllApps -> StillRoute.Home
    StillRoute.Settings -> StillRoute.AllApps
    StillRoute.BrowserFriction -> StillRoute.Home
    is StillRoute.AppPicker -> if (returnHome) StillRoute.Home else StillRoute.Settings
}
