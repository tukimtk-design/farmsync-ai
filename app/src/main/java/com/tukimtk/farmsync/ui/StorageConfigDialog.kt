package com.tukimtk.farmsync.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.i18n.Strings

@Composable
fun StorageConfigDialog(
    currentSelection: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedOption by remember { mutableStateOf(currentSelection) }
    var smbServerIp by remember { mutableStateOf("192.168.1.100") }
    var smbShareFolder by remember { mutableStateOf("StardewSaves") }

    val options = listOf(
        "Local Wi-Fi SMB (1 Gbps Direct)" to Strings.get("💻 Wi-Fi วงแลนในบ้าน (SMB 1 Gbps)", "💻 Local Wi-Fi SMB (1 Gbps Direct)"),
        "Microsoft OneDrive" to Strings.get("🟦 Microsoft OneDrive (เซฟ PC Win 11)", "🟦 Microsoft OneDrive (Windows 11)"),
        "Google Drive" to Strings.get("🟩 Google Drive (Google Play Cloud)", "🟩 Google Drive (Google Play Cloud)"),
        "WebDAV / Synology NAS" to Strings.get("☁️ WebDAV (Nextcloud / Synology NAS)", "☁️ WebDAV (Nextcloud / Synology NAS)"),
        "Dropbox" to Strings.get("📦 Dropbox Storage", "📦 Dropbox Storage")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Strings.get("⚙️ เลือกผู้ให้บริการ Cloud / Network", "⚙️ Select Storage Provider"),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = Strings.get("เลือกช่องทางการซิงค์เซฟระหว่าง PC และมือถือ:", "Choose how you want to sync saves between PC & Android:"),
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                options.forEach { (key, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedOption = key
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOption == key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontWeight = if (selectedOption == key) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                            RadioButton(
                                selected = selectedOption == key,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedOption = key
                                }
                            )
                        }
                    }
                }

                if (selectedOption.contains("SMB")) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(Strings.get("ตั้งค่าเครือข่าย SMB:", "SMB Server Settings:"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        OutlinedTextField(
                            value = smbServerIp,
                            onValueChange = { smbServerIp = it },
                            label = { Text(Strings.get("IP คอมพิวเตอร์ (PC IP)", "PC IP Address")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = smbShareFolder,
                            onValueChange = { smbShareFolder = it },
                            label = { Text(Strings.get("ชื่อแชร์โฟลเดอร์", "Shared Folder Name")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSave(selectedOption)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(Strings.get("บันทึกการตั้งค่า", "Save & Apply"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.get("ยกเลิก", "Cancel"))
            }
        }
    )
}
