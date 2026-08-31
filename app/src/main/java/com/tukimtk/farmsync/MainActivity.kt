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
import com.tukimtk.farmsync.ui.StorageConfigDialog
import com.tukimtk.farmsync.ui.SuccessFeedbackDialog


class MainActivity : ComponentActivity() {
    private var incomingZipUri: Uri? = null


    override fun onResume() {
        super.onResume()
        com.tukimtk.farmsync.shizuku.ShizukuStateManager.refresh(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_VIEW) {
            incomingZipUri = intent.data
        }

        setContent {
            FarmSyncAppTheme {
                MainAppScaffold(
                    incomingZipUri = incomingZipUri,
                    onClearIncomingZip = { incomingZipUri = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_VIEW) {
            incomingZipUri = intent.data
            setContent {
                FarmSyncAppTheme {
                    MainAppScaffold(
                        incomingZipUri = incomingZipUri,
                        onClearIncomingZip = { incomingZipUri = null }
                    )
                }
            }
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

@Composable
fun SettingsTabContent(onOpenApiKey: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val repo = remember { SaveStateRepository(context) }

    var selectedStorage by remember { mutableStateOf(repo.getSelectedStorage()) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf<String?>(null) }

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
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Strings.currentLanguage = AppLanguage.TH
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (Strings.currentLanguage == AppLanguage.TH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (Strings.currentLanguage == AppLanguage.TH) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("🇹🇭 ภาษาไทย")
                    }
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Strings.currentLanguage = AppLanguage.EN
                        },
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
                    text = "${Strings.get("ปัจจุบัน", "Current")}: $selectedStorage",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showStorageDialog = true
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(Strings.get("⚙️ เลือกและตั้งค่า Cloud / SMB", "⚙️ Configure Cloud / Storage"))
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
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenApiKey()
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(Strings.get("🔑 ตั้งค่า API Key", "🔑 Configure API Key"))
                }
            }
        }
    }

    if (showStorageDialog) {
        StorageConfigDialog(
            currentSelection = selectedStorage,
            onSave = { newStorage ->
                repo.setSelectedStorage(newStorage)
                selectedStorage = newStorage
                showStorageDialog = false
                showSuccessToast = Strings.get("บันทึกช่องทางซิงค์: $newStorage เรียบร้อยแล้ว", "Saved sync provider: $newStorage")
            },
            onDismiss = { showStorageDialog = false }
        )
    }

    showSuccessToast?.let { msg ->
        SuccessFeedbackDialog(
            title = Strings.get("ตั้งค่าสำเร็จ!", "Configuration Saved!"),
            message = msg,
            onDismiss = { showSuccessToast = null }
        )
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
