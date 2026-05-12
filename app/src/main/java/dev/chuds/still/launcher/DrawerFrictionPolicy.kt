package dev.chuds.still.launcher

import dev.chuds.still.data.DrawerFrictionMode
import dev.chuds.still.data.LaunchableApp
import dev.chuds.still.data.LauncherSettings

fun drawerFrictionKey(packageName: String, className: String?): String =
    "$packageName/${className.orEmpty()}"

fun drawerFrictionKey(app: LaunchableApp): String =
    drawerFrictionKey(app.packageName, app.className)

fun requiresDrawerFriction(
    app: LaunchableApp,
    settings: LauncherSettings,
): Boolean {
    val key = drawerFrictionKey(app)
    val isListed = key in settings.drawerFrictionExceptions
    return when (settings.drawerFrictionMode) {
        DrawerFrictionMode.Off -> false
        DrawerFrictionMode.Allowlist -> !isListed
        DrawerFrictionMode.Blocklist -> isListed
    }
}
