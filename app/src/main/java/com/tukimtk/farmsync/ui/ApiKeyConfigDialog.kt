package com.tukimtk.farmsync.ui

import android.content.Intent
import android.net.Uri
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
import com.tukimtk.farmsync.ai.AiProviderType
import com.tukimtk.farmsync.ai.AiTranslationEngine
import com.tukimtk.farmsync.ai.TranslationPersona
import com.tukimtk.farmsync.ai.TranslationResult
import com.tukimtk.farmsync.ai.TranslationScope
import com.tukimtk.farmsync.data.SaveStateRepository
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge
import com.tukimtk.farmsync.i18n.Strings
import kotlinx.coroutines.launch

@Composable
fun ApiKeyConfigDialog(onDismiss: () -> Unit = {}) {
    val context = LocalContext.current
    val repo = remember { SaveStateRepository(context) }
    val bridge = remember { ShizukuSaveBridge(context) }
    val engine = remember { AiTranslationEngine(context) }
    val scope = rememberCoroutineScope()

    var selectedProvider by remember { mutableStateOf(AiProviderType.fromId(repo.getAiProvider())) }
    var deepseekKey by remember { mutableStateOf(repo.getDeepSeekApiKey()) }
    var geminiKey by remember { mutableStateOf(repo.getGeminiApiKey()) }
    var openaiKey by remember { mutableStateOf(repo.getOpenAiApiKey()) }
    var customEndpoint by remember { mutableStateOf(repo.getCustomEndpoint()) }

    var selectedPersona by remember { mutableStateOf(TranslationPersona.fromId(repo.getTranslationPersona())) }
    var currentScopeIds by remember { mutableStateOf(repo.getTranslationScopes()) }

    var isTranslating by remember { mutableStateOf(false) }
    var translationProgressText by remember { mutableStateOf("") }
    var resultDialog by remember { mutableStateOf<TranslationResult?>(null) }
    var isSavedToast by remember { mutableStateOf(false) }

    fun persistAll() {
        repo.setAiProvider(selectedProvider.id)
        repo.setDeepSeekApiKey(deepseekKey)
        repo.setGeminiApiKey(geminiKey)
        repo.setOpenAiApiKey(openaiKey)
        repo.setCustomEndpoint(customEndpoint)
        repo.setTranslationPersona(selectedPersona.id)
        repo.setTranslationScopes(currentScopeIds)
    }

    val activeKey = when (selectedProvider) {
        AiProviderType.DEEPSEEK -> deepseekKey
        AiProviderType.GEMINI -> geminiKey
        AiProviderType.OPENAI -> openaiKey
    }

    AlertDialog(
        onDismissRequest = { if (!isTranslating) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌐", fontSize = 22.sp)
                Column {
                    Text("AI Thai Translation Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        Strings.get("ระบบแปลภาษาไทยด้วย AI & BYOK", "AI Translation Studio (BYOK)"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Select AI Provider
                Text(
                    Strings.get("1. เลือกค่าย AI ผู้ให้บริการ (Provider)", "1. Select AI Provider"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AiProviderType.entries.forEach { provider ->
                        FilterChip(
                            selected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                            label = {
                                Text(
                                    when (provider) {
                                        AiProviderType.DEEPSEEK -> "DeepSeek"
                                        AiProviderType.GEMINI -> "Gemini"
                                        AiProviderType.OPENAI -> "OpenAI"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedProvider == provider) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. API Key Input
                when (selectedProvider) {
                    AiProviderType.DEEPSEEK -> {
                        OutlinedTextField(
                            value = deepseekKey,
                            onValueChange = { deepseekKey = it; isSavedToast = false },
                            label = { Text("DeepSeek API Key") },
                            placeholder = { Text("sk-...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "💡 แนะนำ: DeepSeek Chat ราคาประหยัด แปลสำนวนไทยได้เป็นธรรมชาติมาก (รับคีย์ฟรีที่ platform.deepseek.com)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AiProviderType.GEMINI -> {
                        OutlinedTextField(
                            value = geminiKey,
                            onValueChange = { geminiKey = it; isSavedToast = false },
                            label = { Text("Google Gemini API Key") },
                            placeholder = { Text("AIzaSy...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "💡 ใช้งานฟรีด้วย Google AI Studio (aistudio.google.com)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AiProviderType.OPENAI -> {
                        OutlinedTextField(
                            value = openaiKey,
                            onValueChange = { openaiKey = it; isSavedToast = false },
                            label = { Text("OpenAI API Key") },
                            placeholder = { Text("sk-...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customEndpoint,
                            onValueChange = { customEndpoint = it },
                            label = { Text("Custom Endpoint (ไม่ระบุก็ได้)") },
                            placeholder = { Text("https://api.openai.com/v1/chat/completions") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider()

                // 3. Translation Scope (What to translate)
                Text(
                    Strings.get("2. ขอบเขตเนื้อหาที่ต้องการแปล (What to translate)", "2. Translation Scopes"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                TranslationScope.entries.forEach { sc ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = currentScopeIds.contains(sc.id),
                            onCheckedChange = { checked ->
                                currentScopeIds = if (checked) currentScopeIds + sc.id else currentScopeIds - sc.id
                            }
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(sc.titleTh, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(sc.descTh, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                HorizontalDivider()

                // 4. Translation Persona / Tone (How to translate)
                Text(
                    Strings.get("3. สไตล์น้ำเสียงการแปล (Tone & Persona)", "3. Translation Persona"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                TranslationPersona.entries.forEach { persona ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedPersona == persona,
                            onClick = { selectedPersona = persona }
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(persona.titleTh, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(persona.descTh, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (isSavedToast) {
                    Text("✓ บันทึกการตั้งค่าเรียบร้อยแล้ว!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (isTranslating) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Text(translationProgressText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        persistAll()
                        isTranslating = true
                        translationProgressText = "กำลังเตรียมโครงสร้าง Content Patcher Mod..."
                        scope.launch {
                            try {
                                translationProgressText = "กำลังแปลและสังเคราะห์ไฟล์ภาษาไทยด้วย ${selectedProvider.displayName}..."
                                val scopesToRun = currentScopeIds.mapNotNull { scopeId -> TranslationScope.fromId(scopeId) }.toSet()
                                val res = engine.generateAndDeployMod(
                                    scopes = scopesToRun,
                                    persona = selectedPersona,
                                    provider = selectedProvider,
                                    apiKey = activeKey,
                                    customEndpoint = customEndpoint,
                                    bridge = bridge
                                )
                                isTranslating = false
                                resultDialog = res
                            } catch (e: Exception) {
                                isTranslating = false
                                resultDialog = TranslationResult(false, "เกิดข้อผิดพลาด: ${e.localizedMessage}")
                            }
                        }
                    },
                    enabled = !isTranslating,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("🚀 " + Strings.get("เริ่มแปลภาษาไทยและติดตั้งเข้าเกม", "Start AI Translation & Deploy"), fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            persistAll()
                            isSavedToast = true
                        },
                        enabled = !isTranslating
                    ) {
                        Text("💾 " + Strings.get("บันทึกการตั้งค่า", "Save Settings"))
                    }

                    TextButton(
                        onClick = onDismiss,
                        enabled = !isTranslating
                    ) {
                        Text(Strings.get("ปิด", "Close"))
                    }
                }
            }
        },
        dismissButton = {}
    )

    // Result Confirmation Dialog
    if (resultDialog != null) {
        val res = resultDialog!!
        AlertDialog(
            onDismissRequest = { resultDialog = null },
            title = {
                Text(if (res.isSuccess) "🎉 สำเร็จเรียบร้อย!" else "❌ ไม่สามารถติดตั้งได้")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(res.message, fontSize = 14.sp)
                    if (res.isSuccess && res.deployedPaths.isNotEmpty()) {
                        Text("📂 ไดเรกทอรีม็อดที่ติดตั้งแล้ว:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        res.deployedPaths.forEach { p ->
                            Text("• $p", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("💡 สามารถเปิดเกมผ่าน SMAPILoader เพื่อเล่นภาษาไทยได้ทันที!", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                if (res.isSuccess) {
                    Button(
                        onClick = {
                            resultDialog = null
                            onDismiss()
                            try {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage("abc.smapi.gameloader")
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                } else {
                                    val explicitIntent = Intent().apply {
                                        setClassName("abc.smapi.gameloader", "crc64e91f1276c636690c.LauncherActivity")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(explicitIntent)
                                }
                            } catch (_: Exception) {}
                        }
                    ) {
                        Text("▶️ " + Strings.get("เปิดเกม SMAPI ทันที", "Launch Game Now"))
                    }
                } else {
                    Button(onClick = { resultDialog = null }) {
                        Text("ตกลง")
                    }
                }
            },
            dismissButton = {
                if (res.isSuccess) {
                    TextButton(onClick = { resultDialog = null }) {
                        Text("ปิด")
                    }
                }
            }
        )
    }
}

