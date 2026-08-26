# พิมพ์เขียวสถาปัตยกรรม: FarmSync Companion PC & Steam Deck (SMAPI Mod)
**เป้าหมาย:** สร้าง SMAPI Mod (C# / .NET 6) สำหรับ PC (Windows/Mac/Linux) และ Steam Deck ที่ทำหน้าที่ตรวจสอบและซิงค์เซฟเกม Stardew Valley อัตโนมัติเมื่อเข้านอน (Day Saved) หรือเมื่อเปิดเกม โดยทำงานบน Background Thread แบบไร้การกระตุก 100%

---

## 1. สถาปัตยกรรมของ SMAPI C# Mod (`FarmSyncCompanion`)

### 🔹 การตรวจจับ Lifecycle ของเกม (SMAPI Event Hooks)
1. **`Helper.Events.GameLoop.GameLaunched` (เมื่อเปิดเกม):**
   - ตรวจสอบ Cloud / Local SMB ว่ามีเซฟใหม่กว่าที่เพิ่งเล่นมาจาก Android (Xiaomi 14T Pro) หรือไม่
   - หากมีเซฟใหม่กว่า จะแจ้งเตือนบนหน้าจอ (HUD Notification) และดึงมาอัปเดตให้อัตโนมัติ
2. **`Helper.Events.GameLoop.Saved` (เมื่อนอนหลับและเกมเซฟวันใหม่เสร็จ):**
   - คำนวณ In-Game Timeline (Year, Season, Day, Money)
   - สั่งซิงค์ขึ้น Storage เป้าหมายแบบ **Asynchronous (`Task.Run`)** ทันที เพื่อไม่ให้หน้าจอเกมค้าง
3. **`Helper.Events.GameLoop.ReturnedToTitle` (เมื่อออกจากเกมสู่หน้าแรก):**
   - สั่งตรวจเช็กความสมบูรณ์ของเซฟและทำการ Final Backup อีกครั้ง

### 🔹 โครงสร้าง Storage Driver ในฝั่ง C#
* **Local SMB / Windows Network Share:** ซิงค์ผ่าน UNC Path `\\192.168.1.xx\StardewSaves` บนเครือข่ายบ้าน (ไวที่สุด ความเร็วระดับ 1 Gbps)
* **OneDrive Local Folder:** ตรวจจับพาธ `%USERPROFILE%\OneDrive\StardewValley_Saves` อัตโนมัติ
* **Google Drive / WebDAV REST API:** สำหรับผู้ใช้ที่ต้องการซิงค์ผ่านคลาวด์ภายนอก
* **Rolling Backup Engine:** บีบอัด Zip เซฟย้อนหลัง 10 วันไว้ที่ `%APPDATA%\StardewValley\FarmSync_Backups` ป้องกันเซฟเสียหาย 100%

---

## 2. โครงสร้างไฟล์ของ PC Mod (`farmsync-companion-pc/`)

```text
farmsync-companion-pc/
├── FarmSyncCompanion.csproj      # .NET 6.0 SDK + Pathoschild.Stardew.ModBuildConfig
├── manifest.json                 # SMAPI Mod Manifest
├── ModEntry.cs                   # SMAPI Lifecycle Event Listeners
├── ModConfig.cs                  # Generic Mod Config Menu (GMCM) Data Model
├── sync/
│   ├── SaveWatcher.cs            # XML Timeline Inspector (Year/Season/Day)
│   ├── BackupEngine.cs           # ZipArchive Rolling 10-Slot Snapshots
│   └── ConflictDetector.cs       # Timestamp & In-Game Timeline Comparator
└── storage/
    ├── IStorageDriver.cs         # Unified Storage Interface
    ├── LocalSmbDriver.cs         # Windows File Sharing & Local Network UNC
    └── CloudDriveDriver.cs       # OneDrive / Google Drive Direct Client
```

---

## 3. แผนชุดคำสั่ง Micro-Tasks สำหรับ Google Jules (jules-mcp)

### 🚀 Task PC-1: SMAPI Lifecycle Engine & XML Save Watcher
```typescript
ow_jules_dispatch_task({
  repository: "tukimtk-design/farmsync-ai",
  branch: "feature/pc-smapi-mod-core",
  phaseName: "PC SMAPI Mod Core & Save Watcher",
  targetFiles: [
    "pc-mod/FarmSyncCompanion/ModEntry.cs",
    "pc-mod/FarmSyncCompanion/manifest.json",
    "pc-mod/FarmSyncCompanion/sync/SaveWatcher.cs",
    "pc-mod/FarmSyncCompanion.Tests/SaveWatcherTests.cs"
  ],
  objectives: [
    "Implement SMAPI ModEntry listening to GameLaunched and Saved events",
    "Implement SaveWatcher parsing in-game timeline (Year, Season, Day, Money) asynchronously",
    "Add xUnit tests in SaveWatcherTests.cs verifying XML parsing",
    "Run ONLY dotnet test --filter FullyQualifiedName~SaveWatcherTests",
    "Commit changes and create Pull Request"
  ]
})
```

### 🚀 Task PC-2: Multi-Storage Driver & Rolling Zip Backup
```typescript
ow_jules_dispatch_task({
  repository: "tukimtk-design/farmsync-ai",
  branch: "feature/pc-smapi-storage-driver",
  phaseName: "PC SMAPI Storage & Rolling Backup",
  targetFiles: [
    "pc-mod/FarmSyncCompanion/storage/IStorageDriver.cs",
    "pc-mod/FarmSyncCompanion/storage/LocalSmbDriver.cs",
    "pc-mod/FarmSyncCompanion/sync/BackupEngine.cs",
    "pc-mod/FarmSyncCompanion.Tests/BackupEngineTests.cs"
  ],
  objectives: [
    "Implement IStorageDriver and LocalSmbDriver for direct UNC / local folder syncing",
    "Implement BackupEngine generating rolling 10-slot zip archives before save overwrites",
    "Add xUnit tests in BackupEngineTests.cs verifying zip snapshot retention",
    "Run ONLY dotnet test --filter FullyQualifiedName~BackupEngineTests",
    "Commit changes and create Pull Request"
  ]
})
```
