package com.tukimtk.farmsync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import com.tukimtk.farmsync.game.stardew.BackupSnapshot
import com.tukimtk.farmsync.game.stardew.SaveRescueManager
import com.tukimtk.farmsync.game.stardew.SaveScanResult
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge
import com.tukimtk.farmsync.i18n.Strings
import com.tukimtk.farmsync.ui.SuccessFeedbackDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveBackupHistoryCard(
    modifier: Modifier = Modifier,
    onRestored: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val rescueManager = remember { SaveRescueManager(context) }
    val bridge = remember { ShizukuSaveBridge(context) }

    var snapshots by remember { mutableStateOf(rescueManager.listSnapshots()) }
    var selectedSnapshotToRestore by remember { mutableStateOf<BackupSnapshot?>(null) }
    var resultDialogMessage by remember { mutableStateOf<String?>(null) }
    var isBackingUp by remember { mutableStateOf(false) }

    fun refreshSnapshots() {
        snapshots = rescueManager.listSnapshots()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🛟 " + Strings.get("ประวัติสำรองและกู้คืนเซฟ", "Save Backup History"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text("${snapshots.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = Strings.get("เรียงตามเวลาล่าสุด กู้คืนได้ทันทีใน 1 คลิก", "Sorted by time, 1-click restore"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            refreshSnapshots()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Snapshots", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Farm Migration Guide Alert Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Migration Hint",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = Strings.get(
                                "💡 คำแนะนำเมื่อเกมขึ้น 'Farm Migration (v1.5)':",
                                "💡 Stardew 'Farm Migration' Notice:"
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = Strings.get(
                                "เมื่อเปิดเกมแล้วเจอกล่อง 'Farm Migration' ให้กดปุ่ม [ SKIP ] (ขวาล่าง) ทันที ตัวเกมจะโหลดเซฟจากเครื่องโดยตรง ไม่ต้องเลือกโฟลเดอร์ผ่านระบบ Android ที่ติด Scoped Storage",
                                "When Stardew Valley prompts 'Farm Migration', tap [ SKIP ] at bottom-right. The game will load saves directly without needing Android SAF folder picker."
                            ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Action: Manual Backup Button
            OutlinedButton(
                onClick = {
                    if (isBackingUp) return@OutlinedButton
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isBackingUp = true

                    val scan = bridge.scanRealSaves()
                    if (scan is SaveScanResult.SavesFound && scan.saves.isNotEmpty()) {
                        val slot = scan.saves.first()
                        val snapshot = rescueManager.backupCurrentSave(slot.folderPath, bridge, "Manual")
                        if (snapshot != null) {
                            refreshSnapshots()
                            resultDialogMessage = Strings.get(
                                "✓ สำรองเซฟฟาร์ม '${snapshot.farmName}' เรียบร้อยแล้ว!\n(บันทึกเวลา ${snapshot.formattedDate})",
                                "✓ Farm '${snapshot.farmName}' backed up successfully!\n(${snapshot.formattedDate})"
                            )
                        } else {
                            resultDialogMessage = Strings.get("สำรองข้อมูลไม่สำเร็จ กรุณาตรวจสอบสิทธิ์ Shizuku", "Backup failed. Check Shizuku status.")
                        }
                    } else {
                        resultDialogMessage = Strings.get("ไม่พบไฟล์เซฟในเครื่องที่จะสำรอง กรุณาตรวจสอบว่ามีเซฟเกมอยู่ใน Stardew Valley", "No active saves found on device.")
                    }
                    isBackingUp = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Manual Backup", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBackingUp) Strings.get("กำลังสำรองข้อมูล...", "Backing up...") else Strings.get("➕ สำรองเซฟปัจจุบันทันที", "➕ Backup Active Save Now"),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Snapshot List
            if (snapshots.isEmpty()) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = Strings.get("ยังไม่มีประวัติ Snapshot ในระบบ", "No historical snapshots yet."),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = Strings.get(
                                "ระบบจะสำรองให้อัตโนมัติทุกครั้งก่อนแก้ไขเซฟ หรือแตะปุ่ม 'สำรองเซฟปัจจุบันทันที' ด้านบน",
                                "Snapshots are created automatically before edits or tap 'Backup Active Save Now' above."
                            ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    snapshots.forEach { snap ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🏡 ${snap.farmName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "📅 ${snap.formattedDate}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Reason & Size Chips
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val reasonLabel = when (snap.reason) {
                                            "PreEdit" -> Strings.get("ก่อนแก้ไข", "Pre-Edit")
                                            "Manual" -> Strings.get("สำรองเอง", "Manual")
                                            "Auto" -> Strings.get("อัตโนมัติ", "Auto")
                                            else -> snap.reason
                                        }
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(reasonLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (snap.reason == "PreEdit") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("${snap.sizeBytes / 1024} KB", fontSize = 11.sp) },
                                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedSnapshotToRestore = snap
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(
                                        text = "🛟 " + Strings.get("กู้คืนเซฟนี้ (1-Click Restore)", "Restore This Save"),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    selectedSnapshotToRestore?.let { snap ->
        AlertDialog(
            onDismissRequest = { selectedSnapshotToRestore = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🛟", fontSize = 20.sp)
                    Text(
                        text = Strings.get("ยืนยันการกู้คืนเซฟเกม", "Confirm Save Restore"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = Strings.get(
                            "คุณต้องการกู้คืนเซฟฟาร์ม '${snap.farmName}' จากจุดสำรองวันที่ ${snap.formattedDate} หรือไม่?",
                            "Do you want to restore farm '${snap.farmName}' from snapshot (${snap.formattedDate})?"
                        ),
                        fontSize = 14.sp
                    )
                    Text(
                        text = Strings.get(
                            "• ระบบจะนำไฟล์เซฟต้นฉบับกลับมาวางในโฟลเดอร์เกมของ Stardew Valley ทันทีผ่าน Shizuku\n• ปรับสิทธิ์การเข้าถึง (Permissions 666/777) ให้อัตโนมัติ ป้องกันปัญหาเกมโหลดเซฟไม่ขึ้น",
                            "• Save files will be restored directly to Stardew Valley folder via Shizuku.\n• Full permissions (666/777) will be configured automatically."
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val success = rescueManager.restoreSnapshotViaShizuku(snap, bridge)
                        selectedSnapshotToRestore = null
                        if (success) {
                            resultDialogMessage = Strings.get(
                                "✓ กู้คืนเซฟฟาร์ม '${snap.farmName}' สำเร็จเรียบร้อยแล้ว!\n\n💡 คำแนะนำเมื่อเปิดเกม:\nหากตัวเกมขึ้นกล่อง 'Farm Migration (v1.5)' ให้กดปุ่ม [ SKIP ] (ขวาล่าง) ทันที ตัวเกมจะโหลดเซฟกลับมาเล่นได้ 100%",
                                "✓ Farm '${snap.farmName}' restored successfully!\n\n💡 In-game notice:\nIf Stardew Valley prompts 'Farm Migration', tap [ SKIP ] at bottom-right to start playing immediately."
                            )
                        } else {
                            resultDialogMessage = Strings.get(
                                "กู้คืนลงโฟลเดอร์เกมไม่สำเร็จ (อาจยังไม่ได้อนุญาต Shizuku) แต่ระบบได้แตกไฟล์สำรองไปยังโฟลเดอร์ Downloads เรียบร้อยแล้ว",
                                "Direct restore failed. Check Shizuku permission. Files extracted to Downloads backup folder."
                            )
                        }
                        refreshSnapshots()
                        onRestored()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(Strings.get("ยืนยันกู้คืน", "Confirm Restore"))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSnapshotToRestore = null }) {
                    Text(Strings.get("ยกเลิก", "Cancel"))
                }
            }
        )
    }

    // Result Dialog
    resultDialogMessage?.let { msg ->
        SuccessFeedbackDialog(
            title = Strings.get("ผลการดำเนินการ", "Operation Result"),
            message = msg,
            onDismiss = { resultDialogMessage = null }
        )
    }
}
