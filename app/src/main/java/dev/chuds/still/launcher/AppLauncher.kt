package dev.chuds.still.launcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import dev.chuds.still.data.LaunchableApp

/**
 * Launches a selected app activity.
 *
 * The launcher uses explicit package/class components selected by the user, which avoids broad
 * intent guessing and keeps behavior predictable.
 */
class AppLauncher(
    private val context: Context,
) {
    fun launch(app: LaunchableApp): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = app.componentName
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}
