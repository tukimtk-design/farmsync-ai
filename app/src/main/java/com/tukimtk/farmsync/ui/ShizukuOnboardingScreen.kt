package com.tukimtk.farmsync.ui

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge
import com.tukimtk.farmsync.i18n.Strings
import com.tukimtk.farmsync.shizuku.ShizukuState
import com.tukimtk.farmsync.shizuku.ShizukuStateManager
import com.tukimtk.farmsync.shizuku.OemHelper

@Composable
fun ShizukuOnboardingScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val bridge = remember { ShizukuSaveBridge(context) }

    val shizukuState by ShizukuStateManager.state.collectAsState()

    var showDialogMessage by remember { mutableStateOf<String?>(null) }
    var diagnosticOutput by remember { mutableStateOf<String?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Strings.get("⚡ ศูนย์ควบคุมสิทธิ์ Shizuku (Scoped Storage Access)", "⚡ Shizuku Control Center"),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (OemHelper.isXiaomiDevice()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        Strings.get("⚠️ คำแนะนำสำหรับผู้ใช้ Xiaomi/POCO/Redmi", "⚠️ Advisory for Xiaomi/POCO/Redmi users"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        Strings.get("หากพบปัญหา ให้ปิดตัวเลือก MIUI Optimization ใน Developer Options หรืออนุญาตสิทธิ์เพิ่มเติม", "If you encounter issues, consider disabling MIUI Optimization in Developer Options or granting additional permissions via app settings."),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Live Status Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (shizukuState is ShizukuState.Ready) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val iconAndTitle = when (shizukuState) {
                    is ShizukuState.NotInstalled -> "🔴" to Strings.get("ไม่พบ Shizuku (Not Installed)", "Shizuku Not Installed")
                    is ShizukuState.NotRunning -> "🔴" to Strings.get("Shizuku ยังไม่ทำงาน (Not Running)", "Shizuku Not Running")
                    is ShizukuState.VersionTooOld -> "🔴" to Strings.get("เวอร์ชัน Shizuku เก่าเกินไป (Update Required)", "Shizuku Update Required")
                    is ShizukuState.PermissionRequired -> "🟡" to Strings.get("พบ Shizuku แต่ยังไม่ได้รับอนุญาต (Permission Required)", "Shizuku Running (Permission Required)")
                    is ShizukuState.RequiresManualAuthorization -> "🟡" to Strings.get("ต้องอนุญาตสิทธิ์ด้วยตนเอง (Manual Auth Required)", "Manual Authorization Required")
                    is ShizukuState.Ready -> "🟢" to Strings.get("Shizuku พร้อมใช้งาน (Ready)", "Shizuku Ready")
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = iconAndTitle.first, fontSize = 18.sp)
                    Text(text = iconAndTitle.second, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                val desc = when (shizukuState) {
                    is ShizukuState.NotInstalled -> Strings.get("ไม่พบแอป Shizuku กรุณาติดตั้งจาก Google Play Store", "Shizuku is not installed. Please install it from the Play Store.")
                    is ShizukuState.NotRunning -> Strings.get("กรุณาเปิดแอป Shizuku และกด Start", "Please launch Shizuku and start it.")
                    is ShizukuState.VersionTooOld -> Strings.get("กรุณาอัปเดต Shizuku ให้เป็นเวอร์ชันล่าสุด", "Please update Shizuku to the latest version.")
                    is ShizukuState.PermissionRequired -> Strings.get("กดปุ่ม 'ขอยืนยันสิทธิ์' เพื่อดำเนินการ", "Tap 'Request Permission' to proceed.")
                    is ShizukuState.RequiresManualAuthorization -> Strings.get("กรุณาอนุญาตสิทธิ์ในแอป Shizuku ด้วยตนเอง", "Please authorize in the Shizuku app manually.")
                    is ShizukuState.Ready -> Strings.get("ได้รับสิทธิ์แล้ว แต่ยังไม่ได้ตรวจสอบการเข้าถึงโฟลเดอร์เซฟ จำเป็นต้องมีการตรวจสอบสแกนก่อนแก้ไขไฟล์", "Shizuku permission granted. Save-folder access has not yet been verified. Storage diagnostics are required before file operations.")
                }
                Text(text = desc, fontSize = 13.sp, color = Color.DarkGray)
            }
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (shizukuState) {
                is ShizukuState.NotInstalled -> {
                    Button(
                        onClick = {
                            showDialogMessage = Strings.get("กรุณาติดตั้ง Shizuku จาก Play Store", "Please install Shizuku from Play Store.")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🚀 ${Strings.get("ติดตั้ง Shizuku", "Install Shizuku")}")
                    }
                }
                is ShizukuState.NotRunning, is ShizukuState.RequiresManualAuthorization -> {
                    Button(
                        onClick = {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                            if (launchIntent != null) context.startActivity(launchIntent)
                            else showDialogMessage = Strings.get("ไม่สามารถเปิดแอป Shizuku ได้", "Could not launch Shizuku.")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🚀 ${Strings.get("เปิดแอป Shizuku", "Launch Shizuku")}")
                    }
                }
                is ShizukuState.PermissionRequired -> {
                    Button(
                        onClick = { ShizukuStateManager.requestPermission(1001) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🔑 ${Strings.get("ขอยืนยันสิทธิ์ Shizuku", "Request Permission")}")
                    }
                }
                is ShizukuState.Ready -> {
                    Button(
                        onClick = {
                            // Scan all candidate paths - official pkg + aliases used on Xiaomi/HyperOS
                            val paths = listOf(
                                "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves",
                                "/sdcard/Android/data/com.chucklefish.stardewvalley/files/Saves",
                                "/storage/sdcard0/Android/data/com.chucklefish.stardewvalley/files/Saves",
                                "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves"
                            )
                            val results = paths.joinToString("\n\n") { p ->
                                val out = bridge.execCommand("ls -la \"$p\" 2>&1")
                                "=== $p ===\n${out.ifBlank { "(Not found or empty)" }}"
                            }
                            diagnosticOutput = results
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔍 ${Strings.get("ทดสอบสแกนไฟล์เซฟและม็อดในเครื่อง", "Diagnostic Scan")}")
                    }
                }
                is ShizukuState.VersionTooOld -> {
                   Button(onClick = { showDialogMessage = "Update Shizuku" }, modifier = Modifier.weight(1f)) { Text("Update Shizuku") }
                }
            }
        }

        // Diagnostic Console Output
        diagnosticOutput?.let { output ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("💻 Diagnostic Console Output:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(output.ifBlank { "(Directory exists but currently empty)" }, color = Color(0xFF4CAF50), fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }
        }

        Text(
            text = Strings.get("📖 ขั้นตอนการเปิดใช้งาน Shizuku 3 ขั้นตอนง่ายๆ:", "📖 3-Step Shizuku Setup Guide:"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        StepCard(
            stepNumber = "1",
            title = Strings.get("ติดตั้งแอป Shizuku", "Install Shizuku App"),
            desc = Strings.get("ดาวน์โหลดและติดตั้งแอป Shizuku จาก Google Play Store", "Download and install 'Shizuku' from Google Play Store.")
        )

        StepCard(
            stepNumber = "2",
            title = Strings.get("เริ่มระบบผ่าน Wireless Debugging", "Start via Wireless Debugging"),
            desc = Strings.get("เปิด ตัวเลือกสำหรับนักพัฒนา > เปิด การแก้จุดบกพร่องไร้สาย > จับคู่อุปกรณ์ใน Shizuku", "Enable Developer Options > Wireless Debugging > Pair in Shizuku.")
        )

        StepCard(
            stepNumber = "3",
            title = Strings.get("อนุญาตสิทธิ์ให้ FarmSync AI", "Grant Permission to FarmSync AI"),
            desc = Strings.get("เปิดแอป FarmSync AI แล้วกดยืนยันสิทธิ์เพื่อดำเนินการต่อ", "Open FarmSync AI and grant Shizuku permission.")
        )

        showDialogMessage?.let { msg ->
            SuccessFeedbackDialog(
                title = Strings.get("แจ้งเตือน", "Notification"),
                message = msg,
                onDismiss = { showDialogMessage = null }
            )
        }
    }
}

@Composable
fun StepCard(stepNumber: String, title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(stepNumber, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(desc, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}
