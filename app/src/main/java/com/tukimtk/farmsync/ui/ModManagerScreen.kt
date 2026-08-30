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
fun ModManagerScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var sveEnabled by remember { mutableStateOf(true) }
    var uiInfoEnabled by remember { mutableStateOf(true) }
    var translationEnabled by remember { mutableStateOf(true) }
    var showInstallDialog by remember { mutableStateOf<String?>(null) }

    // SAF Zip File Picker
    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showInstallDialog = Strings.get(
                "ติดตั้งม็อดจากไฟล์สำเร็จ: ${uri.lastPathSegment ?: "Mod.zip"}",
                "Successfully installed mod from: ${uri.lastPathSegment ?: "Mod.zip"}"
            )
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
                text = { Text(Strings.get("ม็อดในเครื่อง", "Installed Mods")) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedTab = 1
                },
                text = { Text(Strings.get("🌐 คลังม็อดยอดนิยม (Download)", "🌐 Popular Mods")) }
            )
        }

        if (selectedTab == 0) {
            Text(
                text = Strings.get(
                    "เปิด/ปิดการทำงานของม็อดในโฟลเดอร์ /Android/data/com.zane.stardewvalley/files/Mods/ ได้ทันที",
                    "Enable or disable installed mods directly in your game Mods folder."
                ),
                fontSize = 13.sp,
                color = Color.Gray
            )

            // Installed Mod 1
            ModCard(
                name = "Stardew Valley Expanded",
                author = "FlashShifter",
                version = "v1.14.24",
                isEnabled = sveEnabled,
                onToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    sveEnabled = it
                }
            )

            // Installed Mod 2
            ModCard(
                name = "UI Info Suite 2",
                author = "Annosz",
                version = "v2.3.3",
                isEnabled = uiInfoEnabled,
                onToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    uiInfoEnabled = it
                }
            )

            // Installed Mod 3
            ModCard(
                name = Strings.get("ม็อดแปลบทสนทนาภาษาไทย (AI BYOK)", "AI Thai Dialogue Localization"),
                author = "FarmSync AI (BYOK)",
                version = "v1.0.0",
                isEnabled = translationEnabled,
                onToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    translationEnabled = it
                }
            )
        } else {
            Text(
                text = Strings.get(
                    "กดที่ปุ่ม 'ดาวน์โหลด' เพื่อเปิดหน้า Nexus Mods / GitHub โหลดไฟล์ .zip แล้วกดติดตั้งเข้าเกมได้ทันที",
                    "Tap 'Download' to open the mod page on Nexus Mods / GitHub, download the .zip, and install."
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
                            Text("🌐 ${Strings.get("เปิดหน้าดาวน์โหลดม็อด (Download)", "Open Mod Download Page")}")
                        }
                    }
                }
            }
        }

        // Install result feedback dialog
        showInstallDialog?.let { msg ->
            SuccessFeedbackDialog(
                title = Strings.get("ติดตั้งม็อดสำเร็จ!", "Mod Installed!"),
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
