# แผนการพัฒนาและส่งมอบงานฉบับละเอียด (Production Execution Roadmap)
**โปรเจกต์:** `FarmSync AI: Multi-Game Save Sync, AI Translation & Mod Companion`  
**Repository:** `https://github.com/tukimtk-design/farmsync-ai`  
**เป้าหมาย:** พัฒนาแอปพลิเคชันระดับ Commercial-Grade ใช้งานได้สมบูรณ์แบบบน Xiaomi 14T Pro (HyperOS / Android 14+) และพร้อมวางจำหน่ายบน Google Play Store

---

## สรุปภาพรวมการแบ่งงาน 4 Waves (Smart Adaptive Batching 3 Slots/Wave)

```text
┌──────────────────────────────────────────────────────────────────────────────────┐
│                 FARMSYNC AI: 4-WAVE COMMERCIAL IMPLEMENTATION                    │
├──────────────────────────────────────────────────────────────────────────────────┤
│ 🌊 WAVE 1: Core Foundation & Multi-Storage Layer                                │
│   ├─ Slot 1.1: Shizuku IPC Binder & Universal Save XML Parser                    │
│   ├─ Slot 1.2: Multi-Storage Provider (Google Drive, OneDrive, SMB, WebDAV)      │
│   └─ Slot 1.3: Save Collision Matrix & Rolling 10-Slot Backup Snapshot           │
├──────────────────────────────────────────────────────────────────────────────────┤
│ 🌊 WAVE 2: AI Localization, Dynamic Story & HD Art Engine                       │
│   ├─ Slot 2.1: Stardew Tag Shield & Persona-Aware AI Translator (BYOK)           │
│   ├─ Slot 2.2: Dynamic Chronicle & Content Patcher Live Mod Compiler             │
│   └─ Slot 2.3: HD AI Story Illustration Formatter (512x512 PNG RGBA)             │
├──────────────────────────────────────────────────────────────────────────────────┤
│ 🌊 WAVE 3: Mod Automation & HyperOS Background Lifecycle Engine                  │
│   ├─ Slot 3.1: 1-Click SMAPI Mod Manager (Zip4j + Manifest + Toggle)             │
│   └─ Slot 3.2: HyperOS Game Watcher (Auto-Sync on Game Exit + Battery Keep-Alive)│
├──────────────────────────────────────────────────────────────────────────────────┤
│ 🌊 WAVE 4: Commercial-Grade UI & Play Store Distribution Suite                   │
│   ├─ Slot 4.1: Jetpack Compose Retro Dashboard & Storage Settings UI             │
│   └─ Slot 4.2: Google Play In-App Billing + Shizuku Onboarding Wizard            │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## รายละเอียดแต่ละ Wave และการสั่งงาน Google Jules (jules-mcp)

### 🌊 WAVE 1: Core Foundation & Multi-Storage Layer

#### 📌 Slot 1.1: Shizuku IPC Binder & Save XML Parser
- **Branch:** `feature/shizuku-storage-core`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/shizuku/ShizukuFileService.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/game/stardew/StardewSaveParser.kt`
  3. `app/src/main/java/com/tukimtk/farmsync/model/GameSaveMetadata.kt`
  4. `app/src/test/java/com/tukimtk/farmsync/StardewSaveParserTest.kt`
- **Objectives:**
  - สร้าง Native IPC Service สื่อสารกับ Shizuku เพื่ออ่าน/เขียน `/Android/data/com.zane.stardewvalley/` แบบไม่ต้องรูท
  - สร้าง Parser สกัดข้อมูลเซฟ XML: ชื่อฟาร์ม, ชื่อตัวละคร, ฤดูกาล, วันที่, ปี, และจำนวนเงิน
  - Unit Test ทดสอบความถูกต้องของการ Parse ข้อมูลเซฟ

#### 📌 Slot 1.2: Multi-Storage Provider Engine (GDrive, OneDrive, SMB, WebDAV)
- **Branch:** `feature/multi-storage-engine`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/storage/StorageProvider.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/storage/GoogleDriveProvider.kt`
  3. `app/src/main/java/com/tukimtk/farmsync/storage/SmbLocalProvider.kt`
  4. `app/src/test/java/com/tukimtk/farmsync/StorageProviderTest.kt`
- **Objectives:**
  - สร้าง Unified Interface `StorageProvider`
  - พัฒนา Driver รองรับ Google Drive REST API v3 และ Windows Shared Folder (SMB2/3 ผ่าน `smbj`)
  - Unit Test ทดสอบการเชื่อมต่อและส่งถ่ายไฟล์

#### 📌 Slot 1.3: Save Collision Matrix & Rolling Snapshot Backup
- **Branch:** `feature/save-collision-matrix`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/sync/SaveConflictMatrix.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/sync/RollingBackupManager.kt`
  3. `app/src/main/java/com/tukimtk/farmsync/sync/SyncCoordinator.kt`
  4. `app/src/test/java/com/tukimtk/farmsync/SaveConflictMatrixTest.kt`
- **Objectives:**
  - เปรียบเทียบ Timeline ในเกม (In-Game Date) ระหว่าง Local กับ Remote ป้องกันการเขียนทับเซฟที่ใหม่กว่า
  - จัดเก็บ Snapshot สำรองย้อนหลัง 10 สล็อตแบบ Zip อัตโนมัติก่อนซิงค์ทุกครั้ง
  - Unit Test จำลองสถานการณ์เซฟขัดแย้ง (Conflict Resolution Tests)

