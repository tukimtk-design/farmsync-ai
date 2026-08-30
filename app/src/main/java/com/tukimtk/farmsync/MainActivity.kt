package com.tukimtk.farmsync

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.i18n.AppLanguage
import com.tukimtk.farmsync.i18n.Strings
import com.tukimtk.farmsync.ui.ApiKeyConfigDialog
import com.tukimtk.farmsync.ui.FarmDashboardScreen
import com.tukimtk.farmsync.ui.ModManagerScreen
import com.tukimtk.farmsync.ui.SaveEditorScreen
import com.tukimtk.farmsync.ui.ShizukuOnboardingScreen

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

enum class NavigationTab(val titleTh: String, val titleEn: String, val icon: ImageVector) {
    DASHBOARD("หน้าหลัก", "Dashboard", Icons.Default.Home),
    EDITOR("แก้ไขเซฟ", "Editor", Icons.Default.Edit),
    MODS("ม็อด", "Mods", Icons.Default.Build),
    SHIZUKU("Shizuku", "Shizuku", Icons.Default.Info),
    SETTINGS("ตั้งค่า", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold() {
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

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

                    IconButton(onClick = { showApiKeyDialog = true }) {
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
                        onClick = { currentTab = tab },
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
                NavigationTab.MODS -> ModManagerScreen()
                NavigationTab.SHIZUKU -> ShizukuOnboardingScreen()
                NavigationTab.SETTINGS -> SettingsTabContent(onOpenApiKey = { showApiKeyDialog = true })
            }
        }

        if (showApiKeyDialog) {
            ApiKeyConfigDialog(onDismiss = { showApiKeyDialog = false })
        }
    }
}

@Composable
fun SettingsTabContent(onOpenApiKey: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(Strings.get("⚙️ ตั้งค่าแอปพลิเคชัน", "⚙️ Application Settings"), fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Language Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(Strings.get("🌐 ภาษาการแสดงผล (Language)", "🌐 Display Language"), fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { Strings.currentLanguage = AppLanguage.TH },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (Strings.currentLanguage == AppLanguage.TH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (Strings.currentLanguage == AppLanguage.TH) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("🇹🇭 ภาษาไทย")
                    }
                    Button(
                        onClick = { Strings.currentLanguage = AppLanguage.EN },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (Strings.currentLanguage == AppLanguage.EN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (Strings.currentLanguage == AppLanguage.EN) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("🇬🇧 English")
                    }
                }
            }
        }

        // Storage Provider Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(Strings.get("📁 ผู้ให้บริการซิงค์ข้อมูล (Storage Provider)", "📁 Storage & Sync Provider"), fontWeight = FontWeight.SemiBold)
                Text(
                    Strings.get("ปัจจุบัน: Wi-Fi วงแลนในบ้าน SMB (ความเร็วตรง 1 Gbps)", "Current: Local Wi-Fi SMB (1 Gbps Direct)"),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Button(onClick = { /* Change Provider */ }) {
                    Text(Strings.get("เลือกคลาวด์ / พื้นที่จัดเก็บ", "Select Cloud / Storage Provider"))
                }
            }
        }

        // AI Translation BYOK Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(Strings.get("🤖 ระบบแปลภาษา AI (BYOK Gemini)", "🤖 AI Translation (BYOK Gemini)"), fontWeight = FontWeight.SemiBold)
                Text(
                    Strings.get("ใส่ Gemini API Key ส่วนตัวเพื่อแปลม็อดเป็นภาษาไทยได้ฟรีไม่จำกัด", "Configure your Gemini API Key for zero-cost Thai mod translation."),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Button(onClick = onOpenApiKey) {
                    Text(Strings.get("ตั้งค่า API Key", "Configure API Key"))
                }
            }
        }
    }
}

@Composable
fun FarmSyncAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF436E35),
            primaryContainer = Color(0xFFC7EFA7),
            onPrimaryContainer = Color(0xFF0F2000),
            secondary = Color(0xFF5B624B),
            background = Color(0xFFFDFCF4),
            surface = Color(0xFFFDFCF4)
        ),
        content = content
    )
}
