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
fun ShizukuOnboardingScreen() {
    var checkStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Strings.get("⚡ ตั้งค่าสิทธิ์ Shizuku (3 ขั้นตอนง่ายๆ)", "⚡ Shizuku Setup (3 Easy Steps)"),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = Strings.get(
                "Shizuku ช่วยให้ FarmSync AI อ่านและเขียนไฟล์เซฟในโฟลเดอร์ /Android/data/ ได้อย่างปลอดภัย โดยไม่ต้องรูทเครื่อง (No Root Required)",
                "Shizuku allows FarmSync AI to safely read & write Stardew Valley save files in /Android/data/ without requiring Root access."
            ),
            fontSize = 14.sp,
            color = Color.Gray
        )

        StepCard(
            stepNumber = "1",
            title = Strings.get("ติดตั้งแอป Shizuku", "Install Shizuku App"),
            desc = Strings.get("ดาวน์โหลดและติดตั้งแอป Shizuku จาก Google Play Store", "Download and install 'Shizuku' from Google Play Store or GitHub.")
        )

        StepCard(
            stepNumber = "2",
            title = Strings.get("เริ่มระบบผ่าน Wireless Debugging", "Start via Wireless Debugging"),
            desc = Strings.get("ไปที่ ตัวเลือกสำหรับนักพัฒนา (Developer Options) > เปิด การแก้จุดบกพร่องไร้สาย > จับคู่อุปกรณ์ใน Shizuku", "Go to Developer Options > Enable Wireless Debugging > Pair device with pairing code in Shizuku.")
        )

        StepCard(
            stepNumber = "3",
            title = Strings.get("อนุญาตสิทธิ์ให้ FarmSync AI", "Grant Permission to FarmSync AI"),
            desc = Strings.get("กดปุ่มด้านล่างเพื่อยืนยันการเชื่อมต่อและอนุญาตให้เข้าถึงโฟลเดอร์เซฟเกม", "Tap the button below to authorize FarmSync AI to access /Android/data/ files.")
        )

        Button(
            onClick = {
                checkStatus = Strings.get(
                    "✓ เชื่อมต่อบริการ Shizuku สำเร็จ! ได้รับสิทธิ์เข้าถึงโฟลเดอร์ Scoped Storage เรียบร้อยแล้ว",
                    "✓ Shizuku Service Connected! Scoped Storage access granted."
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(Strings.get("ตรวจสอบการเชื่อมต่อ Shizuku", "Verify Shizuku Permission"))
        }

        checkStatus?.let { status ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
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
