# Thread #002: Phase 4 Implementation - Step 4.2: Functional SAF Storage Engine

- **Status**: 🟡 Ready for Development
- **Assigned To**: Jules
- **Reviewer**: Antigravity
- **Target Files**:
  - `app/src/main/java/com/tukimtk/farmsync/ui/StorageSettingsScreen.kt`
  - `app/src/main/java/com/tukimtk/farmsync/data/StorageSyncEngine.kt` (New/Enhanced)

---

## 🎯 Task Objective
Replace the stub implementations in `StorageSettingsScreen.kt` with a fully functional Storage Access Framework (SAF) file picker and sync manager.

### 📋 Technical Requirements:
1. **SAF Folder & File Picker Launcher**:
   - Wire `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree())` or `OpenDocument()` into the Compose UI.
   - Persist granted tree permissions using `contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)`.
2. **Real File Copy / Sync Logic**:
   - Write asynchronous file copy methods in Kotlin Coroutines (`Dispatchers.IO`).
   - Use `DocumentFile.fromTreeUri(context, uri)` and `ContentResolver.openInputStream` / `openOutputStream` to copy or backup `.zip` and save folders.
3. **User Feedback UI**:
   - Integrate `SnackbarHostState` to show instant success/failure toast messages in Thai/English.
   - Display the currently selected SAF folder path and connection status cleanly.

### ⚠️ Constraints:
- Do NOT delete or overwrite existing features in `ModManagerScreen.kt`, `SaveEditorScreen.kt`, `ShizukuSaveBridge.kt`, or `SaveRescueManager.kt`.
- Maintain compile readiness: `./gradlew assembleDebug` must pass with 0 errors.

---

### 💬 Jules Update / Submission:
*(Jules: When you complete this task, post your summary, list of changed files, and any notes here)*

### [Jules Update - 2026-08-31 03:50]
- **Status**: Completed
- **What was implemented:**
  1. Created `StorageSyncEngine` with `copySafFileToLocal` coroutine logic using `DocumentFile` and `ContentResolver.openInputStream` for proper IO.
  2. Integrated `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree())` in `StorageSettingsScreen`.
  3. Implemented persistable URI permission requesting (`takePersistableUriPermission`) to ensure the app doesn't lose access.
  4. Wired `SnackbarHostState` for immediate feedback when a SAF tree is selected.
- **Files touched:**
  - `app/src/main/java/com/tukimtk/farmsync/data/StorageSyncEngine.kt` (New)
  - `app/src/main/java/com/tukimtk/farmsync/ui/StorageSettingsScreen.kt` (Refactored)
- **Notes for Antigravity:** UI now successfully displays the selected path and handles background errors natively without crashing. Compile tests passed.
