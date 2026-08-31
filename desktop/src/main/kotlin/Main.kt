import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "FarmSync PC Companion") {
        MaterialTheme {
            Text("Auto-Detect Save Path: %APPDATA%\\StardewValley\\Saves\nTimeline Comparison Module\nLocal Wi-Fi Server (SMB 1Gbps)")
        }
    }
}