---

### 🌊 WAVE 2: AI Localization, Dynamic Story & HD Art Engine

#### 📌 Slot 2.1: Stardew Tag Shield & Persona-Aware AI Translator
- **Branch:** `feature/ai-tag-shield-translator`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/ai/StardewTagShield.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/ai/AiModTranslator.kt`
  3. `app/src/main/java/com/tukimtk/farmsync/ai/CharacterPersonaPrompt.kt`
  4. `app/src/test/java/com/tukimtk/farmsync/StardewTagShieldTest.kt`
- **Objectives:**
  - ป้องกัน Stardew Tag (`%firstname`, `^`, `$e`, `$h`, `[icons]`) ด้วย Regex Shield
  - ระบบแปลภาษาไทยผสาน Gemini 2.0 Flash แบบ BYOK ตามบุคลิกตัวละคร (Sebastian, Abigail, Linus ฯลฯ)
  - Unit Test ทดสอบความถูกต้องของ Tag Shield หลังการแปล

#### 📌 Slot 2.2: Dynamic Chronicle & Content Patcher Live Mod Compiler
- **Branch:** `feature/ai-dynamic-chronicle-patcher`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/ai/AiChronicleGenerator.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/patcher/ContentPatcherBuilder.kt`
  3. `app/src/main/java/com/tukimtk/farmsync/patcher/DynamicQuestInjector.kt`
  4. `app/src/test/java/com/tukimtk/farmsync/ContentPatcherBuilderTest.kt`
- **Objectives:**
  - แปลงข่าวและสถานการณ์จริงสู่เรื่องราว Pelican Town โดยไม่ทำลายเนื้อเรื่องหลัก
  - สร้างและคอมไพล์ Mod `[CP] Pelican Town Gazette` (แทรก `Data/mail` และ `Data/Quests`)
  - Unit Test ตรวจสอบความถูกต้องของ Content Patcher JSON

#### 📌 Slot 2.3: HD AI Story Illustration Formatter
- **Branch:** `feature/ai-hd-illustration-engine`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/ai/AiIllustrationGenerator.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/ai/PromptStylePresets.kt`
  3. `app/src/main/java/com/tukimtk/farmsync/patcher/ImageAssetFormatter.kt`
  4. `app/src/test/java/com/tukimtk/farmsync/ImageAssetFormatterTest.kt`
- **Objectives:**
  - สร้างภาพประกอบสไตล์ Stardew Concept Art และ HD Pixel Art 512x512 / 1024x1024
  - แปลงและจัดวางไฟล์ RGBA PNG ลงโฟลเดอร์ Assets ของ Content Patcher
  - Unit Test ตรวจสอบสัดส่วนและฟอร์แมตรูปภาพ

---

### 🌊 WAVE 3: Mod Automation & HyperOS Background Engine

#### 📌 Slot 3.1: 1-Click SMAPI Mod Manager (Zip4j)
- **Branch:** `feature/mod-manager-zip4j`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/mods/ModInstaller.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/mods/ModManifestParser.kt`
  3. `app/src/main/java/com/tukimtk/farmsync/model/StardewMod.kt`
  4. `app/src/test/java/com/tukimtk/farmsync/ModManifestParserTest.kt`
- **Objectives:** แตกไฟล์ Mod `.zip` ลง `Android/data/.../files/Mods/` ผ่าน Shizuku และรองรับสวิตช์เปิด-ปิด Mod

#### 📌 Slot 3.2: HyperOS Game Watcher Service
- **Branch:** `feature/hyperos-game-watcher`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/service/GameWatcherService.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/service/HyperOsOptimizer.kt`
  3. `app/src/main/AndroidManifest.xml`
- **Objectives:** ตรวจจับเมื่อออกจากเกม Stardew Valley เพื่อสั่งซิงค์ทันที พร้อมระบบขอสิทธิ์ Battery No-Restrictions

---

### 🌊 WAVE 4: Commercial-Grade UI & Play Store Suite

#### 📌 Slot 4.1: Jetpack Compose Retro Dashboard UI
- **Branch:** `feature/compose-retro-dashboard`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/ui/FarmDashboardScreen.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/ui/StorageSettingsScreen.kt`
  3. `app/src/main/java/com/tukimtk/farmsync/ui/ModManagerScreen.kt`
  4. `app/src/main/java/com/tukimtk/farmsync/ui/theme/Theme.kt`
- **Objectives:** สร้าง UI สไตล์ Retro Cozy แสดงการ์ดฟาร์ม สถานะการซิงค์ และปุ่มสลับ Storage

#### 📌 Slot 4.2: Google Play In-App Billing & Onboarding Wizard
- **Branch:** `feature/playstore-billing-onboarding`
- **Target Files:**
  1. `app/src/main/java/com/tukimtk/farmsync/billing/PlayBillingManager.kt`
  2. `app/src/main/java/com/tukimtk/farmsync/ui/ShizukuOnboardingScreen.kt`
  3. `app/src/main/java/com/tukimtk/farmsync/ui/ApiKeyConfigDialog.kt`
- **Objectives:** เชื่อมต่อระบบ Google Play Purchases สำหรับ Pro Unlock และหน้าสอนเปิด Shizuku ใน 3 ขั้นตอน
