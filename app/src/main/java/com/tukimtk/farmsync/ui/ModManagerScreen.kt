package com.tukimtk.farmsync.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.i18n.Strings
import com.tukimtk.farmsync.mods.ModInstaller

data class InstalledModItem(
    val id: String,
    val name: String,
    val author: String,
    val version: String,
    var isEnabled: Boolean = true
)

data class ModDownloadItem(
    val name: String,
    val author: String,
    val descriptionTh: String,
    val descriptionEn: String,
    val nexusUrl: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModManagerScreen(incomingZipUri: Uri? = null, onClearIncomingZip: () -> Unit = {}) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val installer = remember { ModInstaller(context) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var installedMods by remember {
        mutableStateOf(
            listOf(
                InstalledModItem("1", "Stardew Valley Expanded", "FlashShifter", "v1.14.24", true),
                InstalledModItem("2", "UI Info Suite 2", "Annosz", "v2.3.3", true),
                InstalledModItem("3", Strings.get("ม็อดแปลบทสนทนาภาษาไทย (AI BYOK)", "AI Thai Dialogue Localization"), "FarmSync AI", "v1.0.0", true)
            )
        )
    }

    var showInstallDialog by remember { mutableStateOf<String?>(null) }
    var isInstalling by remember { mutableStateOf(false) }

    fun processZipInstall(uri: Uri) {
        isInstalling = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val result = installer.installModFromUri(uri)
        isInstalling = false

        if (result.isSuccess) {
            installedMods = installedMods + InstalledModItem(
                id = System.currentTimeMillis().toString(),
                name = result.modName,
                author = result.author,
                version = result.version,
                isEnabled = true
            )
            showInstallDialog = Strings.get(
                "✓ ติดตั้งม็อด '${result.modName}' (${result.version}) โดย ${result.author} สำเร็จเรียบร้อยแล้ว! (แตกไฟล์ ${result.extractedFilesCount} รายการเข้าสู่โฟลเดอร์ Mods)",
                "✓ Successfully installed '${result.modName}' (${result.version}) by ${result.author}! (${result.extractedFilesCount} files extracted to Mods directory)"
            )
        } else {
            showInstallDialog = Strings.get(
                "❌ ไม่สามารถติดตั้งม็อดได้: ${result.message}",
                "❌ Failed to install mod: ${result.message}"
            )
        }
    }

    // Auto-process incoming zip if opened from external download
    LaunchedEffect(incomingZipUri) {
        incomingZipUri?.let { uri ->
            processZipInstall(uri)
            onClearIncomingZip()
        }
    }

    // SAF Zip File Picker
    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processZipInstall(uri)
        }
    }

    val popularMods = listOf(
        ModDownloadItem(
            name = "Stardew Valley Expanded (SVE)",
            author = "FlashShifter",
            descriptionTh = "ม็อดขยายเนื้อเรื่องอันดับ 1 เพิ่มตัวละคร พื้นที่ เควส และอีเวนต์ใหม่มากมาย",
            descriptionEn = "The #1 expansion mod adding dozens of NPCs, maps, quests, and events.",
            nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/3753",
            category = "Expansion"
        ),
        ModDownloadItem(
            name = "UI Info Suite 2",
            author = "Annosz",
            descriptionTh = "แสดงไอคอนบอกสิ่งที่ต้องทำ ดูสภาพอากาศ วันเกิดชาวบ้าน และราคาขายผลผลิต",
            descriptionEn = "Displays luck, weather, birthday reminders, and crop harvest timers.",
            nexusUrl = "https://github.com/Annosz/UIInfoSuite2/releases",
            category = "Quality of Life"
        ),
        ModDownloadItem(
            name = "Content Patcher (CP)",
            author = "Pathoschild",
            descriptionTh = "ม็อดรากฐานที่จำเป็นที่สุดสำหรับใช้โหลดม็อดกราฟิก และเนื้อเรื่องเสริม 1.6",
            descriptionEn = "Crucial framework mod required for modern graphic and content mods.",
            nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/1915",
            category = "Framework"
        ),
        ModDownloadItem(
            name = "Automate",
            author = "Pathoschild",
            descriptionTh = "ระบบเชื่อมต่อกล่องกับเตาหลอม/ถังหมัก ให้ทำงานอัตโนมัติ 100%",
            descriptionEn = "Automatically pulls resources from chests into nearby machines.",
            nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/1063",
            category = "Automation"
        ),
        ModDownloadItem(
            name = "NPC Map Locations",
            author = "Bouhm",
            descriptionTh = "แสดงตำแหน่งตัวละคร NPC ทุกคนบนแผนที่แบบ Real-time",
            descriptionEn = "Shows real-time positions of all villagers directly on the game map.",
            nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/239",
            category = "Quality of Life"
        ),
        ModDownloadItem(
            name = "Ridgeside Village",
            author = "Rafseazz",
            descriptionTh = "เพิ่มหมู่บ้านขนาดใหญ่บนยอดเขา พร้อมชาวบ้านใหม่กว่า 50 คน",
            descriptionEn = "Adds an entire new mountain village with 50+ voiced characters.",
            nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/7286",
            category = "Expansion"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Install Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Strings.get("📦 ตัวจัดการม็อด (Mod Manager)", "📦 1-Click Mod Manager"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    zipPickerLauncher.launch("application/zip")
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(Strings.get("+ ติดตั้งไฟล์ .zip", "+ Install .zip"))
            }
        }

        // Tab Selector (Installed vs Popular Store)
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedTab = 0
                },
                text = { Text("${Strings.get("ม็อดในเครื่อง", "Installed Mods")} (${installedMods.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedTab = 1
                },
                text = { Text(Strings.get("🌐 คลังม็อดยอดนิยม", "🌐 Popular Mods")) }
            )
        }

        if (selectedTab == 0) {
            Text(
                text = Strings.get(
                    "เปิด/ปิดการทำงานของม็อดในโฟลเดอร์เกมได้ทันที หรือกดปุ่ม '+ ติดตั้งไฟล์ .zip' เพื่อให้แอปช่วยแตกไฟล์และติดตั้งให้อัตโนมัติ",
                    "Enable/disable mods or tap '+ Install .zip' to let FarmSync AI extract and install automatically."
                ),
                fontSize = 13.sp,
                color = Color.Gray
            )

            installedMods.forEach { mod ->
                ModCard(
                    name = mod.name,
                    author = mod.author,
                    version = mod.version,
                    isEnabled = mod.isEnabled,
                    onToggle = { isChecked: Boolean ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        installedMods = installedMods.map {
                            if (it.id == mod.id) it.copy(isEnabled = isChecked) else it
                        }
                    }
                )
            }
        } else {
            Text(
                text = Strings.get(
                    "กดปุ่ม 'ดาวน์โหลดม็อด' เพื่อโหลดไฟล์ .zip จาก Nexus Mods / GitHub เมื่อโหลดเสร็จ แอป FarmSync AI จะช่วยเปิดและติดตั้งลงเกมให้อัตโนมัติ",
                    "Tap 'Download' to download the .zip mod. When finished, FarmSync AI can open and install it automatically."
                ),
                fontSize = 13.sp,
                color = Color.Gray
            )

            popularMods.forEach { mod ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mod.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            AssistChip(
                                onClick = {},
                                label = { Text(mod.category, fontSize = 11.sp) }
                            )
                        }
                        Text("By ${mod.author}", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = Strings.get(mod.descriptionTh, mod.descriptionEn),
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mod.nexusUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🌐 ${Strings.get("ดาวน์โหลดม็อด .zip (Download)", "Download .zip Mod")}")
                        }
                    }
                }
            }
        }

        // Install result feedback dialog
        showInstallDialog?.let { msg ->
            SuccessFeedbackDialog(
                title = Strings.get("ผลการติดตั้งม็อด", "Mod Installation Result"),
                message = msg,
                onDismiss = { showInstallDialog = null }
            )
        }
    }
}

@Composable
fun ModCard(
    name: String,
    author: String,
    version: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("By $author | $version", fontSize = 13.sp, color = Color.Gray)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}
