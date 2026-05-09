package dev.chuds.still

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.chuds.still.ui.theme.StillTheme

/**
 * Single Activity entry point for Still.
 *
 * The manifest marks this activity as both HOME/DEFAULT and LAUNCHER. Android can therefore
 * offer Still as the default Home app, while the optional LAUNCHER filter makes development
 * and first-time setup easier.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        setContent {
            StillTheme {
                StillApp()
            }
        }
    }
}
