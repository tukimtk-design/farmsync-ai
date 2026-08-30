# สรุปแผนการพัฒนา FarmSync AI

แอปพลิเคชัน FarmSync AI ถูกออกแบบมาเพื่อตอบโจทย์ผู้เล่นเกมทำฟาร์ม (เช่น Stardew Valley) ที่ต้องการเล่นข้ามแพลตฟอร์มอย่างราบรื่น โดยมีฟีเจอร์เด่นตามที่คุณต้องการดังนี้:

## 1. ระบบซิงค์เซฟเกมและรองรับ Mod อย่างสมบูรณ์
* **Save Collision Matrix:** ระบบจะตรวจสอบ Timeline ภายในเกม (เช่น ฤดูกาล, วันที่, ปี, จำนวนเงิน) เพื่อป้องกันปัญหาการเขียนทับเซฟผิด หรือเซฟเก่าไปทับเซฟใหม่
* **1-Click Mod Manager:** มีระบบจัดการ Mod ที่สามารถติดตั้งไฟล์ `.zip` ลงไปในโฟลเดอร์เกมผ่านสิทธิ์ Shizuku ได้ทันที ทำให้การซิงค์เซฟที่ใช้ร่วมกับ Mod ทำงานได้อย่างสมบูรณ์

## 2. ระบบ AI Translation (BYOK)
* **Bring Your Own Key (BYOK):** ผู้เล่นสามารถนำ API Key ของตนเอง (เช่น Gemini API) มาใส่ในแอปได้ เพื่อใช้ในการแปลภาษาภายในเกมและบทสนทนา Mod เป็นภาษาไทยหรือภาษาอื่นๆ
* **Persona-Aware & Tag Shield:** ระบบจะทำการปกป้องโค้ดและแท็ก (Tags) ที่สำคัญของเกมไม่ให้ถูกทำลายจากการแปลภาษา และรักษาบุคลิกของตัวละคร (Persona) ให้ยังคงเดิมหลังจากการแปล

## 3. Dynamic Story Injection (ปรับแต่งเนื้อเรื่องโดยไม่กระทบโครงเรื่องหลัก)
* **AiChronicleGenerator:** ระบบ AI จะสร้างเรื่องราวเหตุการณ์ใหม่ๆ เช่น จดหมายหรือเควสรายวัน โดยอาศัยสถานการณ์ปัจจุบันในเกม
* **Content Patcher Live Builder:** สร้างและคอมไพล์ Mod อย่าง `[CP] Pelican Town Gazette` เพื่อนำเนื้อเรื่องเหล่านี้ใส่เข้าไปในเกมแบบไดนามิก โดยไม่ไปขัดกับแก่นเนื้อเรื่องเดิม

## 4. AI HD Illustration Engine (อัปเกรดรูปภาพให้สวยงาม)
* ระบบจะสร้างภาพประกอบสไตล์ Stardew Concept Art และ HD Pixel Art ในขนาด 512x512 หรือ 1024x1024
* สามารถแปลงภาพและจัดรูปแบบไฟล์ (RGBA PNG) ให้พร้อมใช้งานร่วมกับ Content Patcher ทำให้สามารถยกระดับกราฟิกและภาพอาร์ตเวิร์คภายในเกมได้ทันที

## 5. รองรับระบบ Cloud Drive และ Network ที่หลากหลาย (Multi-Storage)
เพื่อให้ดึงดูดกลุ่มลูกค้าได้มากที่สุด แอปจะไม่ได้จำกัดอยู่แค่ Google Drive แต่จะขยายให้ครอบคลุม:
* **Consumer Cloud:** Google Drive, Microsoft OneDrive, Dropbox
* **Local Network (Zero-Cloud):** Windows Shared Folder (SMB / CIFS) ที่ส่งไฟล์ผ่าน Wi-Fi ได้ไวมาก โดยไม่ต้องใช้เน็ต
* **Power Users / Self-Host:** WebDAV (Nextcloud, Synology NAS), SFTP / SSH (Steam Deck, Linux), และ S3-Compatible Storage (MinIO, S3)

