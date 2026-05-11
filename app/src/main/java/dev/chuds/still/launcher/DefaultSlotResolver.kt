package dev.chuds.still.launcher

import android.content.Intent
import android.content.pm.PackageManager
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
        val target = resolveComponent(category.intentSpec().toIntent()) ?: return null
        return matchResolvedDefault(target, apps)
    }

    private fun resolveComponent(intent: Intent): ResolvedDefaultComponent? {
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
        val activity = resolveInfo?.activityInfo ?: return null
        val packageName = activity.packageName.takeIf { it.isNotBlank() } ?: return null
        val className = activity.name.takeIf { it.isNotBlank() }
        return ResolvedDefaultComponent(packageName = packageName, className = className)
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

internal data class ResolvedDefaultComponent(
    val packageName: String,
    val className: String?,
)

internal fun matchResolvedDefault(
    resolved: ResolvedDefaultComponent,
    apps: List<LaunchableApp>,
): LaunchableApp? {
    resolved.className?.let { resolvedClass ->
        return apps.firstOrNull {
            it.packageName == resolved.packageName && it.className == resolvedClass
        }
    }
    return apps.firstOrNull { it.packageName == resolved.packageName }
}

internal data class DefaultIntentSpec(
    val action: String,
    val category: String? = null,
) {
    fun toIntent(): Intent = Intent(action).apply {
        category?.let { addCategory(it) }
    }
}

internal enum class DefaultCategory {
    Phone,
    Messages,
    Browser,
    Camera,
    Calendar,
    Settings;

    fun intentSpec(): DefaultIntentSpec = when (this) {
        Phone -> DefaultIntentSpec(Intent.ACTION_DIAL)
        Messages -> DefaultIntentSpec(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING)
        Browser -> DefaultIntentSpec(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
        Camera -> DefaultIntentSpec(MediaStore.ACTION_IMAGE_CAPTURE)
        Calendar -> DefaultIntentSpec(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR)
        Settings -> DefaultIntentSpec(AndroidSettings.ACTION_SETTINGS)
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
