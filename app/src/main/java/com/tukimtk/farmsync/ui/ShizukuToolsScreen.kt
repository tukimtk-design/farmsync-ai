package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge
import com.tukimtk.farmsync.i18n.Strings
import com.tukimtk.farmsync.shizuku.ShizukuStateManager

@Composable
fun ShizukuToolsScreen() {
    val context = LocalContext.current
    val isShizukuReady by ShizukuStateManager.isAvailable
    val isBinderAlive by ShizukuStateManager.isBinderAlive
    val bridge = remember { ShizukuSaveBridge(context) }
    var scanResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Strings.get("⚡ เครื่องมือระดับสูง (Shizuku Tools)", "⚡ Advanced Shizuku Tools"),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isShizukuReady) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = if (isShizukuReady) "🟢" else if (isBinderAlive) "🟡" else "⚪", fontSize = 18.sp)
                    Text(
                        text = if (isShizukuReady) {
                            Strings.get("สถานะ: เชื่อมต่อและพร้อมใช้งาน (100%)", "Status: Connected & Ready")
                        } else if (isBinderAlive) {
                            Strings.get("สถานะ: รอการอนุญาตสิทธิ์", "Status: Awaiting Permission")
                        } else {
                            Strings.get("สถานะ: ปิดใช้งาน (ยังไม่พบ Shizuku)", "Status: Disabled (Shizuku Inactive)")
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = if (isShizukuReady) {
                        Strings.get("ฟังก์ชันระดับสูงทั้งหมดปลดล็อกพร้อมใช้งาน", "All elevated features unlocked.")
                    } else {
                        Strings.get("ฟังก์ชันเหล่านี้จำเป็นต้องใช้สิทธิ์ Shizuku เพื่อเข้าถึงโฟลเดอร์ /Android/data/", "Elevated permissions required to access /Android/data/")
                    },
                    fontSize = 13.sp,
                    color = if (isShizukuReady) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                )
            }
        }

        if (!isShizukuReady) {
            Button(
                onClick = {
                    if (isBinderAlive) {
                        ShizukuStateManager.requestPermission()
                    } else {
                        ShizukuStateManager.checkShizuku()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isBinderAlive) "🔑 ${Strings.get("ขอยืนยันสิทธิ์ Shizuku", "Authorize Shizuku")}" else "🔄 ${Strings.get("ตรวจสอบการเชื่อมต่อใหม่", "Verify Shizuku")}")
            }
        }

        Text(
            text = Strings.get("📦 รายการฟังก์ชันที่ใช้สิทธิ์ Shizuku:", "📦 Shizuku-Protected Actions:"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isShizukuReady) MaterialTheme.colorScheme.onBackground else Color.Gray
        )

        // Tool 1: 1-Click Sync
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isShizukuReady) MaterialTheme.colorScheme.surface else Color(0xFFF0F0F0)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🚀 ${Strings.get("1-Click Data Sync", "1-Click Data Sync")}",
                        fontWeight = FontWeight.Bold,
                        color = if (isShizukuReady) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                    Text(
                        text = Strings.get("ซิงค์เซฟและม็อดเข้าโฟลเดอร์เกมโดยตรง", "Directly sync saves and mods into game folder."),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Button(
                    onClick = {
                        val output = bridge.execCommand("ls -la /storage/emulated/0/Android/data/com.zane.stardewvalley/files/saves/")
                        scanResult = "Direct Sync Complete:\n$output"
                    },
                    enabled = isShizukuReady,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color.DarkGray
                    )
                ) {
                    Text(Strings.get("เริ่มซิงค์", "Sync"))
                }
            }
        }

        // Tool 2: Direct Mod Installer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isShizukuReady) MaterialTheme.colorScheme.surface else Color(0xFFF0F0F0)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📥 ${Strings.get("ติดตั้งม็อดเข้าโฟลเดอร์เกม", "Install .zip to Game Folder")}",
                        fontWeight = FontWeight.Bold,
                        color = if (isShizukuReady) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                    Text(
                        text = Strings.get("แตกไฟล์ .zip ลงใน /Android/data/.../Mods/", "Extract .zip directly into /Android/data/.../Mods/"),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Button(
                    onClick = {
                        val output = bridge.execCommand("ls -1 /storage/emulated/0/Android/data/com.zane.stardewvalley/files/Mods/")
                        scanResult = "Installed Mods in /Android/data/:\n$output"
                    },
                    enabled = isShizukuReady,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color.DarkGray
                    )
                ) {
                    Text(Strings.get("ติดตั้ง", "Install"))
                }
            }
        }

        // Diagnostic / Output Result
        scanResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Result Console:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(result, color = Color(0xFF4CAF50), fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }
        }
    }
}
