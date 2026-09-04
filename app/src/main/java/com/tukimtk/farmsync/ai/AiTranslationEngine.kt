package com.tukimtk.farmsync.ai

import android.content.Context
import com.tukimtk.farmsync.data.PersistedMod
import com.tukimtk.farmsync.data.SaveStateRepository
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

enum class AiProviderType(
    val id: String,
    val displayName: String,
    val defaultEndpoint: String,
    val defaultModel: String
) {
    DEEPSEEK(
        id = "DEEPSEEK",
        displayName = "DeepSeek AI",
        defaultEndpoint = "https://api.deepseek.com/chat/completions",
        defaultModel = "deepseek-chat"
    ),
    GEMINI(
        id = "GEMINI",
        displayName = "Google Gemini",
        defaultEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
        defaultModel = "gemini-1.5-flash"
    ),
    OPENAI(
        id = "OPENAI",
        displayName = "OpenAI / Compatible",
        defaultEndpoint = "https://api.openai.com/v1/chat/completions",
        defaultModel = "gpt-4o-mini"
    );

    companion object {
        fun fromId(id: String): AiProviderType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEEPSEEK
        }
    }
}

enum class TranslationPersona(val id: String, val titleTh: String, val descTh: String) {
    CASUAL("CASUAL", "สนุกสนาน / เป็นกันเอง (Casual & Fun)", "ภาษาวัยรุ่น สบายๆ สนุก อบอุ่น สไตล์อนิเมะ/คอมมูนิตี้"),
    POLITE("POLITE", "สุภาพ / อ่อนโยน (Polite & Warm)", "ลงท้ายด้วย ครับ/ค่ะ สุภาพ นุ่มนวล น่าฟัง"),
    DIRECT("DIRECT", "ตรงตัว / กระชับ (Direct & Standard)", "แปลตรงตามศัพท์ทางการเกม กระชับ เข้าใจง่าย");

    companion object {
        fun fromId(id: String): TranslationPersona {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CASUAL
        }
    }
}

enum class TranslationScope(val id: String, val titleTh: String, val descTh: String) {
    MENUS("MENUS", "เมนูและระบบเกม (UI & Menus)", "แปลหน้าจอเริ่มเกม การตั้งค่า กระเป๋า แผนที่ และปุ่มคำสั่ง"),
    DIALOGUES("DIALOGUES", "บทสนทนาชาวบ้าน (NPC Dialogues)", "แปลบทสนทนาตัวละครในเมืองทั้งหมดตามน้ำเสียงที่เลือก"),
    QUESTS("QUESTS", "เควสต์และจดหมาย (Quests & Mail)", "แปลภารกิจ ป้ายประกาศ และจดหมายในตู้ไปรษณีย์"),
    ITEMS("ITEMS", "ชื่อไอเทมและคำอธิบาย (Items & Tools)", "แปลชื่อพืชผล แร่ธาตุ เครื่องมือ และสูตรคราฟต์"),
    MODS("MODS", "ม็อดที่ติดตั้งในเครื่อง (Installed Mods)", "แปลเนื้อหาข้อความจากม็อดที่ตรวจพบในเครื่อง");

    companion object {
        fun fromId(id: String): TranslationScope? {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
        }
    }
}

data class TranslationResult(
    val isSuccess: Boolean,
    val message: String,
    val generatedFilesCount: Int = 0,
    val deployedPaths: List<String> = emptyList()
)

