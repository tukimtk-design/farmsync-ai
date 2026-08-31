package com.tukimtk.farmsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tukimtk.farmsync.ui.FarmDashboardScreen
import com.tukimtk.farmsync.ui.ModManagerScreen
import com.tukimtk.farmsync.ui.ShizukuOnboardingScreen
import com.tukimtk.farmsync.ui.StorageSettingsScreen
import com.tukimtk.farmsync.ui.theme.FarmSyncAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmSyncAppTheme {
                MainAppScaffold()
            }
        }
    }
}

@Composable
fun MainAppScaffold() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Mods", "Shizuku", "Settings")

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Text(title.take(1)) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> FarmDashboardScreen()
                1 -> ModManagerScreen()
                2 -> ShizukuOnboardingScreen()
                3 -> StorageSettingsScreen()
            }
        }
    }
}
