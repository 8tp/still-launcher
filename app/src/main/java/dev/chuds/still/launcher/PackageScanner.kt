package dev.chuds.still.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import dev.chuds.still.data.LaunchableApp

/**
 * Reads installed launchable apps using a scoped ACTION_MAIN + CATEGORY_LAUNCHER query.
 *
 * This intentionally avoids QUERY_ALL_PACKAGES. The manifest declares a matching <queries>
 * intent so Android 11+ package visibility remains limited to launchable apps.
 */
class PackageScanner(
    private val packageManager: PackageManager,
) {
    fun loadLaunchableApps(): List<LaunchableApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(launcherIntent, 0)
            }
        } catch (_: RuntimeException) {
            emptyList()
        }

        return resolveInfos
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val label = resolveInfo.loadLabel(packageManager)
                    ?.toString()
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: activityInfo.packageName

                LaunchableApp(
                    label = label,
                    packageName = activityInfo.packageName,
                    className = activityInfo.name,
                )
            }
            .distinctBy { it.componentKey }
            .sortedWith(
                compareBy<LaunchableApp>(String.CASE_INSENSITIVE_ORDER) { it.label }
                    .thenBy { it.packageName }
                    .thenBy { it.className },
            )
    }
}