## 6. ระบบตรวจสอบและจัดการ Mod ชั้นสูง (Advanced Mod Manager & Version Checking)
* **Game & Mod Version Validation:** ระบบสามารถตรวจสอบเวอร์ชันของตัวเกมและ Mod (ผ่าน `ModManifestParser`) เพื่อรายงานผลความเข้ากันได้หรือความผิดปกติได้อย่างแม่นยำ ป้องกันปัญหาเกมแครชจากการลง Mod ผิดเวอร์ชัน
* **Mod Manager UI:** เพิ่มหน้าต่าง UI (พัฒนาด้วย Jetpack Compose) สำหรับใช้เลือกติดตั้ง เปิด/ปิด หรือปรับแต่ง Mod ที่ใช้งานง่ายเพียงไม่กี่คลิก
* **Multi-Game Architecture:** วางโครงสร้างแอปให้สามารถนำสถาปัตยกรรมการเช็คเวอร์ชันและการซิงค์นี้ ไปรองรับเกมทำฟาร์มอื่นๆ ที่มีรูปแบบการใช้ไฟล์เซฟและ Mod คล้ายคลึงกันได้ในอนาคต (เช่น Terraria หรือเกมอื่นๆ)

## 7. โครงสร้างรองรับ Multi-Game (Cross-Game Support Architecture)
* **Game Profile Engine:** ระบบถูกออกแบบเป็นสถาปัตยกรรมแบบ Multi-Game Profile โดยมีโมดูลกลางที่รองรับทั้ง Stardew Valley และเปิดกว้างสำหรับเกมอื่นๆ ในอนาคต (เช่น Terraria หรือ Minecraft)
* **Dynamic Configuration Menu:** มีหน้าจอ (UI) สำหรับให้ผู้เล่นกรอกและตั้งค่าพาธไฟล์เซฟ หรือข้อมูลเฉพาะของเกมแต่ละเกมที่ต้องการซิงค์ได้ด้วยตนเอง

## 8. ระบบเชิงพาณิชย์และ Google Play Store (Commercial & Distribution)
* **Google Play In-App Billing:** เชื่อมต่อกับระบบชำระเงินของ Google (PlayBillingManager) สำหรับการปลดล็อกฟีเจอร์ระดับ Pro (Pro Unlock) เพื่อให้แอปพลิเคชันพร้อมสำหรับการวางจำหน่ายจริง
* **Onboarding & Compliance:** มีหน้าสอนการตั้งค่า Shizuku (ShizukuOnboardingScreen) และการตั้งค่า API Key แบบทีละขั้นตอน เพื่อให้แอปผ่านข้อกำหนดการเข้าถึงข้อมูลของ Google Play Policy

## 9. ระบบความปลอดภัยและป้องกันข้อมูลสูญหาย (Failsafe & Multi-Layer Verification)
* **Save Collision Matrix (Multi-Layer Verification):** ระบบตรวจสอบไฟล์เซฟแบบหลายชั้นก่อนทำการซิงค์ โดยเปรียบเทียบทั้ง Timestamp และเวลาภายในเกม (In-Game Timeline) เพื่อป้องกันการนำเซฟที่เก่ากว่าไปเขียนทับเซฟที่ใหม่กว่าอย่างเด็ดขาด
* **Rolling Snapshot Backup & Restore:** ระบบสำรองข้อมูลอัตโนมัติแบบ Zip ย้อนหลังหลายสล็อต (Rolling Backup) ทุกครั้งก่อนเริ่มขั้นตอนที่อาจมีความเสี่ยง (เช่น การเขียนทับไฟล์เซฟ หรือการติดตั้ง Mod) พร้อมระบบกู้คืน (Restore) สู่สถานะปกติในคลิกเดียวหากเกิดข้อผิดพลาด
* **Advanced Error Handling & Failsafe:** กลไกการหยุดพักการซิงค์ทันที (Failsafe) พร้อมแจ้งเตือนผู้ใช้งานหากพบว่าไฟล์เสียหาย มีขนาดผิดปกติ หรือระบบเครือข่ายไม่เสถียร เพื่อรับประกันความปลอดภัยของข้อมูลผู้เล่นอย่างสูงสุด

