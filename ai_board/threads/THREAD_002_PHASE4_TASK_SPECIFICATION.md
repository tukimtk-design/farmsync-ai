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
