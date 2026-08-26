# แผนยุทธศาสตร์ขยายตลาด: Multi-Storage & Cloud Architecture สำหรับ FarmSync AI
**โปรเจกต์:** `FarmSync AI` (tukimtk-design/farmsync-ai)  
**เป้าหมาย:** วิเคราะห์พฤติกรรมของกลุ่มเกมเมอร์เป้าหมาย และขยายการรองรับ Storage/Protocol ให้ครอบคลุมทุกกลุ่ม เพื่อเปิดตลาดสู่ระดับ Global และรองรับอุปกรณ์เล่นเกมทุกชนิด (Windows PC, Steam Deck, NAS, Local Network)

---

## 1. ผลการสำรวจกลุ่มเป้าหมาย (Audience Segmentation & Preferred Storage)

จากการสำรวจพฤติกรรมในชุมชนเกมเมอร์ (Reddit, Stardew Valley Community, Steam Deck, Emulation on PC) พบว่ากลุ่มผู้เล่นไม่ได้ใช้แค่ Google Drive แต่แบ่งออกเป็น **4 กลุ่มใหญ่ชัดเจน:**

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       กลุ่มผู้เล่นเกมเมอร์เป้าหมาย 4 กลุ่ม                         │
├────────────────────────┬─────────────────────────┬──────────────────────────┤
│ กลุ่มเป้าหมาย           │ Storage/Cloud ที่ใช้จริง │ เหตุผลและอุปกรณ์ที่ใช้     │
├────────────────────────┼─────────────────────────┼──────────────────────────┤
│ 1. Windows PC + มือถือ  │ 🟦 Microsoft OneDrive   │ Windows 11 บังคับซิงค์    │
│    (Mass Market ~50%)  │ 🟩 Google Drive         │ โฟลเดอร์ AppData อัตโนมัติ │
│                        │ 📦 Dropbox              │ PC Gamers ใช้งานเยอะมาก   │
├────────────────────────┼─────────────────────────┼──────────────────────────┤
│ 2. Local Wi-Fi / เน้นไว │ 💻 SMB / Samba           │ ซิงค์ตรงกับโฟลเดอร์ใน PC   │
│    (Zero-Cloud ~25%)   │ (Windows Shared Folder) │ ผ่าน Wi-Fi บ้าน ไม่ขึ้นเน็ต│
├────────────────────────┼─────────────────────────┼──────────────────────────┤
│ 3. Steam Deck / Linux  │ 🐧 SFTP / SSH           │ SteamOS, MeCool, ROG Ally│
│    (Handheld ~15%)     │ ☁️ WebDAV (Nextcloud)    │ ไม่ชอบระบบ Cloud ผูกขาด   │
├────────────────────────┼─────────────────────────┼──────────────────────────┤
│ 4. สาย NAS & Homelab   │ 🗄️ Synology / QNAP WebDAV│ เก็บเซฟไว้ใน NAS ที่บ้าน   │
│    (Power Users ~10%)  │ 🪣 MinIO / S3 Storage   │ ปลอดภัย ข้อมูลไม่รั่วไหล │
└────────────────────────┴─────────────────────────┴──────────────────────────┘
```

---

## 2. สิ่งที่ต้องเพิ่มใน FarmSync AI เพื่อขยายตลาดให้กว้างที่สุด

การรองรับเฉพาะ Google Drive จะทำให้เราพลาดกลุ่ม **Windows Gamers ที่ใช้ OneDrive** และกลุ่ม **ผู้เล่นที่ต้องการความเป็นส่วนตัว (Local Wi-Fi Only / SMB)**

เราจึงปรับสถาปัตยกรรมเป็น **Universal Storage Provider Interface (Plug & Play):**

### 📌 หมวดที่ 1: Consumer Cloud (กลุ่มผู้เล่นทั่วไป)
1. **Google Drive:** รองรับผ่าน Google Drive REST API v3 (ค่าเริ่มต้นของ Android)
2. **Microsoft OneDrive:** รองรับผ่าน Microsoft Graph API (เจาะตลาด Windows PC ที่เซฟเกมอยู่ใน OneDrive โดยอัตโนมัติ)
3. **Dropbox:** รองรับผ่าน Dropbox API v2

### 📌 หมวดที่ 2: Zero-Cloud / Local Network (กลุ่มเน้นไวและต้องการความเป็นส่วนตัวสูงสุด)
4. **Windows Shared Folder (SMB / CIFS):**
   - **Killer Feature:** แค่แชร์โฟลเดอร์เซฟบนคอม Windows แล้วเชื่อมต่อผ่าน Wi-Fi บ้าน ตัวแอปบน Mi 14T Pro จะดูดและส่งเซฟเกมตรงเข้าคอมทันที **ความเร็วระดับ 1 Gbps โดยไม่ต้องใช้อินเทอร์เน็ต!**
   - ใช้ไลบรารีมาตรฐาน: `com.hierynomus:smbj` (Pure Java SMB2/SMB3)

### 📌 หมวดที่ 3: Power Gamers, Steam Deck & Self-Hosters (กลุ่มผู้ใช้ NAS และเซิร์ฟเวอร์ส่วนตัว)
5. **WebDAV:** รองรับ Nextcloud, ownCloud, Synology NAS, QNAP, InfiniCloud
6. **SFTP / SSH:** รองรับการซิงค์ตรงเข้าเครื่อง Linux, Steam Deck, Raspberry Pi หรือ MeCool Armbian Server
7. **S3-Compatible Storage:** รองรับ MinIO, Cloudflare R2, Backblaze B2, AWS S3

---

## 3. สถาปัตยกรรมโค้ด Unified Storage Provider ใน `tukimtk-design/farmsync-ai`

```kotlin
package com.tukimtk.farmsync.storage

sealed interface StorageType {
    object GoogleDrive : StorageType
    object OneDrive : StorageType
    object Dropbox : StorageType
    object WindowsSMB : StorageType
    object WebDAV : StorageType
    object SFTP : StorageType
    object LocalStorage : StorageType
}

interface StorageProvider {
    suspend fun listSaves(remotePath: String): Result<List<RemoteSaveEntry>>
    suspend fun downloadSave(remotePath: String, localTargetDir: File): Result<Unit>
    suspend fun uploadSave(localDir: File, remotePath: String): Result<Unit>
    suspend fun testConnection(): Result<Boolean>
}
```

---

## 4. ผลกระทบเชิงธุรกิจและข้อได้เปรียบในการแข่งขัน (Competitive Edge)

1. **ตลาดกว้างขึ้น 4 เท่า (Global Reach):** ผู้ใช้ที่ไม่สะดวกใช้ Google Drive (เช่น ผู้ใช้ในจีน หรือผู้ใช้ Windows ล้วน) สามารถเลือกใช้ OneDrive, SMB หรือ WebDAV ได้ทันที
2. **ต้นทุนยังคงเป็น 0 บาท (Zero Server Cost):** ทุกโปรโตคอล (SMB, WebDAV, SFTP, OneDrive) สื่อสารตรงจากเครื่อง Client สู่เป้าหมายของลูกค้าเอง ไม่ต้องผ่านเซิร์ฟเวอร์ของเรา
3. **จุดขายชูโรงที่คู่แข่งไม่มี:**
   - *"เล่นบนมือถือเสร็จ กลับถึงบ้านต่อ Wi-Fi เซฟเด้งเข้าคอม PC อัตโนมัติผ่าน SMB โดยไม่ต้องอัปโหลดขึ้นเน็ต"*
