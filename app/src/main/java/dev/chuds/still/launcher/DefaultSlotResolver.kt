package dev.chuds.still.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings as AndroidSettings
import dev.chuds.still.data.HomeSlot
import dev.chuds.still.data.LaunchableApp

/**
 * Resolves the canonical app for each first-launch default category.
 *
 * Each category is tried independently — categories that don't resolve (or whose package isn't
 * in the launchable list) leave their slot empty, so the user can fill it manually. The
 * resolver matches against the existing launchable list rather than introspecting packages
 * directly, so we never widen visibility beyond what the user can already see in all-apps.
 */
class DefaultSlotResolver(
    private val packageManager: PackageManager,
) {
    fun resolve(apps: List<LaunchableApp>): List<HomeSlot> {
        if (apps.isEmpty()) return emptyList()

        return DEFAULT_CATEGORIES.mapIndexedNotNull { index, category ->
            val app = resolveApp(category, apps) ?: return@mapIndexedNotNull null
            HomeSlot(
                index = index,
                packageName = app.packageName,
                className = app.className,
            )
        }
    }

    private fun resolveApp(category: DefaultCategory, apps: List<LaunchableApp>): LaunchableApp? {
        val targetPackage = resolvePackageName(category.intent()) ?: return null
        return apps.firstOrNull { it.packageName == targetPackage }
            ?: apps.firstOrNull { it.packageName.startsWith(targetPackage) }
    }

    private fun resolvePackageName(intent: Intent): String? {
        val resolveInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.resolveActivity(intent, 0)
            }
        } catch (_: RuntimeException) {
            null
        }
        return resolveInfo?.activityInfo?.packageName?.takeIf { it.isNotBlank() }
    }

    private enum class DefaultCategory {
        Phone,
        Messages,
        Browser,
        Camera,
        Calendar,
        Settings;

        fun intent(): Intent = when (this) {
            Phone -> Intent(Intent.ACTION_DIAL)
            Messages -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)
            Browser -> Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_APP_BROWSER)
                .also { it.data = Uri.parse("https://") }
            Camera -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            Calendar -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
            Settings -> Intent(AndroidSettings.ACTION_SETTINGS)
        }
    }

    companion object {
        private val DEFAULT_CATEGORIES = listOf(
            DefaultCategory.Phone,
            DefaultCategory.Messages,
            DefaultCategory.Browser,
            DefaultCategory.Camera,
            DefaultCategory.Calendar,
            DefaultCategory.Settings,
        )
    }
}

/**
 * Resolves the system Settings package once. Used to enforce that the Settings slot is never
 * gated by the intent prompt — Settings is the escape hatch and must always be one tap away.
 */
class SystemSettingsLocator(
    private val packageManager: PackageManager,
) {
    val packageName: String? by lazy { resolve() }

    private fun resolve(): String? {
        val intent = Intent(AndroidSettings.ACTION_SETTINGS)
        val resolveInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.resolveActivity(intent, 0)
            }
        } catch (_: RuntimeException) {
            null
        }
        return resolveInfo?.activityInfo?.packageName?.takeIf { it.isNotBlank() }
    }
}