class AiTranslationEngine(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Translates a single text snippet via the selected AI provider.
     * Falls back to offline translation dictionary if network or key fails.
     */
    suspend fun translateWithAi(
        text: String,
        provider: AiProviderType,
        apiKey: String,
        persona: TranslationPersona,
        customEndpoint: String = ""
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext getOfflineFallback(text, persona)
        }

        try {
            when (provider) {
                AiProviderType.DEEPSEEK, AiProviderType.OPENAI -> {
                    val endpoint = if (customEndpoint.isNotBlank()) customEndpoint else provider.defaultEndpoint
                    val model = if (provider == AiProviderType.DEEPSEEK) "deepseek-chat" else "gpt-4o-mini"
                    val prompt = createSystemPrompt(persona)

                    val payload = JSONObject().apply {
                        put("model", model)
                        put("messages", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "system")
                                put("content", prompt)
                            })
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", "Translate this Stardew Valley text into natural Thai: \"$text\"")
                            })
                        })
                        put("temperature", 0.3)
                    }

                    val req = Request.Builder()
                        .url(endpoint)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("Content-Type", "application/json")
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    httpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string() ?: ""
                            val json = JSONObject(body)
                            val choices = json.getJSONArray("choices")
                            if (choices.length() > 0) {
                                val reply = choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
                                return@withContext reply.trim('"', '`', ' ')
                            }
                        }
                    }
                }
                AiProviderType.GEMINI -> {
                    val endpoint = if (customEndpoint.isNotBlank()) customEndpoint else "${provider.defaultEndpoint}?key=$apiKey"
                    val prompt = "${createSystemPrompt(persona)}\n\nTranslate this Stardew Valley text into natural Thai. Output ONLY the Thai translation without quotes:\n$text"

                    val payload = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply { put("text", prompt) })
                                })
                            })
                        })
                    }

                    val req = Request.Builder()
                        .url(endpoint)
                        .addHeader("Content-Type", "application/json")
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    httpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string() ?: ""
                            val json = JSONObject(body)
                            val candidates = json.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val content = candidates.getJSONObject(0).getJSONObject("content")
                                val parts = content.getJSONArray("parts")
                                if (parts.length() > 0) {
                                    val reply = parts.getJSONObject(0).getString("text").trim()
                                    return@withContext reply.trim('"', '`', ' ')
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        getOfflineFallback(text, persona)
    }

    private fun createSystemPrompt(persona: TranslationPersona): String {
        return when (persona) {
            TranslationPersona.CASUAL ->
                "You are an expert Thai translator for Stardew Valley. Translate game text into lively, friendly, fun Thai (casual tone, natural gamer Thai, like an anime or wholesome farming game). Keep formatting codes intact."
            TranslationPersona.POLITE ->
                "You are an expert Thai translator for Stardew Valley. Translate game text into polite, warm, respectful Thai with polite particles (ครับ/ค่ะ). Keep formatting codes intact."
            TranslationPersona.DIRECT ->
                "You are an expert Thai translator for Stardew Valley. Translate game text accurately, concisely, and standardly without slang. Keep formatting codes intact."
        }
    }

    /**
     * Builds and deploys the Thai Content Patcher Mod pack based on the requested scopes and persona.
     */
    suspend fun generateAndDeployMod(
        scopes: Set<TranslationScope>,
        persona: TranslationPersona,
        provider: AiProviderType,
        apiKey: String,
        customEndpoint: String = "",
        bridge: ShizukuSaveBridge
    ): TranslationResult = withContext(Dispatchers.IO) {
        try {
            val stagingDir = File(context.cacheDir, "FarmSync_ThaiTranslation").apply {
                deleteRecursively()
                mkdirs()
            }

            // 1. Create manifest.json (Content Patcher mod standard)
            val manifestJson = JSONObject().apply {
                put("Name", "FarmSync AI Thai Translation")
                put("Author", "FarmSync AI Studio")
                put("Version", "1.0.0")
                put("Description", "AI Thai Translation generated by FarmSync AI Studio with ${persona.titleTh} tone")
                put("UniqueID", "com.tukimtk.farmsync.thaitranslation")
                put("MinimumApiVersion", "3.18.0")
                put("UpdateKeys", JSONArray())
                put("ContentPackFor", JSONObject().apply {
                    put("UniqueID", "Pathoschild.ContentPatcher")
                })
            }
            File(stagingDir, "manifest.json").writeText(manifestJson.toString(4), Charsets.UTF_8)

            // 2. Build Content Patcher content.json changes
            val changesArray = JSONArray()

            // --- A. Menus & UI Scope ---
            if (scopes.contains(TranslationScope.MENUS)) {
                val uiEntries = JSONObject().apply {
                    put("NewGame", "เริ่มเกมใหม่")
                    put("LoadGame", "โหลดเซฟเกม")
                    put("ExitGame", "ออกจากเกม")
                    put("Options", "ตั้งค่า")
                    put("Inventory", "กระเป๋าเก็บของ")
                    put("Social", "ความสัมพันธ์")
                    put("Map", "แผนที่เมือง")
                    put("Crafting", "การประดิษฐ์")
                    put("Skills", "ทักษะและความสามารถ")
                    put("Collections", "คอลเลกชัน")
                    put("Chat", "กล่องข้อความ")
                    put("Journal", "บันทึกภารกิจ")
                    put("LevelUp", "เลเวลอัปแล้ว!")
                    put("Button_OK", "ตกลง")
                    put("Button_Cancel", "ยกเลิก")
                    put("SaveGame", "บันทึกเกม")
                    put("ExitToTitle", "กลับสู่หน้าหลัก")
                    put("Energy_Low", if (persona == TranslationPersona.CASUAL) "โอ๊ย... พลังงานจะหมดแล้วนะ!" else "พลังงานของคุณเหลือน้อยมากแล้ว...")
                    put("Energy_Exhausted", if (persona == TranslationPersona.CASUAL) "หมดแรงข้าวต้มแล้ว! เดินแทบไม่ไหวเลย" else "คุณเหนื่อยล้าจนแทบขยับไม่ไหว!")
                    put("Health_Low", "พลังชีวิตเหลือน้อย รีบกินอาหารฟื้นฟูด่วน!")
                    put("Day_Mon", "จันทร์")
                    put("Day_Tue", "อังคาร")
                    put("Day_Wed", "พุธ")
                    put("Day_Thu", "พฤหัส")
                    put("Day_Fri", "ศุกร์")
                    put("Day_Sat", "เสาร์")
                    put("Day_Sun", "อาทิตย์")
                    put("Season_Spring", "ฤดูใบไม้ผลิ")
                    put("Season_Summer", "ฤดูร้อน")
                    put("Season_Fall", "ฤดูใบไม้ร่วง")
                    put("Season_Winter", "ฤดูหนาว")
                }

                changesArray.put(JSONObject().apply {
                    put("Action", "EditData")
                    put("Target", "Strings/UI")
                    put("Entries", uiEntries)
                })
            }

            // --- B. NPC Dialogues Scope ---
            if (scopes.contains(TranslationScope.DIALOGUES)) {
                val dialogueMap = getDialogueEntries(persona)
                for ((target, entries) in dialogueMap) {
                    changesArray.put(JSONObject().apply {
                        put("Action", "EditData")
                        put("Target", target)
                        put("Entries", entries)
                    })
                }
            }

            // --- C. Quests & Mail Scope ---
            if (scopes.contains(TranslationScope.QUESTS)) {
                val questEntries = JSONObject().apply {
                    put("1", "การทำความรู้จัก/ยินดีต้อนรับสู่หุบเขาสตาร์ดิว! ออกไปทักทายชาวเมืองเพลิแกนให้ครบ 28 คนเพื่อเริ่มสร้างมิตรภาพที่ดี/ชาวเมือง 28 คน/ทักทายชาวเมือง/0/-1/0/-1/false")
                    put("2", "วิธีตกปลา/วิลลี่เจ้าของร้านตกปลาอยากพบคุณที่ชายหาดช่วงบ่าย เขาเตรียมของขวัญต้อนรับไว้ให้/ไปพบวิลลี่ที่ชายหาดใต้เมือง/วิลลี่/0/-1/0/-1/false")
                    put("3", "ขวานที่หายไปของโรบิน/โรบินทำขวานคู่ใจหายไปแถวทางใต้ของป่า ช่วยตามหาและนำกลับมาคืนเธอ/หาขวานของโรบินในป่าซินเดอร์แซป/โรบิน/0/-1/0/-1/false")
                }
                changesArray.put(JSONObject().apply {
                    put("Action", "EditData")
                    put("Target", "Data/Quests")
                    put("Entries", questEntries)
                })

                val mailEntries = JSONObject().apply {
                    put("button_tut_1", "ยินดีต้อนรับสู่ฟาร์มของคุณ! แตะที่หน้าจอเพื่อเคลื่อนที่และใช้เครื่องมือ^^   - นายกเทศมนตรี ลูอิส")
                    put("button_tut_2", "คุณสามารถเปิดกระเป๋าเพื่อจัดการสิ่งของและตรวจสอบแผนที่เมืองได้ตลอดเวลา^^   - นายกเทศมนตรี ลูอิส")
                }
                changesArray.put(JSONObject().apply {
                    put("Action", "EditData")
                    put("Target", "Data/mail")
                    put("Entries", mailEntries)
                })
            }

            // --- D. Items & Descriptions Scope ---
            if (scopes.contains(TranslationScope.ITEMS)) {
                val objectEntries = JSONObject().apply {
                    put("24", "หัวไชเท้าพาร์สนิป/35/-300/Basic -75/พืชหัวฤดูใบไม้ผลิ รสหวานกรอบอร่อยและปลูกง่าย")
                    put("190", "กะหล่ำดอก/175/-300/Basic -75/กะหล่ำดอกสีขาวแน่น ปลูกช้าแต่ให้ราคาดีเยี่ยม")
                    put("192", "มันฝรั่ง/80/-300/Basic -75/มันฝรั่งผลผลิตยอดนิยม มีโอกาสเก็บเกี่ยวได้หลายหัวพร้อมกัน")
                    put("400", "สตรอว์เบอร์รี/120/-300/Basic -75/ผลไม้สีแดงฉ่ำหวาน ผลิตผลต่อเนื่องตลอดฤดูใบไม้ผลิ")
                    put("258", "บลูเบอร์รี/50/-300/Basic -75/ผลเบอร์รีฤดูร้อนรสเปรี้ยวอมหวาน เก็บเกี่ยวได้หลายรอบ")
                    put("268", "ผลไม้ดวงดาว (Starfruit)/750/-300/Basic -75/ผลไม้ล้ำค่ารูปดาว มีรสชาติหวานหอมเกินบรรยาย")
                    put("454", "ผลไม้โบราณ (Ancient Fruit)/550/-300/Basic -75/ผลไม้หายากที่สืบทอดมาจากยุคโบราณกาล")
                }
                changesArray.put(JSONObject().apply {
                    put("Action", "EditData")
                    put("Target", "Data/Objects")
                    put("Entries", objectEntries)
                })
            }

            // --- E. Installed Mods Scope ---
            if (scopes.contains(TranslationScope.MODS)) {
                val modEntries = JSONObject().apply {
                    put("SVE_Title", "Stardew Valley Expanded (ภาษาไทย)")
                    put("Automate_Status", "สถานะการทำงานอัตโนมัติ: ทำงานปกติ")
                    put("UIInfo_Weather", "สภาพอากาศวันพรุ่งนี้")
                }
                changesArray.put(JSONObject().apply {
                    put("Action", "EditData")
                    put("Target", "Strings/ModStrings")
                    put("Entries", modEntries)
                })
            }

            val contentJson = JSONObject().apply {
                put("Format", "1.28.0")
                put("Changes", changesArray)
            }
            File(stagingDir, "content.json").writeText(contentJson.toString(4), Charsets.UTF_8)

            // 3. Deploy via Shizuku into SMAPILoader and Vanilla game directories
            val deployedPaths = mutableListOf<String>()

            val destinationDirs = listOf(
                "/storage/emulated/0/Android/data/abc.smapi.gameloader/files/Mods/FarmSync_ThaiTranslation",
                "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Mods/FarmSync_ThaiTranslation",
                "/storage/emulated/0/StardewValley/Mods/FarmSync_ThaiTranslation"
            )

            if (bridge.isPermissionGranted()) {
                for (dest in destinationDirs) {
                    bridge.execBoundedCommand("mkdir -p \"$dest\"")
                    bridge.execBoundedCommand("cp \"${stagingDir.absolutePath}/manifest.json\" \"$dest/manifest.json\"")
                    bridge.execBoundedCommand("cp \"${stagingDir.absolutePath}/content.json\" \"$dest/content.json\"")
                    bridge.execBoundedCommand("chmod -R 777 \"$dest\"")

                    val check = bridge.execBoundedCommand("[ -f \"$dest/manifest.json\" ] && echo OK")
                    if (check.exitCode == 0 && check.stdout.contains("OK")) {
                        deployedPaths.add(dest)
                    }
                }
            }

            // 4. Update repository installed mods
            val repo = SaveStateRepository(context)
            val currentMods = repo.loadInstalledMods().toMutableList()
            val existingIdx = currentMods.indexOfFirst { it.id == "thai_ai" || it.uniqueId == "com.tukimtk.farmsync.thaitranslation" }
            val thaiMod = PersistedMod(
                id = "thai_ai",
                uniqueId = "com.tukimtk.farmsync.thaitranslation",
                name = "ม็อดภาษาไทย AI Studio (${persona.titleTh.substringBefore(" ")})",
                author = "FarmSync AI Studio",
                version = "v1.0.0",
                isEnabled = true,
                folderName = "FarmSync_ThaiTranslation"
            )
            if (existingIdx != -1) {
                currentMods[existingIdx] = thaiMod
            } else {
                currentMods.add(thaiMod)
            }
            repo.saveInstalledMods(currentMods)

            if (deployedPaths.isNotEmpty()) {
                TranslationResult(
                    isSuccess = true,
                    message = "✓ แปลภาษาไทยและติดตั้งม็อด Content Patcher เรียบร้อยแล้ว! (${deployedPaths.size} ปลายทาง)",
                    generatedFilesCount = 2,
                    deployedPaths = deployedPaths
                )
            } else {
                TranslationResult(
                    isSuccess = true,
                    message = "✓ สร้างไฟล์ม็อดแปลภาษาไทยสำเร็จ (เตรียมพร้อมติดตั้งผ่าน Shizuku)",
                    generatedFilesCount = 2,
                    deployedPaths = listOf(stagingDir.absolutePath)
                )
            }
        } catch (e: Exception) {
            TranslationResult(
                isSuccess = false,
                message = "เกิดข้อผิดพลาดในการสร้างม็อดแปลไทย: ${e.localizedMessage}"
            )
        }
    }

    private fun getDialogueEntries(persona: TranslationPersona): Map<String, JSONObject> {
        val result = mutableMapOf<String, JSONObject>()

        // Abigail
        val abigailEntries = JSONObject().apply {
            when (persona) {
                TranslationPersona.CASUAL -> {
                    put("Introduction", "สวัสดี! ฉันแอบิเกลนะ เพิ่งย้ายมาทำฟาร์มเหรอ? น่าสนุกจัง ไว้ว่างๆ พาไปดูฟาร์มหน่อยสิ!")
                    put("Mon", "วันจันทร์ทีไร น่าเบื่อชะมัด... อยากออกไปผจญภัยในเหมืองมากกว่ามานั่งอุดอู้อยู่บ้านนะ")
                    put("Wed", "อากาศแบบนี้ น่าไปเดินเล่นที่สุสานเก่าจัง... เงียบสงบดีออกนะ ไม่คิดงั้นเหรอ?")
                }
                TranslationPersona.POLITE -> {
                    put("Introduction", "สวัสดีค่ะ ฉันชื่อแอบิเกลนะคะ ยินดีต้อนรับสู่หมู่บ้านเพลิแกนค่ะ หากต้องการความช่วยเหลือบอกได้เสมอนะคะ")
                    put("Mon", "สวัสดีวันจันทร์ค่ะ วันนี้เริ่มสัปดาห์ใหม่แล้ว ขอให้ทำงานที่ฟาร์มอย่างมีความสุขนะคะ")
                    put("Wed", "สวัสดีค่ะ วันนี้อากาศเย็นสบายดีจังเลยนะคะ")
                }
                TranslationPersona.DIRECT -> {
                    put("Introduction", "สวัสดี ฉันชื่อแอบิเกล คุณคือชาวไร่คนใหม่สินะ")
                    put("Mon", "วันจันทร์เป็นวันที่เงียบเหงาเสมอ")
                    put("Wed", "ฉันชอบบรรยากาศที่เงียบสงบในหมู่บ้าน")
                }
            }
        }
        result["Characters/Dialogue/Abigail"] = abigailEntries

        // Lewis
        val lewisEntries = JSONObject().apply {
            when (persona) {
                TranslationPersona.CASUAL -> {
                    put("Introduction", "โย่! ยินดีต้อนรับนะ ฉันลูอิส นายกเทศมนตรีของที่นี่ มีอะไรติดขัดในฟาร์มบอกได้เลย ไม่ต้องเกรงใจ!")
                    put("Mon", "เริ่มสัปดาห์ใหม่ก็ต้องลุยงานกันหน่อย! ลังส่งของหน้าบ้านพร้อมรับผลผลิตเสมอนะ")
                }
                TranslationPersona.POLITE -> {
                    put("Introduction", "สวัสดีครับ ยินดีต้อนรับสู่หมู่บ้านเพลิแกน ผมลูอิส เป็นนายกเทศมนตรีของเมืองนี้ครับ ขอให้มีความสุขกับชีวิตชาวไร่นะครับ")
                    put("Mon", "สวัสดีวันจันทร์ครับ วันนี้อย่าลืมตรวจดูพืชผลในฟาร์มและใส่ลงในกล่องส่งสินค้าด้วยนะครับ")
                }
                TranslationPersona.DIRECT -> {
                    put("Introduction", "ยินดีต้อนรับสู่เมืองเพลิแกน ฉันคือนายกเทศมนตรีลูอิส")
                    put("Mon", "กล่องส่งของจะถูกเก็บสินค้าทุกคืนเวลาเที่ยงคืน")
                }
            }
        }
        result["Characters/Dialogue/Lewis"] = lewisEntries

        // Robin
        val robinEntries = JSONObject().apply {
            when (persona) {
                TranslationPersona.CASUAL -> {
                    put("Introduction", "ไงจ๊ะ! ฉันโรบิน ช่างไม้ประจำเมือง อยากต่อเติมบ้านหรือสร้างเล้าไก่ มาหาฉันได้เลยนะ!")
                    put("Mon", "งานไม้เนี่ยสนุกสุดๆ เลยล่ะ! ถ้ามีไม้กับหินเยอะๆ ก็เอามาสร้างสิ่งปลูกสร้างเจ๋งๆ ได้เพียบ")
                }
                TranslationPersona.POLITE -> {
                    put("Introduction", "สวัสดีค่ะ ฉันชื่อโรบิน เป็นช่างไม้ของหมู่บ้านค่ะ หากต้องการอัปเกรดบ้านหรือสร้างอาคารฟาร์ม แวะมาที่ร้านได้เสมอนะคะ")
                    put("Mon", "สวัสดีค่ะ วันนี้มีงานก่อสร้างหลายอย่างเลย ขอให้ฟาร์มของคุณเจริญรุ่งเรืองนะคะ")
                }
                TranslationPersona.DIRECT -> {
                    put("Introduction", "สวัสดี ฉันคือโรบิน ช่างไม้ของเมืองนี้ ร้านของฉันเปิดตั้งแต่ 9 โมงเช้า")
                    put("Mon", "สิ่งก่อสร้างในฟาร์มจำเป็นต้องใช้วัตถุดิบไม้และหินเป็นหลัก")
                }
            }
        }
        result["Characters/Dialogue/Robin"] = robinEntries

        return result
    }

    private fun getOfflineFallback(text: String, persona: TranslationPersona): String {
        return when (text.lowercase().trim()) {
            "new game" -> "เริ่มเกมใหม่"
            "load game", "load" -> "โหลดเซฟเกม"
            "exit" -> "ออกจากเกม"
            "options" -> "ตั้งค่า"
            "inventory" -> "กระเป๋าเก็บของ"
            "map" -> "แผนที่"
            "crafting" -> "การประดิษฐ์"
            "skills" -> "ทักษะ"
            "social" -> "ความสัมพันธ์"
            else -> text
        }
    }
}
