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
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge
import com.tukimtk.farmsync.game.stardew.StardewSaveEditor
import com.tukimtk.farmsync.i18n.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveEditorScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val repo = remember { SaveStateRepository(context) }
    val saveEditor = remember { StardewSaveEditor() }
    val bridge = remember { ShizukuSaveBridge(context) }

    // Detected saves on device
    var realSaves by remember { mutableStateOf<List<RealSaveSlot>>(emptyList()) }
    var selectedRealSlot by remember { mutableStateOf<RealSaveSlot?>(null) }
    var selectedTreeUri by remember { mutableStateOf<Uri?>(null) }

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

    // Auto-scan on load
    LaunchedEffect(Unit) {
        val detected = bridge.scanRealSaves()
        realSaves = detected
        if (detected.isNotEmpty()) {
            val first = detected.first()
            selectedRealSlot = first
            farmerName = first.farmerName
            farmName = first.farmName
            money = first.money.toString()
            selectedSeason = first.season
            dayOfMonth = first.day.toFloat()
            year = first.year.toString()
        }
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
            AssistChip(
                onClick = {},
                label = { Text(if (bridge.isShizukuAvailable()) "⚡ Shizuku Direct Sync" else "🛡️ Safe XML Engine") }
            )
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

                if (realSaves.isNotEmpty()) {
                    realSaves.forEach { slot ->
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
                                        color = Color.Gray
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
                } else {
                    Text(
                        text = Strings.get(
                            "ยังไม่พบเซฟอัตโนมัติ (Android 14 Scoped Storage) คุณสามารถกดปุ่มด้านล่างเพื่อเลือกโฟลเดอร์เซฟ หรือเปิดใช้งาน Shizuku เพื่อตรวจจับอัตโนมัติได้ทันที",
                            "No saves auto-detected due to Scoped Storage. Tap below to pick your save folder or enable Shizuku for 1-click access."
                        ),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            folderPicker.launch(null)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(Strings.get("📂 เลือกโฟลเดอร์เซฟ", "📂 Pick Save Folder"))
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val detected = bridge.scanRealSaves()
                            realSaves = detected
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(Strings.get("🔄 สแกนใหม่", "🔄 Rescan"))
                    }
                }
            }
        }

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

                // 1. Persist in SharedPreferences
                repo.persistSaveData(updatedData)

                // 2. Write to real game save slot
                var writeSuccess = false
                if (selectedRealSlot != null) {
                    writeSuccess = bridge.writeSaveWithProtection(selectedRealSlot!!.folderPath, updatedData, saveEditor)
                }

                if (!writeSuccess && selectedTreeUri != null) {
                    writeSuccess = bridge.writeToDocumentTree(selectedTreeUri!!, updatedData, saveEditor)
                }

                resultDialogMessage = if (writeSuccess) {
                    Strings.get(
                        "✓ บันทึกค่าลงไฟล์เซฟจริงและไฟล์ SaveGameInfo สำเร็จเรียบร้อยแล้ว!\n(ชื่อฟาร์ม: $farmName | เงิน: ${money}g | วันที่: $selectedSeason วันที่ ${dayOfMonth.toInt()} ปี $year)\n\nเมื่อเปิดเข้าเกม Stardew Valley จะพบเซฟพร้อมเล่นได้ทันที!",
                        "✓ Successfully written to the real save file and SaveGameInfo!\n(Farm: $farmName | Gold: ${money}g | Date: $selectedSeason Day ${dayOfMonth.toInt()} Year $year)\n\nOpen Stardew Valley to see your updated farm in the Load menu!"
                    )
                } else {
                    Strings.get(
                        "✓ บันทึกการตั้งค่าในระบบเรียบร้อยแล้ว! (หากต้องการเขียนลงโฟลเดอร์เซฟของตัวเกมโดยตรง กรุณากดปุ่ม '📂 เลือกโฟลเดอร์เซฟ' ด้านบน หรือเปิดสิทธิ์ Shizuku)",
                        "✓ Configuration saved in app! (To write directly into the game directory, please tap '📂 Pick Save Folder' above or enable Shizuku)"
                    )
                }

                showSuccessDialog = true
            },
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
