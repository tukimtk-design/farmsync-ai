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
import com.tukimtk.farmsync.data.PersistedMod
import com.tukimtk.farmsync.data.SaveStateRepository
import com.tukimtk.farmsync.i18n.Strings
import com.tukimtk.farmsync.mods.ModInstallResult
import com.tukimtk.farmsync.mods.ModInstaller

data class ModDownloadItem(
    val idKey: String,
    val uniqueId: String,
    val name: String,
    val author: String,
    val descriptionTh: String,
    val descriptionEn: String,
    val nexusUrl: String,
    val category: String
)

data class PendingUpdateInfo(
    val existingMod: PersistedMod,
    val newResult: ModInstallResult
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModManagerScreen(incomingZipUri: Uri? = null, onClearIncomingZip: () -> Unit = {}) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val repo = remember { SaveStateRepository(context) }
    val installer = remember { ModInstaller(context) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var installedMods by remember { mutableStateOf(repo.loadInstalledMods()) }

    var showInstallDialog by remember { mutableStateOf<String?>(null) }
    var pendingUpdate by remember { mutableStateOf<PendingUpdateInfo?>(null) }
    var modToDelete by remember { mutableStateOf<PersistedMod?>(null) }
    var isInstalling by remember { mutableStateOf(false) }

    val allPopularMods = remember {
        listOf(
            ModDownloadItem(
                idKey = "sve",
                uniqueId = "FlashShifter.StardewValleyExpandedCP",
                name = "Stardew Valley Expanded (SVE)",
                author = "FlashShifter",
                descriptionTh = "ม็อดขยายเนื้อเรื่องอันดับ 1 เพิ่มตัวละคร พื้นที่ เควส และอีเวนต์ใหม่มากมาย",
                descriptionEn = "The #1 expansion mod adding dozens of NPCs, maps, quests, and events.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/3753",
                category = "Expansion"
            ),
            ModDownloadItem(
                idKey = "ui_info",
                uniqueId = "Annosz.UIInfoSuite2",
                name = "UI Info Suite 2",
                author = "Annosz",
                descriptionTh = "แสดงไอคอนบอกสิ่งที่ต้องทำ ดูสภาพอากาศ วันเกิดชาวบ้าน และราคาขายผลผลิต",
                descriptionEn = "Displays luck, weather, birthday reminders, and crop harvest timers.",
                nexusUrl = "https://github.com/Annosz/UIInfoSuite2/releases",
                category = "Quality of Life"
            ),
            ModDownloadItem(
                idKey = "cp",
                uniqueId = "Pathoschild.ContentPatcher",
                name = "Content Patcher (CP)",
                author = "Pathoschild",
                descriptionTh = "ม็อดรากฐานที่จำเป็นที่สุดสำหรับใช้โหลดม็อดกราฟิก และเนื้อเรื่องเสริม 1.6",
                descriptionEn = "Crucial framework mod required for modern graphic and content mods.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/1915",
                category = "Framework"
            ),
            ModDownloadItem(
                idKey = "automate",
                uniqueId = "Pathoschild.Automate",
                name = "Automate",
                author = "Pathoschild",
                descriptionTh = "ระบบเชื่อมต่อกล่องกับเตาหลอม/ถังหมัก ให้ทำงานอัตโนมัติ 100%",
                descriptionEn = "Automatically pulls resources from chests into nearby machines.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/1063",
                category = "Automation"
            ),
            ModDownloadItem(
                idKey = "npc_map",
                uniqueId = "Bouhm.NPCMapLocations",
                name = "NPC Map Locations",
                author = "Bouhm",
                descriptionTh = "แสดงตำแหน่งตัวละคร NPC ทุกคนบนแผนที่แบบ Real-time",
                descriptionEn = "Shows real-time positions of all villagers directly on the game map.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/239",
                category = "Quality of Life"
            ),
            ModDownloadItem(
                idKey = "ridgeside",
                uniqueId = "Rafseazz.RidgesideVillage",
                name = "Ridgeside Village",
                author = "Rafseazz",
                descriptionTh = "เพิ่มหมู่บ้านขนาดใหญ่บนยอดเขา พร้อมชาวบ้านใหม่กว่า 50 คน",
                descriptionEn = "Adds an entire new mountain village with 50+ voiced characters.",
                nexusUrl = "https://www.nexusmods.com/stardewvalley/mods/7286",
                category = "Expansion"
            )
        )
    }

    // Filter out already-installed mods from download catalog using exact IDs
    val availablePopularMods = remember(installedMods) {
        allPopularMods.filter { popMod ->
            installedMods.none { installed ->
                installed.id == popMod.idKey ||
                (installed.uniqueId.isNotBlank() && installed.uniqueId.equals(popMod.uniqueId, ignoreCase = true)) ||
                installed.name.trim().equals(popMod.name.trim(), ignoreCase = true)
            }
        }
    }

    fun updateAndPersistMods(newModList: List<PersistedMod>) {
        installedMods = newModList
        repo.saveInstalledMods(newModList)
    }

    fun applyModInstall(result: ModInstallResult, replaceTargetId: String? = null) {
        if (replaceTargetId != null) {
            // Replace existing mod cleanly
            val updated = installedMods.map {
                if (it.id == replaceTargetId) {
                    it.copy(
                        uniqueId = result.uniqueId,
                        name = result.modName,
                        author = result.author,
                        version = result.version,
                        isEnabled = true
                    )
                } else it
            }
            updateAndPersistMods(updated)
            showInstallDialog = Strings.get(
                "✓ อัปเดตม็อด '${result.modName}' เป็นเวอร์ชัน ${result.version} สำเร็จเรียบร้อยแล้ว!",
                "✓ Successfully updated '${result.modName}' to ${result.version}!"
            )
        } else {
            // Append as a brand new distinct mod (unlimited support)
            val newMod = PersistedMod(
                id = "mod_${System.currentTimeMillis()}_${installedMods.size + 1}",
                uniqueId = result.uniqueId,
                name = result.modName,
                author = result.author,
                version = result.version,
                isEnabled = true
            )
            updateAndPersistMods(installedMods + newMod)
            showInstallDialog = Strings.get(
                "✓ ติดตั้งม็อด '${result.modName}' (${result.version}) โดย ${result.author} สำเร็จเรียบร้อยแล้ว! (รวม ${installedMods.size + 1} รายการ)",
                "✓ Successfully installed '${result.modName}' (${result.version}) by ${result.author}! (Total: ${installedMods.size + 1} mods)"
            )
        }
    }

    fun processZipInstall(uri: Uri) {
        isInstalling = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val result = installer.installModFromUri(uri)
        isInstalling = false

        if (result.isSuccess) {
            // Precise duplicate check using UniqueID or Exact Name match only
            val existing = installedMods.find {
                (result.uniqueId.isNotBlank() && it.uniqueId.isNotBlank() && it.uniqueId.equals(result.uniqueId, ignoreCase = true)) ||
                it.name.trim().equals(result.modName.trim(), ignoreCase = true)
            }

            if (existing != null) {
                // Mod is genuinely the same mod -> prompt for update if different version
                val comp = com.tukimtk.farmsync.mods.compareVersions(result.version, existing.version)
                if (comp == 0) {
                    showInstallDialog = Strings.get(
                        "✓ ม็อด '${result.modName}' เป็นเวอร์ชันล่าสุด (${result.version}) อยู่แล้ว!",
                        "✓ Mod '${result.modName}' is already installed at this version (${result.version})!"
                    )
                } else {
                    pendingUpdate = PendingUpdateInfo(existingMod = existing, newResult = result)
                }
            } else {
                // Brand new distinct mod -> append directly!
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
                    "เปิด/ปิดการทำงานของม็อดได้ทันที สามารถติดตั้งม็อดได้ไม่จำกัดจำนวน และข้อมูลจะบันทึกคงอยู่ถาวร",
                    "Enable/disable mods or tap the trash icon to uninstall. Supports unlimited mods with persistent state."
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
                            val updated = installedMods.map {
                                if (it.id == mod.id) it.copy(isEnabled = isChecked) else it
                            }
                            updateAndPersistMods(updated)
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
                            val updated = installedMods.filter { it.id != mod.id }
                            updateAndPersistMods(updated)
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
