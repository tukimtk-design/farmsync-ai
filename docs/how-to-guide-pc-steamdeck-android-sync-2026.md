# [GUIDE] How to Seamlessly Sync Your Stardew Valley Farm Between PC / Steam Deck & Android in 2026 (Without Cable Hassle & Zero Save Loss)

> **Applies to:** Windows 10/11 PC, Steam Deck (SteamOS), ROG Ally, and Android 14/15 (HyperOS, OneUI, Pixel)  
> **Tools used:** **FarmSync AI** (Android) + **FarmSync Companion** (PC/Steam Deck SMAPI Mod) or Google Drive / OneDrive / Local Wi-Fi SMB

---

## 🌟 The Problem: Why Playing Stardew Valley Across PC & Mobile Used to be Painful
You build an amazing 100-hour farm on your PC, and you want to continue playing while commuting or lying in bed with your Android phone. But:
1. **Android 14/15 Scoped Storage** blocks access to `/Android/data/com.zane.stardewvalley/`
2. Manually transferring files via USB cable every time is exhausting.
3. Generic sync tools don't understand Stardew save files and can accidentally overwrite your Year 3 Summer save with an old Year 1 backup!

Here is the complete **2026 Seamless Cross-Save Setup** that fixes everything with 1-click automatic background sync!

---

## 🚀 Part 1: Android Setup (Xiaomi 14T Pro / Any Android 14+)

### Step 1: Grant Fast Scoped Storage Access via Shizuku (No Root Required)
1. Install **Shizuku** from Google Play Store.
2. Go to Android **Settings > Developer Options > Enable Wireless Debugging**.
3. Tap **Pair device with pairing code** in Shizuku and start the Shizuku service.

### Step 2: Configure FarmSync AI
1. Open **FarmSync AI** on your phone.
2. Grant Shizuku permission when prompted (allows instant access to Stardew Valley save files).
3. Select your preferred sync destination:
   - **Option A (Easiest):** Google Drive / Microsoft OneDrive.
   - **Option B (Fastest & 100% Offline):** Local Wi-Fi SMB (Syncs directly to your PC at 1 Gbps!).
4. Enable **Auto-Sync on Game Exit** (Under Settings > HyperOS Optimization > Allow Background Autostart).

---

## 🎮 Part 2: PC & Steam Deck Setup

### Option A: Using the Free `FarmSync Companion` SMAPI Mod (Recommended)
1. Download **`FarmSync Companion`** from [Nexus Mods](https://www.nexusmods.com).
2. Extract the mod into your Stardew Valley `Mods/` folder.
3. Open `config.json` (or use Generic Mod Config Menu in-game) to choose your sync provider (OneDrive, Google Drive, or Local SMB).
4. **How it works:** Whenever your character goes to sleep and the game saves, FarmSync Companion automatically and silently pushes the latest save snapshot to the cloud/local share in the background!

### Option B: Built-in Windows 11 OneDrive Sync (Zero Mods Needed)
If you play on vanilla PC without SMAPI:
1. Your PC save lives at: `%APPDATA%\StardewValley\Saves`
2. Point your OneDrive / Google Drive desktop client to mirror this folder.
3. In the FarmSync AI Android app, select the same OneDrive / Google Drive folder. Done!

---

## 🛡️ Part 3: How the "Save Collision Matrix" Protects Your Farm

Unlike generic file sync tools, FarmSync AI inspects the **in-game timeline XML** before touching any file:
* **Timeline Check:** It reads `Farmer Name`, `Current Season`, `Day of Month`, `Year`, and `Total Money`.
* **Zero Accidental Overwrite:** If you played to *Fall Day 12* on your phone, the PC will NEVER overwrite it with an older *Fall Day 5* save, even if your PC clock was messed up.
* **Rolling 10-Slot Snapshots:** The system keeps zip backups of your last 10 save states locally. If anything ever goes wrong, restore your farm in 1 tap!

---

## 📦 Part 4: Bonus — 1-Click Modding & AI Translation on Mobile

If you use mods (like *Stardew Valley Expanded, UI Info Suite*, or Portrait mods):
1. **1-Click Zip Installer:** Download any `.zip` mod from Nexus Mods on your phone and tap **Install via FarmSync AI** — it extracts directly into the game's `files/Mods/` folder.
2. **AI Mod Auto-Translator:** Got an English mod you want in Thai? Tap **Translate with AI (BYOK)** to automatically translate all NPC dialogues while keeping character personalities (Sebastian, Abigail, Linus) and game formatting tags intact!

---

## 💬 Community Discussion & Links
* 🔗 **Android App:** [FarmSync AI on Google Play](https://play.google.com/store/apps/details?id=com.tukimtk.farmsync)
* 🔗 **PC SMAPI Mod:** [FarmSync Companion on Nexus Mods](https://github.com/tukimtk-design/farmsync-ai)
* 🔗 **Open-Source GitHub:** [tukimtk-design/farmsync-ai](https://github.com/tukimtk-design/farmsync-ai)

*Got questions or need help setting up on Steam Deck? Leave a comment below!*
