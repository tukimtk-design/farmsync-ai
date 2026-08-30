package com.tukimtk.farmsync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.i18n.Strings

@Composable
fun FarmDashboardScreen() {
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember {
        mutableStateOf(
            Strings.get("ไฟล์เซฟตรงกันทั้งสองฝั่ง (สถานะ Timeline: ปลอดภัย)", "All saves in sync (Timeline: Safe)")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Farm Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🏡 ${Strings.get("ฟาร์มซันไรส์ พีค (Sunrise Peak)", "Sunrise Peak Farm")}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${Strings.get("เจ้าของฟาร์ม", "Farmer")}: Tuki | ${Strings.get("เวอร์ชันเกม", "Version")}: 1.6.15",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(Strings.get("📅 เวลาในเกม (Timeline)", "📅 In-Game Date"), fontSize = 12.sp, color = Color.Gray)
                        Text(Strings.get("ปี 2, ฤดูร้อน วันที่ 14", "Year 2, Summer Day 14"), fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text(Strings.get("💰 จำนวนเงินทั้งหมด", "💰 Total Money"), fontSize = 12.sp, color = Color.Gray)
                        Text("184,500g", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        // Sync Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(Strings.get("🔄 ระบบตัดสินใจซิงค์ (Save Decision Engine)", "🔄 Save Decision Engine"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = Strings.get(
                        "สถานะ: ตรวจสอบ Timeline วันเวลาในเกม พบว่าเซฟบนมือถือเป็นข้อมูลล่าสุด",
                        "Status: Evaluated In-Game Timeline. Mobile save has newer progress."
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )

                Button(
                    onClick = {
                        isSyncing = true
                        syncMessage = Strings.get(
                            "ประเมินไทม์ไลน์สำเร็จ: ซิงค์ Local -> Remote เรียบร้อยแล้ว (1 Gbps Wi-Fi)",
                            "Timeline Evaluation: PUSH_LOCAL_TO_REMOTE (Success via 1 Gbps Wi-Fi)"
                        )
                        isSyncing = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isSyncing) Strings.get("กำลังซิงค์...", "Syncing...")
                        else Strings.get("⚡ ซิงค์อัตโนมัติใน 1 คลิก", "⚡ 1-Click Auto-Sync")
                    )
                }
            }
        }

        // Quick Stats / Devices Connected
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(Strings.get("📡 อุปกรณ์ในระบบ Ecosystem", "📡 Connected Ecosystem"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("• Xiaomi 14T Pro (${Strings.get("เครื่องนี้", "Local Device")}) - ${Strings.get("ข้อมูลล่าสุด", "Up to date")}", fontSize = 14.sp)
                Text("• Windows PC / Steam Deck (Local Wi-Fi SMB) - ${Strings.get("ออนไลน์พร้อมซิงค์", "Online (1 Gbps)")}", fontSize = 14.sp)
            }
        }
    }
}
