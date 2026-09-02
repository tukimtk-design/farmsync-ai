package com.tukimtk.farmsync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.data.SaveStateRepository
import com.tukimtk.farmsync.i18n.AppLanguage
import com.tukimtk.farmsync.i18n.Strings
import com.tukimtk.farmsync.ui.ApiKeyConfigDialog
import com.tukimtk.farmsync.ui.FarmDashboardScreen
import com.tukimtk.farmsync.ui.ModManagerScreen
import com.tukimtk.farmsync.ui.SaveEditorScreen
import com.tukimtk.farmsync.ui.ShizukuOnboardingScreen
import com.tukimtk.farmsync.ui.SettingsTabContent
import com.tukimtk.farmsync.ui.theme.FarmSyncAppTheme
import com.tukimtk.farmsync.ui.SettingsTabContent
import com.tukimtk.farmsync.ui.StorageConfigDialog
import com.tukimtk.farmsync.ui.SuccessFeedbackDialog


class MainActivity : ComponentActivity() {
    private var incomingZipUriState = mutableStateOf<Uri?>(null)


    override fun onResume() {
        super.onResume()
        com.tukimtk.farmsync.shizuku.ShizukuStateManager.refresh(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register Shizuku binder listeners early so permission state is ready
        com.tukimtk.farmsync.shizuku.ShizukuStateManager.init(this)

        if (intent?.action == Intent.ACTION_VIEW) {
            incomingZipUriState.value = intent.data
        }

        setContent {
            FarmSyncAppTheme {
                MainAppScaffold(
                    incomingZipUri = incomingZipUriState.value,
                    onClearIncomingZip = { incomingZipUriState.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_VIEW) {
            incomingZipUriState.value = intent.data
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

enum class NavigationTab(val titleTh: String, val titleEn: String, val icon: ImageVector) {
    DASHBOARD("หน้าหลัก", "Dashboard", Icons.Default.Home),
    EDITOR("แก้ไขเซฟ", "Editor", Icons.Default.Edit),
    MODS("ม็อด", "Mods", Icons.Default.Build),
    SHIZUKU("Shizuku", "Shizuku", Icons.Default.Info),
    SETTINGS("ตั้งค่า", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    incomingZipUri: Uri? = null,
    onClearIncomingZip: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var currentTab by remember { mutableStateOf(if (incomingZipUri != null) NavigationTab.MODS else NavigationTab.DASHBOARD) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(incomingZipUri) {
        if (incomingZipUri != null) {
            currentTab = NavigationTab.MODS
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🌾 FarmSync AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Language Switcher Toggle
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Strings.currentLanguage = if (Strings.currentLanguage == AppLanguage.TH) AppLanguage.EN else AppLanguage.TH
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = if (Strings.currentLanguage == AppLanguage.TH) "🇹🇭 TH" else "🇬🇧 EN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showApiKeyDialog = true
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "BYOK API Key")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentTab = tab
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.titleEn) },
                        label = { Text(Strings.get(tab.titleTh, tab.titleEn), fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> FarmDashboardScreen()
                NavigationTab.EDITOR -> SaveEditorScreen()
                NavigationTab.MODS -> ModManagerScreen(
                    incomingZipUri = incomingZipUri,
                    onClearIncomingZip = onClearIncomingZip
                )
                NavigationTab.SHIZUKU -> ShizukuOnboardingScreen()
                NavigationTab.SETTINGS -> SettingsTabContent(onOpenApiKey = { showApiKeyDialog = true })
            }
        }

        if (showApiKeyDialog) {
            ApiKeyConfigDialog(onDismiss = { showApiKeyDialog = false })
        }
    }
}

