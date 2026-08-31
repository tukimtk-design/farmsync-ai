package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tukimtk.farmsync.shizuku.ShizukuStateManager

@Composable
fun ShizukuToolsScreen() {
    val isShizukuReady = ShizukuStateManager.isAvailable.value
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Advanced Shizuku Tools", modifier = Modifier.padding(bottom = 16.dp))

        if (!isShizukuReady) {
            Text(
                "Shizuku is not connected. These tools require elevated permissions to access Android/data.",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Text(
                "Status: Connected",
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = { ShizukuStateManager.checkShizuku() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Verify Shizuku Permission")
        }

        Button(
            onClick = { /* Trigger SyncCoordinator */ },
            enabled = isShizukuReady,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("1-Click Sync (Android/data)")
        }

        Button(
            onClick = { /* Trigger ModInstaller */ },
            enabled = isShizukuReady
        ) {
            Text("+ Install .zip Mod")
        }
    }
}
