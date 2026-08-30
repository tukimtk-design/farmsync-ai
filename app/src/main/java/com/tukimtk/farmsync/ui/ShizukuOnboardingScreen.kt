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

@Composable
fun ShizukuOnboardingScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val bridge = remember { ShizukuSaveBridge(context) }

    var isBinderAlive by remember { mutableStateOf(bridge.isBinderAlive()) }
    var isPermissionGranted by remember { mutableStateOf(bridge.isPermissionGranted()) }
    var diagnosticOutput by remember { mutableStateOf<String?>(null) }
    var showDialogMessage by remember { mutableStateOf<String?>(null) }

    fun refreshStatus() {
        isBinderAlive = bridge.isBinderAlive()
        isPermissionGranted = bridge.isPermissionGranted()
    }

    LaunchedEffect(Unit) {
        refreshStatus()
    }

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

        // Live Status Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPermissionGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isPermissionGranted) "🟢" else if (isBinderAlive) "🟡" else "🔴",
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isPermissionGranted) {
                            Strings.get("สิทธิ์เชื่อมต่อสมบูรณ์ (Authorized 100%)", "Shizuku Authorized 100%")
                        } else if (isBinderAlive) {
                            Strings.get("พบ Shizuku แต่ยังไม่ได้รับอนุญาตสิทธิ์", "Shizuku Running (Needs Permission)")
                        } else {
                            Strings.get("Shizuku Service ยังไม่ได้เริ่มทำงาน", "Shizuku Service Not Running")
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = if (isPermissionGranted) {
                        Strings.get("FarmSync AI สามารถอ่านและเขียนโฟลเดอร์เซฟและม็อด /Android/data/ ได้อย่างอิสระโดยไม่ต้องรูท", "Full read/write access to /Android/data/ is active.")
                    } else if (isBinderAlive) {
                        Strings.get("กรุณากดปุ่ม 'ขอยืนยันสิทธิ์' ด้านล่างเพื่ออนุญาตให้เข้าถึงโฟลเดอร์เซฟเกม", "Tap 'Request Permission' below to authorize access.")
                    } else {
                        Strings.get("กรุณาเปิดแอป Shizuku แล้วกด Start via Wireless Debugging ก่อนใช้งาน", "Please launch Shizuku and start it via Wireless Debugging first.")
                    },
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!isBinderAlive) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val launchIntent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                        if (launchIntent != null) {
                            context.startActivity(launchIntent)
                        } else {
                            showDialogMessage = Strings.get("ไม่พบแอป Shizuku ในเครื่อง กรุณาติดตั้งจาก Play Store", "Shizuku app not found. Please install from Play Store.")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🚀 ${Strings.get("เปิดแอป Shizuku", "Launch Shizuku")}")
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("⚙️ ${Strings.get("ตัวเลือกนักพัฒนา", "Dev Options")}")
                }
            } else if (!isPermissionGranted) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        bridge.requestShizukuPermission(1001)
                        refreshStatus()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🔑 ${Strings.get("ขอยืนยันสิทธิ์ Shizuku", "Request Permission")}")
                }
            } else {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // Run diagnostic scan
                        val filesOutput = bridge.execCommand("ls -la /storage/emulated/0/Android/data/com.zane.stardewvalley/files/saves/")
                        val modsOutput = bridge.execCommand("ls -la /storage/emulated/0/Android/data/com.zane.stardewvalley/files/Mods/")
                        diagnosticOutput = "=== Stardew Valley Saves Directory ===\n$filesOutput\n\n=== Stardew Valley Mods Directory ===\n$modsOutput"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🔍 ${Strings.get("ทดสอบสแกนไฟล์เซฟและม็อดในเครื่อง", "Diagnostic Scan")}")
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

        // 3-Step Setup Guide
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
            desc = Strings.get("เปิดแอป FarmSync AI แล้วกดยืนยันสิทธิ์เพื่อเข้าถึงโฟลเดอร์ /Android/data/ ได้ 100%", "Open FarmSync AI and grant Shizuku permission.")
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
