package com.tukimtk.farmsync.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.data.SaveStateRepository
import com.tukimtk.farmsync.game.stardew.EditableSaveData
import com.tukimtk.farmsync.game.stardew.RealSaveSlot
import com.tukimtk.farmsync.game.stardew.SaveHealthReport
import com.tukimtk.farmsync.game.stardew.SaveRescueManager
import com.tukimtk.farmsync.game.stardew.SaveScanResult
import com.tukimtk.farmsync.game.stardew.SaveWriteResult
import com.tukimtk.farmsync.game.stardew.ScanFailureReason
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge
import com.tukimtk.farmsync.game.stardew.StardewSaveEditor
import com.tukimtk.farmsync.i18n.Strings
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveEditorScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val repo = remember { SaveStateRepository(context) }
    val saveEditor = remember { StardewSaveEditor() }
    val bridge = remember { ShizukuSaveBridge(context) }
    val rescueManager = remember { SaveRescueManager(context) }

    // Detected saves on device
    var scanResult by remember { mutableStateOf<SaveScanResult>(SaveScanResult.Idle) }
    var selectedRealSlot by remember { mutableStateOf<RealSaveSlot?>(null) }
    var selectedTreeUri by remember { mutableStateOf<Uri?>(null) }

    var healthReport by remember { mutableStateOf<SaveHealthReport?>(null) }
    var showRescueDialog by remember { mutableStateOf(false) }

    // Initial data
    val initialData = remember { repo.loadSaveData() }

    var farmerName by remember { mutableStateOf(initialData.characterName) }
    var farmName by remember { mutableStateOf(initialData.farmName) }
    var money by remember { mutableStateOf(initialData.money.toString()) }
    var selectedSeason by remember { mutableStateOf(initialData.season) }
    var dayOfMonth by remember { mutableFloatStateOf(initialData.dayOfMonth.toFloat()) }
    var year by remember { mutableStateOf(initialData.year.toString()) }
    var maxHealth by remember { mutableFloatStateOf(initialData.maxHealth.toFloat()) }
    var maxStamina by remember { mutableFloatStateOf(initialData.maxStamina.toFloat()) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var resultDialogMessage by remember { mutableStateOf("") }
    var isWriting by remember { mutableStateOf(false) }

    fun refreshSaves() {
        if (scanResult is SaveScanResult.Scanning) return
        scanResult = SaveScanResult.Scanning
        val result = bridge.scanRealSaves()
        scanResult = result
        if (result is SaveScanResult.SavesFound) {
            val first = result.saves.first()
            selectedRealSlot = first
            farmerName = first.farmerName
            farmName = first.farmName
            money = first.money.toString()
            selectedSeason = first.season
            dayOfMonth = first.day.toFloat()
            year = first.year.toString()

            // Run Health Inspector
            val activeMods = repo.loadInstalledMods().filter { it.isEnabled }.map { it.name }
            val rawXml = bridge.execCommand("cat \"${first.folderPath}/SaveGameInfo\"")
            healthReport = rescueManager.inspectSaveHealth(rawXml, activeMods)
        }
    }

    LaunchedEffect(Unit) {
        refreshSaves()
    }

    // SAF Document Tree Picker
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            selectedTreeUri = uri
            resultDialogMessage = Strings.get(
                "เชื่อมต่อโฟลเดอร์เซฟเกมสำเร็จแล้ว! ระบบจะเขียนค่าลงไฟล์เซฟในโฟลเดอร์นี้โดยตรง",
                "Connected to save folder! Edits will be applied directly to files in this directory."
            )
            showSuccessDialog = true
        }
    }

    val seasons = listOf("Spring", "Summer", "Fall", "Winter")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Rescue Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Strings.get("✏️ ตัวแก้ไขเซฟเกม (Save Editor)", "✏️ In-Game Save Editor"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showRescueDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🛟 ${Strings.get("กู้คืนเซฟ", "Save Rescue")}")
            }
        }

        // Mod Health & Compatibility Alert Banner
        healthReport?.let { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (report.isBootable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(report.statusTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(report.statusDetail, fontSize = 12.sp)
                }
            }
        }

        // Real Save Slot Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = Strings.get("📂 เลือกเซฟเกมในเครื่องที่จะแก้ไข:", "📂 Select Farm Save to Edit:"),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                if (scanResult is SaveScanResult.Scanning) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                } else if (scanResult is SaveScanResult.SavesFound) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🟢 " + Strings.get(
                                    "Shizuku เชื่อมต่อโฟลเดอร์เซฟให้อัตโนมัติแล้ว (พร้อมแก้ไขและบันทึกได้ทันที ไม่ต้องเลือกโฟลเดอร์ด้วยตนเอง)",
                                    "Shizuku connected directly to save folder! Ready to edit and save without manual picker."
                                ),
                                fontSize = 12.sp,
                                color = Color(0xFF1B5E20),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    (scanResult as SaveScanResult.SavesFound).saves.forEach { slot ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedRealSlot = slot
                                    farmerName = slot.farmerName
                                    farmName = slot.farmName
                                    money = slot.money.toString()
                                    selectedSeason = slot.season
                                    dayOfMonth = slot.day.toFloat()
                                    year = slot.year.toString()
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedRealSlot?.folderPath == slot.folderPath) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "🏡 ${slot.farmName} (${slot.farmerName})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${slot.season} วันที่ ${slot.day} ปี ${slot.year} | 💰 ${slot.money}g",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = selectedRealSlot?.folderPath == slot.folderPath,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedRealSlot = slot
                                        farmerName = slot.farmerName
                                        farmName = slot.farmName
                                        money = slot.money.toString()
                                        selectedSeason = slot.season
                                        dayOfMonth = slot.day.toFloat()
                                        year = slot.year.toString()
                                    }
                                )
                            }
                        }
                    }
                } else if (scanResult is SaveScanResult.NoSavesFound) {
                    Text(
                        text = Strings.get(
                            "ไม่พบเซฟ Stardew Valley ในตำแหน่งที่รองรับ กรุณาเข้าเกมเพื่อสร้างเซฟใหม่และเข้านอน 1 คืนก่อน",
                            "No Stardew Valley saves found in supported locations. Create a character and sleep 1 night in-game first."
                        ),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (scanResult is SaveScanResult.ScanFailed) {
                    val failMsg = when ((scanResult as SaveScanResult.ScanFailed).reason) {
                        ScanFailureReason.ShizukuNotReady -> Strings.get("Shizuku ยังไม่พร้อมทำงาน กรุณาไปที่แท็บ Shizuku", "Shizuku is not ready. Go to Shizuku tab.")
                        ScanFailureReason.AccessDenied -> Strings.get("ถูกปฏิเสธการเข้าถึงโฟลเดอร์เซฟ", "Access to save folder was denied.")
                        ScanFailureReason.CommandFailed -> Strings.get("เกิดข้อผิดพลาดในการดึงข้อมูลโฟลเดอร์", "Failed to retrieve folder info.")
                        else -> Strings.get("เกิดข้อผิดพลาดที่ไม่ทราบสาเหตุระหว่างแสกน", "An unknown error occurred during scan.")
                    }
                    Text(
                        text = "⚠️ $failMsg",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (scanResult is SaveScanResult.Idle) {
                    // Start state
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        refreshSaves()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(Strings.get("🔄 สแกนหาเซฟเกมในเครื่องใหม่", "🔄 Rescan Save Slots"), fontWeight = FontWeight.Bold)
                }

                if (scanResult !is SaveScanResult.SavesFound) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            folderPicker.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Strings.get("📂 เลือกโฟลเดอร์ด้วยตนเอง (กรณีไม่ได้ใช้ Shizuku)", "📂 Manual SAF Folder (Non-Shizuku)"))
                    }
                }
            }
        }

        // Save Backup History & 1-Click Restore
        com.tukimtk.farmsync.ui.components.SaveBackupHistoryCard(
            onRestored = { refreshSaves() }
        )

        // 1. Basic Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(Strings.get("👤 ข้อมูลฟาร์มและตัวละคร", "👤 Farmer & Farm Info"), fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = farmerName,
                    onValueChange = { farmerName = it },
                    label = { Text(Strings.get("ชื่อตัวละคร (Farmer Name)", "Character Name")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = farmName,
                    onValueChange = { farmName = it },
                    label = { Text(Strings.get("ชื่อฟาร์ม (Farm Name)", "Farm Name")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // 2. Economy & Money Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(Strings.get("💰 จำนวนเงินในเกม (Money)", "💰 In-Game Money (g)"), fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = money,
                    onValueChange = { money = it.filter { ch -> ch.isDigit() } },
                    label = { Text(Strings.get("จำนวนเงินปัจจุบัน (g)", "Current Gold (g)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val current = money.toIntOrNull() ?: 0
                            money = (current + 50000).toString()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+50,000g")
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            money = "999999"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Max (999k)")
                    }
                }
            }
        }

        // 3. Timeline Date Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(Strings.get("📅 ฤดูกาลและวันเวลา (In-Game Timeline)", "📅 Season & Timeline"), fontWeight = FontWeight.Bold)

                Text("${Strings.get("ฤดูกาล", "Season")}: $selectedSeason", fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    seasons.forEach { season ->
                        FilterChip(
                            selected = selectedSeason == season,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedSeason = season
                            },
                            label = {
                                Text(
                                    when (season) {
                                        "Spring" -> Strings.get("ฤดูใบไม้ผลิ", "Spring")
                                        "Summer" -> Strings.get("ฤดูร้อน", "Summer")
                                        "Fall" -> Strings.get("ฤดูใบไม้ร่วง", "Fall")
                                        "Winter" -> Strings.get("ฤดูหนาว", "Winter")
                                        else -> season
                                    }
                                )
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${Strings.get("วันที่", "Day")}: ${dayOfMonth.toInt()}", fontWeight = FontWeight.SemiBold)
                    Text("${Strings.get("ปีที่", "Year")}: $year", fontWeight = FontWeight.SemiBold)
                }

                Slider(
                    value = dayOfMonth,
                    onValueChange = { dayOfMonth = it },
                    valueRange = 1f..28f,
                    steps = 26
                )

                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it.filter { ch -> ch.isDigit() } },
                    label = { Text(Strings.get("ปีที่เล่น (Year 1, 2, 3...)", "In-Game Year")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // 4. Character Stats (Stamina & Health)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(Strings.get("⚡ พลังงานและเลือดสูงสุด (Stats)", "⚡ Max Energy & Health"), fontWeight = FontWeight.Bold)

                Text("${Strings.get("พลังงานสูงสุด (Max Stamina)", "Max Stamina")}: ${maxStamina.toInt()}", fontSize = 14.sp)
                Slider(
                    value = maxStamina,
                    onValueChange = { maxStamina = it },
                    valueRange = 270f..508f
                )

                Text("${Strings.get("พลังชีวิตสูงสุด (Max Health)", "Max Health")}: ${maxHealth.toInt()}", fontSize = 14.sp)
                Slider(
                    value = maxHealth,
                    onValueChange = { maxHealth = it },
                    valueRange = 100f..200f
                )
            }
        }

        // Action Button: Save & Apply
        Button(
            onClick = {
                if (isWriting) return@Button
                isWriting = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                val updatedData = EditableSaveData(
                    characterName = farmerName.ifBlank { "Tuki" },
                    farmName = farmName.ifBlank { "Sunrise Peak" },
                    money = money.toIntOrNull() ?: 184500,
                    season = selectedSeason,
                    dayOfMonth = dayOfMonth.toInt(),
                    year = year.toIntOrNull() ?: 2,
                    maxHealth = maxHealth.toInt(),
                    maxStamina = maxStamina.toInt()
                )

                repo.persistSaveData(updatedData)

                var writeResult: SaveWriteResult = SaveWriteResult.UnexpectedFailure
                if (selectedRealSlot != null) {
                    writeResult = bridge.writeSaveWithProtection(selectedRealSlot!!.folderPath, updatedData, saveEditor, rescueManager)
                } else if (selectedTreeUri != null) {
                    val fallbackSuccess = bridge.writeToDocumentTree(selectedTreeUri!!, updatedData, saveEditor)
                    if (fallbackSuccess) writeResult = SaveWriteResult.SuccessVerified
                }

                if (writeResult == SaveWriteResult.SuccessVerified) {
                    resultDialogMessage = Strings.get(
                        "บันทึกและตรวจสอบไฟล์เซฟจริงเรียบร้อยแล้ว!\n(${selectedRealSlot?.farmName ?: "Farm"} - ${selectedRealSlot?.folderName ?: ""})",
                        "Save written and verified successfully!\n(${selectedRealSlot?.farmName ?: "Farm"} - ${selectedRealSlot?.folderName ?: ""})"
                    )
                } else {
                    val errDetail = when (writeResult) {
                        is SaveWriteResult.BackupFailed -> Strings.get("การสำรองเซฟก่อนเขียนล้มเหลว (BackupFailed)", "Backup failed")
                        is SaveWriteResult.ShizukuNotReady -> Strings.get("Shizuku ยังไม่พร้อมทำงาน (ShizukuNotReady)", "Shizuku not ready")
                        is SaveWriteResult.InvalidDestination -> Strings.get("ไม่พบไฟล์เซฟในโฟลเดอร์ หรือโฟลเดอร์ไม่ถูกต้อง (InvalidDestination)", "Invalid destination folder")
                        is SaveWriteResult.MainSaveStageFailed -> Strings.get("คัดลอกไฟล์เซฟหลักชั่วคราวล้มเหลว (MainSaveStageFailed)", "Main save stage copy failed")
                        is SaveWriteResult.SaveGameInfoStageFailed -> Strings.get("คัดลอกไฟล์ SaveGameInfo ชั่วคราวล้มเหลว (SaveGameInfoStageFailed)", "SaveGameInfo stage copy failed")
                        is SaveWriteResult.MainSaveReplaceFailed -> Strings.get("เขียนทับไฟล์เซฟหลักล้มเหลว (MainSaveReplaceFailed)", "Main save live replacement failed")
                        is SaveWriteResult.SaveGameInfoReplaceFailed -> Strings.get("เขียนทับไฟล์ SaveGameInfo ล้มเหลว (SaveGameInfoReplaceFailed)", "SaveGameInfo live replacement failed")
                        is SaveWriteResult.PermissionDenied -> Strings.get("ถูกปฏิเสธสิทธิ์การเขียนไฟล์ (PermissionDenied)", "Permission denied")
                        is SaveWriteResult.VerificationFailed -> Strings.get("ตรวจสอบความถูกต้องของไฟล์เซฟไม่ผ่าน (VerificationFailed)", "Checksum verification failed")
                        is SaveWriteResult.ReloadFailed -> Strings.get("โหลดไฟล์เซฟกลับมาตรวจสอบไม่สำเร็จ (ReloadFailed)", "Reloading save failed")
                        is SaveWriteResult.RollbackFailed -> Strings.get("การย้อนคืนค่าเดิมล้มเหลว (RollbackFailed)", "Rollback failed")
                        else -> Strings.get("เกิดข้อผิดพลาดที่ไม่คาดคิด กรุณาเลือกเซฟเกมก่อนบันทึก (UnexpectedFailure)", "Unexpected failure")
                    }
                    resultDialogMessage = "Error: $errDetail"
                }

                showSuccessDialog = true
                isWriting = false
            },
            enabled = !isWriting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = Strings.get("💾 บันทึกการแก้ไขลงเซฟเกมจริง", "💾 Save & Apply to Game Files"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Rescue Dialog
        if (showRescueDialog) {
            SaveRescueDialog(
                onDismiss = { showRescueDialog = false },
                onRestored = { refreshSaves() }
            )
        }

        // Success Confirmation Dialog
        if (showSuccessDialog) {
            SuccessFeedbackDialog(
                title = Strings.get("ผลการบันทึกเซฟเกม", "Save Operation Result"),
                message = resultDialogMessage,
                onDismiss = { showSuccessDialog = false }
            )
        }
    }
}
