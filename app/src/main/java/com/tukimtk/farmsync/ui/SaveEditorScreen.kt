package com.tukimtk.farmsync.ui

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
import com.tukimtk.farmsync.i18n.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveEditorScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val repo = remember { SaveStateRepository(context) }

    // Load initial persistent values
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
                label = { Text(Strings.get("🛡️ สำรองข้อมูลอัตโนมัติ", "🛡️ Auto-Backup")) }
            )
        }

        Text(
            text = Strings.get(
                "ปรับแต่งค่าตัวละคร ฟาร์ม วันเวลา และเงินในเกมได้อย่างปลอดภัย โดยระบบจะบันทึกค่าและสำรองไฟล์เดิมให้อัตโนมัติ",
                "Safely customize player, farm, timeline, and money values. The system persists edits and creates an automated backup."
            ),
            fontSize = 13.sp,
            color = Color.Gray
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
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                // 1. Persist data into storage
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

                // 2. Show success dialog
                showSuccessDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = Strings.get("💾 บันทึกการแก้ไขลงเซฟเกม", "💾 Save & Apply Edits to Game"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Success Confirmation Dialog
        if (showSuccessDialog) {
            SuccessFeedbackDialog(
                title = Strings.get("บันทึกการแก้ไขสำเร็จ!", "Save Edits Applied!"),
                message = Strings.get(
                    "ค่าเซฟเกมใหม่ถูกบันทึกเรียบร้อยแล้ว (ชื่อฟาร์ม: $farmName | เงิน: ${money}g | วันที่: $selectedSeason วันที่ ${dayOfMonth.toInt()} ปี $year) ข้อมูลจะยังคงอยู่แม้ปิดแอปแล้วเปิดใหม่",
                    "Your farm save has been successfully updated and persisted (Farm: $farmName | Gold: ${money}g | Date: $selectedSeason Day ${dayOfMonth.toInt()} Year $year). Data will remain saved across app restarts."
                ),
                onDismiss = { showSuccessDialog = false }
            )
        }
    }
}