## 10. การปรับปรุงสถาปัตยกรรมระบบเพื่อความยั่งยืน (Clean Architecture & Scalability Overhaul)
* **Clean Architecture & Dependency Injection:** ปรับโครงสร้างโฟลเดอร์ของโปรเจกต์ให้อยู่ในรูปแบบ Clean Architecture (แบ่งแยก Layer ชัดเจน เช่น `core`, `sync`, `ai`, `patcher`, `ui`) และเตรียมโครงสร้างพื้นฐานสำหรับ Dependency Injection (DI) ในชั้น `core` เพื่อลดการยึดติดของโค้ด (Loose Coupling)
* **Scalable & Maintainable:** สถาปัตยกรรมนี้ทำให้โค้ดอ่านง่าย แก้ไขง่าย ทดสอบง่าย และพร้อมรองรับการขยายฟีเจอร์ใหม่ๆ (เช่น การรองรับเกมใหม่, AI Engine ตัวใหม่, หรือระบบคลาวด์เจ้าใหม่) ในอนาคตได้อย่างมีประสิทธิภาพสูงสุด

## 11. โครงสร้างรองรับ Android หลายแบรนด์และระบบปฏิบัติการเฉพาะ (Multi-Brand Android Support)
* **Dynamic Path Resolver:** โครงสร้างการค้นหาและเข้าถึงไฟล์ `/Android/data/` ถูกออกแบบให้ยืดหยุ่น สามารถปรับตัวตามโครงสร้าง File System ที่อาจแตกต่างกันในมือถือแต่ละค่าย (เช่น Xiaomi, Samsung, Google Pixel)
* **OS-Specific Optimizers:** ระบบมีโมดูลย่อยสำหรับจัดการ Background Service และการขอสิทธิ์ Battery No-Restrictions ที่ปรับแต่งมาเฉพาะสำหรับ OS แต่ละแบรนด์ (เช่น `HyperOsOptimizer` สำหรับ Xiaomi / HyperOS, `OneUiOptimizer` สำหรับ Samsung / OneUI) เพื่อให้การทำงานเบื้องหลัง (Auto-Sync) เสถียรที่สุดในทุกอุปกรณ์

## 12. ส่วนต่อขยายสำหรับ PC & Steam Deck (SMAPI Companion Mod)
* **SMAPI Lifecycle Engine (C#):** พัฒนา Mod ฝั่ง PC (`FarmSyncCompanion`) ที่ทำงานสอดคล้องกับ Lifecycle ของเกม เช่น `GameLaunched` (ตรวจสอบเซฟใหม่ตอนเข้าเกม), `Saved` (ซิงค์อัตโนมัติตอนเข้านอน), และ `ReturnedToTitle` (ตรวจเช็กความสมบูรณ์ตอนออกเกม)
* **Asynchronous & Zero-Lag:** กระบวนการซิงค์เซฟบน PC จะทำงานแบบเบื้องหลัง (Asynchronous ผ่าน `Task.Run`) ทันทีที่วันใหม่ในเกมเริ่มขึ้น เพื่อรับประกันว่าหน้าจอเกมจะไม่ค้างหรือกระตุก (100% Zero-Lag)
* **PC-Side Storage Drivers:** ตัว Mod ฝั่ง PC ถูกออกแบบให้รองรับการรับส่งไฟล์ (IStorageDriver) ผ่าน Local SMB (ซิงค์ผ่าน Wi-Fi บ้านโดยตรง), การตรวจจับพาธ OneDrive Local Folder อัตโนมัติ, และการเชื่อมต่อ Google Drive / WebDAV REST API
