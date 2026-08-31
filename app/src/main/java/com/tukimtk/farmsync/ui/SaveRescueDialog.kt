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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🛟", fontSize = 24.sp)
                Text(
                    Strings.get("กล่องกู้คืนเซฟฉุกเฉิน (Save Rescue Kit)", "Emergency Save Rescue Kit"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Strings.get(
                        "ระบบจะสำรองไฟล์เซฟต้นฉบับเป็น Zip Snapshot ให้อัตโนมัติทุกครั้งก่อนแก้ไขหรือลงม็อด หากเซฟในเกมหายหรือมีปัญหาม็อดไม่ตรง สามารถเลือกจุดย้อนเวลากลับได้ 100% ทันที",
                        "Automated untouched Zip snapshots are saved before any edit or mod change. If a save disappears or encounters a mod conflict, you can restore any snapshot with 1-click."
                    ),
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                HorizontalDivider()

                Text(
                    text = "${Strings.get("ประวัติจุดสำรองข้อมูลทั้งหมด", "Available Snapshots")}: (${snapshots.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                if (snapshots.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                Strings.get("ยังไม่มีประวัติ Snapshot ในระบบ", "No historical snapshots found."),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                Strings.get("ไฟล์สำรองจะถูกสร้างขึ้นอัตโนมัติเมื่อกดบันทึกเซฟ หรือกดปุ่มด้านล่างเพื่อสำรองข้อมูลทันที", "Snapshots are created automatically when saving edits or tapping below."),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    snapshots.forEach { snap ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏡 ฟาร์ม ${snap.farmName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("${snap.sizeBytes / 1024} KB", fontSize = 10.sp) }
                                    )
                                }
                                Text("📅 ${snap.formattedDate}", fontSize = 12.sp, color = Color.DarkGray)
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedSnapshotToRestore = snap
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("🛟 ${Strings.get("กู้คืนเซฟนี้กลับเข้าเกม", "Restore this Save")}")
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
