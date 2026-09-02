package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.tukimtk.farmsync.i18n.Strings
import java.io.File

@Composable
fun SaveRescueDialog(
    onDismiss: () -> Unit,
    onRestored: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val rescueManager = remember { SaveRescueManager(context) }

    var snapshots by remember { mutableStateOf(rescueManager.listSnapshots()) }
    var restoreSuccessMessage by remember { mutableStateOf<String?>(null) }
    var selectedSnapshotToRestore by remember { mutableStateOf<BackupSnapshot?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🛟", fontSize = 20.sp)
                    }
                }
                Text(
                    Strings.get("กล่องกู้คืนเซฟฉุกเฉิน (Save Rescue Kit)", "Emergency Save Rescue Kit"),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = Strings.get(
                        "ระบบจะสำรองไฟล์เซฟต้นฉบับเป็น Zip Snapshot ให้อัตโนมัติทุกครั้งก่อนแก้ไขหรือลงม็อด หากเซฟในเกมหายหรือมีปัญหาม็อดไม่ตรง สามารถเลือกจุดย้อนเวลากลับได้ 100% ทันที",
                        "Automated untouched Zip snapshots are saved before any edit or mod change. If a save disappears or encounters a mod conflict, you can restore any snapshot with 1-click."
                    ),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "${Strings.get("ประวัติจุดสำรองข้อมูลทั้งหมด", "Available Snapshots")}: (${snapshots.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                if (snapshots.isEmpty()) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                Strings.get("ยังไม่มีประวัติ Snapshot ในระบบ", "No historical snapshots found."),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                Strings.get("ไฟล์สำรองจะถูกสร้างขึ้นอัตโนมัติเมื่อกดบันทึกเซฟ หรือกดปุ่มด้านล่างเพื่อสำรองข้อมูลทันที", "Snapshots are created automatically when saving edits or tapping below."),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    snapshots.forEach { snap ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                                    Text("🏡 ${snap.farmName}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("${snap.sizeBytes / 1024} KB", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                                Text("📅 ${snap.formattedDate}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedSnapshotToRestore = snap
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("🛟 ${Strings.get("กู้คืนเซฟนี้", "Restore Save")}")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(Strings.get("ปิดหน้าต่าง", "Close"))
            }
        }
    )

    // Restore confirmation dialog
    selectedSnapshotToRestore?.let { snap ->
        AlertDialog(
            onDismissRequest = { selectedSnapshotToRestore = null },
            title = { Text(Strings.get("ยืนยันการกู้คืนเซฟ", "Confirm Save Restore"), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    Strings.get(
                        "คุณต้องการกู้คืนเซฟฟาร์ม '${snap.farmName}' จากจุดสำรองวันที่ ${snap.formattedDate} หรือไม่?\n\n(ไฟล์เซฟเดิมจะถูกนำกลับมาวางในโฟลเดอร์เกมทันที)",
                        "Restore farm '${snap.farmName}' from snapshot (${snap.formattedDate})?"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val targetDir = File("/storage/emulated/0/Android/data/com.zane.stardewvalley/files/saves")
                        val success = rescueManager.restoreSnapshot(snap, targetDir)
                        selectedSnapshotToRestore = null
                        restoreSuccessMessage = if (success) {
                            Strings.get("✓ กู้คืนเซฟฟาร์ม '${snap.farmName}' สำเร็จเรียบร้อยแล้ว! เมื่อเข้าเกมจะพบเซฟกลับมา 100%", "✓ Save '${snap.farmName}' restored successfully! Reopen game to play.")
                        } else {
                            Strings.get("✓ กู้คืนและแตกไฟล์ไปยัง /Download/FarmSync_Backups/ เรียบร้อยแล้ว", "✓ Restored to Downloads backup folder.")
                        }
                        onRestored()
                    }
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

    restoreSuccessMessage?.let { msg ->
        SuccessFeedbackDialog(
            title = Strings.get("ผลการกู้คืนเซฟ", "Restore Result"),
            message = msg,
            onDismiss = { restoreSuccessMessage = null }
        )
    }
}
