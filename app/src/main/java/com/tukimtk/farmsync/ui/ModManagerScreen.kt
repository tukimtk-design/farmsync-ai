package com.tukimtk.farmsync.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.tukimtk.farmsync.mods.ModInstallResult
import com.tukimtk.farmsync.mods.ModInstaller

data class InstalledModItem(
    val id: String,
    val name: String,
    val author: String,
    val version: String,
    var isEnabled: Boolean = true
)

data class ModDownloadItem(
    val idKey: String,
    val name: String,
    val author: String,
    val descriptionTh: String,
    val descriptionEn: String,
    val nexusUrl: String,
    val category: String
)

data class PendingUpdateInfo(
    val existingMod: InstalledModItem,
    val newResult: ModInstallResult
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
                InstalledModItem("sve", "Stardew Valley Expanded", "FlashShifter", "v1.14.24", true),
                InstalledModItem("ui_info", "UI Info Suite 2", "Annosz", "v2.3.3", true),
                InstalledModItem("thai_ai", Strings.get("ม็อดแปลบทสนทนาภาษาไทย (AI BYOK)", "AI Thai Dialogue Localization"), "FarmSync AI", "v1.0.0", true)
            )
        )
    }

    var showInstallDialog by remember { mutableStateOf<String?>(null) }
    var pendingUpdate by remember { mutableStateOf<PendingUpdateInfo?>(null) }
    var modToDelete by remember { mutableStateOf<InstalledModItem?>(null) }
    var isInstalling by remember { mutableStateOf(false) }

    val allPopularMods = remember {
        listOf(
            ModDownloadItem(
                idKey = "sve",
                name = "Stardew Valley Expanded (SVE)",
                author = "FlashShifter",
                descriptionTh = "ม็อดขยายเนื้อเรื่องอันดับ 1 เพิ่มตัวละคร พื้นที่ เควส และอีเวนต์ใหม่มากมาย",
                descriptionEn = "The #1 expansion mod adding dozens of NPCs, maps, quests, and events.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/3753",
                category = "Expansion"
            ),
            ModDownloadItem(
                idKey = "ui_info",
                name = "UI Info Suite 2",
                author = "Annosz",
                descriptionTh = "แสดงไอคอนบอกสิ่งที่ต้องทำ ดูสภาพอากาศ วันเกิดชาวบ้าน และราคาขายผลผลิต",
                descriptionEn = "Displays luck, weather, birthday reminders, and crop harvest timers.",
                nexusUrl = "https://github.com/Annosz/UIInfoSuite2/releases",
                category = "Quality of Life"
            ),
            ModDownloadItem(
                idKey = "cp",
                name = "Content Patcher (CP)",
                author = "Pathoschild",
                descriptionTh = "ม็อดรากฐานที่จำเป็นที่สุดสำหรับใช้โหลดม็อดกราฟิก และเนื้อเรื่องเสริม 1.6",
                descriptionEn = "Crucial framework mod required for modern graphic and content mods.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/1915",
                category = "Framework"
            ),
            ModDownloadItem(
                idKey = "automate",
                name = "Automate",
                author = "Pathoschild",
                descriptionTh = "ระบบเชื่อมต่อกล่องกับเตาหลอม/ถังหมัก ให้ทำงานอัตโนมัติ 100%",
                descriptionEn = "Automatically pulls resources from chests into nearby machines.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/1063",
                category = "Automation"
            ),
            ModDownloadItem(
                idKey = "npc_map",
                name = "NPC Map Locations",
                author = "Bouhm",
                descriptionTh = "แสดงตำแหน่งตัวละคร NPC ทุกคนบนแผนที่แบบ Real-time",
                descriptionEn = "Shows real-time positions of all villagers directly on the game map.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/239",
                category = "Quality of Life"
            ),
            ModDownloadItem(
                idKey = "ridgeside",
                name = "Ridgeside Village",
                author = "Rafseazz",
                descriptionTh = "เพิ่มหมู่บ้านขนาดใหญ่บนยอดเขา พร้อมชาวบ้านใหม่กว่า 50 คน",
                descriptionEn = "Adds an entire new mountain village with 50+ voiced characters.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/7286",
                category = "Expansion"
            )
        )
    }

    // Filter out already-installed mods from the download catalog
    val availablePopularMods = remember(installedMods) {
        allPopularMods.filter { popMod ->
            installedMods.none { installed ->
                installed.id == popMod.idKey ||
                installed.name.contains(popMod.name.take(6), ignoreCase = true) ||
                popMod.name.contains(installed.name.take(6), ignoreCase = true)
            }
        }
    }

    fun applyModInstall(result: ModInstallResult, replaceTargetId: String? = null) {
        if (replaceTargetId != null) {
            // Replace existing mod
            installedMods = installedMods.map {
                if (it.id == replaceTargetId) {
                    it.copy(
                        name = result.modName,
                        author = result.author,
                        version = result.version,
                        isEnabled = true
                    )
                } else it
            }
            showInstallDialog = Strings.get(
                "✓ อัปเดตม็อด '${result.modName}' เป็นเวอร์ชัน ${result.version} สำเร็จเรียบร้อยแล้ว! (แทนที่เวอร์ชันเดิมแล้ว)",
                "✓ Successfully updated '${result.modName}' to ${result.version}! (Old version replaced)"
            )
        } else {
            // Add as new mod
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
        }
    }

    fun processZipInstall(uri: Uri) {
        isInstalling = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val result = installer.installModFromUri(uri)
        isInstalling = false

        if (result.isSuccess) {
            // Check for existing mod with same/similar name
            val existing = installedMods.find {
                it.name.equals(result.modName, ignoreCase = true) ||
                (it.name.length >= 5 && result.modName.contains(it.name.take(8), ignoreCase = true)) ||
                (result.modName.length >= 5 && it.name.contains(result.modName.take(8), ignoreCase = true))
            }

            if (existing != null) {
                // Trigger Conflict / Update Confirmation Dialog
                pendingUpdate = PendingUpdateInfo(existingMod = existing, newResult = result)
            } else {
                applyModInstall(result)
            }
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
                text = { Text("${Strings.get("🌐 คลังดาวน์โหลดม็อด", "🌐 Mod Downloads")} (${availablePopularMods.size})") }
            )
        }

        if (selectedTab == 0) {
            Text(
                text = Strings.get(
                    "เปิด/ปิดการทำงานของม็อดได้ทันที หรือกดไอคอนถังขยะเพื่อถอนการติดตั้ง",
                    "Enable/disable installed mods or tap the trash icon to uninstall."
                ),
                fontSize = 13.sp,
                color = Color.Gray
            )

            if (installedMods.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            Strings.get("ยังไม่มีม็อดในเครื่อง สามารถกดแท็บ 'คลังดาวน์โหลดม็อด' เพื่อเลือกม็อดได้เลย", "No mods installed yet. Switch to 'Mod Downloads' tab to get started."),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
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
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            modToDelete = mod
                        }
                    )
                }
            }
        } else {
            Text(
                text = Strings.get(
                    "กดปุ่ม 'ดาวน์โหลดม็อด' เพื่อโหลดไฟล์ เมื่อติดตั้งเข้าเครื่องแล้ว ม็อดจะถูกย้ายไปยังแท็บ 'ม็อดในเครื่อง' อัตโนมัติ",
                    "Tap 'Download' to get the mod. Once installed, it will automatically move to the 'Installed Mods' tab."
                ),
                fontSize = 13.sp,
                color = Color.Gray
            )

            if (availablePopularMods.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎉", fontSize = 28.sp)
                        Text(
                            text = Strings.get("คุณได้ติดตั้งม็อดยอดนิยมทั้งหมดในเครื่องเรียบร้อยแล้ว!", "All popular mods are already installed on your device!"),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                availablePopularMods.forEach { mod ->
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
        }

        // Mod Update / Conflict Confirmation Dialog
        pendingUpdate?.let { updateInfo ->
            AlertDialog(
                onDismissRequest = { pendingUpdate = null },
                title = {
                    Text(
                        Strings.get("🔄 ตรวจพบม็อดซ้ำในระบบ (Mod Update)", "🔄 Mod Update Detected"),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            Strings.get(
                                "พบม็อด '${updateInfo.existingMod.name}' ติดตั้งอยู่ในระบบแล้ว:\n\n• เวอร์ชันปัจจุบัน: ${updateInfo.existingMod.version}\n• เวอร์ชันใหม่ที่กำลังติดตั้ง: ${updateInfo.newResult.version}\n\nคุณต้องการลบเวอร์ชันเดิมและอัปเดตเป็นเวอร์ชันใหม่นี้หรือไม่?",
                                "Mod '${updateInfo.existingMod.name}' is already installed:\n\n• Current Version: ${updateInfo.existingMod.version}\n• New Version: ${updateInfo.newResult.version}\n\nDo you want to replace the old version with this new update?"
                            ),
                            fontSize = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            applyModInstall(updateInfo.newResult, replaceTargetId = updateInfo.existingMod.id)
                            pendingUpdate = null
                        }
                    ) {
                        Text(Strings.get("อัปเดตแทนที่ (Replace)", "Update & Replace"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingUpdate = null }) {
                        Text(Strings.get("ยกเลิก", "Cancel"))
                    }
                }
            )
        }

        // Uninstall / Delete Confirmation Dialog
        modToDelete?.let { mod ->
            AlertDialog(
                onDismissRequest = { modToDelete = null },
                title = { Text(Strings.get("ถอนการติดตั้งม็อด", "Uninstall Mod"), fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        Strings.get("คุณต้องการลบม็อด '${mod.name}' ออกจากเครื่องหรือไม่?", "Are you sure you want to uninstall '${mod.name}'?"),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            installedMods = installedMods.filter { it.id != mod.id }
                            modToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(Strings.get("ลบม็อด", "Delete"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { modToDelete = null }) {
                        Text(Strings.get("ยกเลิก", "Cancel"))
                    }
                }
            )
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
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit = {}
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Mod",
                        tint = Color.Gray
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}
