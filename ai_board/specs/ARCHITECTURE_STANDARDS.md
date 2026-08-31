# 🏛️ FarmSync AI Architecture & Code Quality Standards

## 1. Storage & SAF (Storage Access Framework) Rules
- On Android 11+ (API 30+) and Android 14 (API 34), direct `File("/storage/emulated/0/Android/data/...")` read/write is strictly blocked by the OS kernel.
- Always use `ShizukuSaveBridge` (or SAF `DocumentFile.fromTreeUri`) to perform operations inside `/Android/data/`.
- Never catch `SecurityException` silently without falling back or reporting failure to user.

## 2. Stardew Valley XML Integrity
- **DO NOT** use global regex replacement across the whole XML save.
- `<name>` tags are shared by `Farmer`, `NPC`, `FarmAnimal`, and items.
- Only edit within `<player>...</player>` tags to avoid breaking Stardew dictionary keys.
- Always update both `SaveGameInfo` and the main save XML file together to ensure Stardew's Load Game menu displays the updated save slot.

## 3. SMAPI Mod Manifest Parsing
- SMAPI `manifest.json` files often contain UTF-8 BOM characters (`\uFEFF`) and C-style comments (`//`, `/* */`).
- Standard `JSONObject(text)` will crash on un-sanitized manifests. Always strip BOM and regex comment patterns before parsing JSON.

## 4. UI & State Management
- Use Jetpack Compose Material 3 components.
- State must reactively update using `mutableStateOf` and Compose `DisposableEffect` / `LifecycleEventObserver`.
- Do not use hardcoded English strings directly in Composable without offering Thai fallback via `Strings.get(th, en)`.
