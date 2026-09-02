package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.tukimtk.farmsync.data.SaveStateRepository
import com.tukimtk.farmsync.i18n.AppLanguage
import com.tukimtk.farmsync.i18n.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabContent(onOpenApiKey: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val repo = remember { SaveStateRepository(context) }

    var selectedStorage by remember { mutableStateOf(repo.getSelectedStorage()) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf<String?>(null) }
    
    // Mock shizuku state for settings
    var shizukuEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(Strings.get("⚙️ ตั้งค่าแอปพลิเคชัน", "⚙️ Application Settings"), fontSize = 24.sp, fontWeight = FontWeight.Bold)

        // Language & Localization Section
        SectionTitle(title = Strings.get("ภาษาและการแปล", "Language & Localization"))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(Strings.get("🌐 ภาษาการแสดงผล (Language)", "🌐 Display Language"), fontWeight = FontWeight.SemiBold)
                
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = Strings.currentLanguage == AppLanguage.TH,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Strings.currentLanguage = AppLanguage.TH
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("🇹🇭 ภาษาไทย")
                    }
                    SegmentedButton(
                        selected = Strings.currentLanguage == AppLanguage.EN,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Strings.currentLanguage = AppLanguage.EN
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("🇬🇧 English")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(Strings.get("🤖 ระบบแปลภาษา AI (BYOK Gemini)", "🤖 AI Translation (BYOK Gemini)"), fontWeight = FontWeight.SemiBold)
                Text(
                    Strings.get("ใส่ Gemini API Key ส่วนตัวเพื่อแปลม็อดเป็นภาษาไทยได้ฟรีไม่จำกัด", "Configure your Gemini API Key for zero-cost Thai mod translation."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenApiKey()
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(Strings.get("🔑 ตั้งค่า API Key", "🔑 Configure API Key"))
                }
            }
        }
        
        // Shizuku Integration Section
        SectionTitle(title = Strings.get("การรวม Shizuku", "Shizuku Integration"))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(Strings.get("การเข้าถึงไฟล์ระบบ", "System File Access"), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = Strings.get("อนุญาตการเขียนเซฟเกมผ่าน Shizuku", "Allow game save writes via Shizuku"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    Switch(
                        checked = shizukuEnabled,
                        onCheckedChange = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            shizukuEnabled = it 
                        }
                    )
                }
            }
        }

        // Storage & Cloud Section
        SectionTitle(title = Strings.get("ที่เก็บข้อมูลและคลาวด์", "Storage & Cloud"))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(Strings.get("📁 ผู้ให้บริการซิงค์ข้อมูล (Storage Provider)", "📁 Storage & Sync Provider"), fontWeight = FontWeight.SemiBold)
                
                AssistChip(
                    onClick = { },
                    label = { Text("${Strings.get("ปัจจุบัน", "Current")}: $selectedStorage") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showStorageDialog = true
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(Strings.get("⚙️ เลือกและตั้งค่า Cloud / SMB", "⚙️ Configure Cloud / Storage"))
                }
            }
        }
        
        // App Info & Diagnostics
        SectionTitle(title = Strings.get("ข้อมูลแอปและการวินิจฉัย", "App Info & Diagnostics"))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(Strings.get("เวอร์ชันแอป", "App Version"), fontWeight = FontWeight.SemiBold)
                Text("1.0.0-beta", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(Strings.get("สถานะระบบ", "System Status"), fontWeight = FontWeight.SemiBold)
                Text(Strings.get("ปกติ", "Normal"), color = Color(0xFF2E7D32), fontSize = 14.sp)
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
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}
