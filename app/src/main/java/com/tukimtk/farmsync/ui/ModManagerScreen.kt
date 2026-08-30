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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukimtk.farmsync.i18n.Strings

@Composable
fun ModManagerScreen() {
    var sveEnabled by remember { mutableStateOf(true) }
    var uiInfoEnabled by remember { mutableStateOf(true) }
    var translationEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(Strings.get("📦 ตัวจัดการม็อด (Mod Manager)", "📦 1-Click Mod Manager"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { /* Install Zip */ }) {
                Text(Strings.get("+ ติดตั้งไฟล์ .zip", "+ Install .zip"))
            }
        }

        Text(
            text = Strings.get(
                "จัดการม็อด SMAPI และ Content Patcher ในโฟลเดอร์ /Android/data/ ได้โดยตรง ไม่ต้องรูทเครื่อง",
                "Manage SMAPI & Content Patcher mods directly in /Android/data/ without root."
            ),
            fontSize = 13.sp,
            color = Color.Gray
        )

        // Mod 1
        ModCard(
            name = "Stardew Valley Expanded",
            author = "FlashShifter",
            version = "v1.14.24",
            isEnabled = sveEnabled,
            onToggle = { sveEnabled = it }
        )

        // Mod 2
        ModCard(
            name = "UI Info Suite 2",
            author = "Annosz",
            version = "v2.3.3",
            isEnabled = uiInfoEnabled,
            onToggle = { uiInfoEnabled = it }
        )

        // Mod 3
        ModCard(
            name = Strings.get("ม็อดแปลบทสนทนาภาษาไทย (AI)", "AI Thai Dialogue Localization"),
            author = "FarmSync AI (BYOK)",
            version = "v1.0.0",
            isEnabled = translationEnabled,
            onToggle = { translationEnabled = it }
        )
    }
}

@Composable
fun ModCard(
    name: String,
    author: String,
    version: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("By $author | $version", fontSize = 13.sp, color = Color.Gray)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}
