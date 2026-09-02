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
import com.tukimtk.farmsync.shizuku.ShizukuState
import com.tukimtk.farmsync.shizuku.ShizukuStateManager

@Composable
fun ShizukuToolsScreen() {
    val context = LocalContext.current
    val shizukuState by ShizukuStateManager.state.collectAsState()
    val isShizukuReady = shizukuState is ShizukuState.Ready
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
                    Text(text = if (isShizukuReady) "🟢" else "⚪", fontSize = 18.sp)
                    Text(
                        text = if (isShizukuReady) {
                            Strings.get("สถานะ: พร้อมใช้งาน", "Status: Ready")
                        } else {
                            Strings.get("สถานะ: ปิดใช้งาน", "Status: Disabled")
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = if (isShizukuReady) {
                        Strings.get("ฟังก์ชันระดับสูงทั้งหมดปลดล็อกพร้อมใช้งาน", "All elevated features unlocked.")
                    } else {
                        Strings.get("ฟังก์ชันเหล่านี้ไม่สามารถใช้งานได้เนื่องจาก Shizuku ยังไม่พร้อม (ขาดสิทธิ์หรือไม่ได้เปิดทำงาน)", "These features are disabled because Shizuku is not ready (missing permission or not running).")
                    },
                    fontSize = 13.sp,
                    color = if (isShizukuReady) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                )
            }
        }

        if (!isShizukuReady) {
            Button(
                onClick = {
                    if (shizukuState is ShizukuState.PermissionRequired) {
                        ShizukuStateManager.requestPermission()
                    } else {
                        ShizukuStateManager.refresh(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (shizukuState is ShizukuState.PermissionRequired) "🔑 ${Strings.get("ขอยืนยันสิทธิ์ Shizuku", "Authorize Shizuku")}" else "🔄 ${Strings.get("ตรวจสอบการเชื่อมต่อใหม่", "Verify Shizuku")}")
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
                        // Scan saves across all candidate paths (official pkg + Xiaomi aliases)
                        val paths = listOf(
                            "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves",
                            "/sdcard/Android/data/com.chucklefish.stardewvalley/files/Saves",
                            "/storage/sdcard0/Android/data/com.chucklefish.stardewvalley/files/Saves"
                        )
                        val results = paths.joinToString("\n\n") { p ->
                            val out = bridge.execCommand("ls -la \"$p\" 2>&1")
                            "[$p]\n${out.ifBlank { "(Not found or empty)" }}"
                        }
                        scanResult = "Save Scan Results:\n$results"
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
                        val output = bridge.execCommand("ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Mods/\" 2>&1")
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
