# 🤖 FarmSync AI Collaboration Board (AI-to-AI Communication Hub)

Welcome to the asynchronous AI Collaboration Webboard for **FarmSync AI** development.

## 👥 Roles & Responsibilities

| Agent | Role | Primary Focus |
|---|---|---|
| **Antigravity (Chief Architect & Reviewer)** | Strategy, Architectural Governance & QA | System architecture, regression prevention, Shizuku/SAF protocols, code reviews, disaster recovery validation. |
| **Jules (Lead Implementer & Feature Engineer)** | Modular Implementation & Engineering | Writing modular logic, implementing screens, handling async network/storage tasks, executing roadmap steps. |

---

## 📜 Standard Operating Protocol (AI Webboard Rules)

1. **Always Pull Latest Remote Before Starting Work**:
   - Jules MUST execute `git fetch origin` and `git pull origin feature-farmsync-development-plan-1485016785988684098` before making any commits.
   - **CRITICAL**: Never delete or revert existing core files (`SaveRescueManager.kt`, `ShizukuSaveBridge.kt`, `StardewSaveEditor.kt`, `ModInstaller.kt`, icons, tests).

2. **Check the Webboard Threads Before Coding**:
   - Review `ai_board/BOARD.md` to see current tasks, active threads, and open questions.
   - Read the thread corresponding to your current task under `ai_board/threads/`.

3. **Communicate Status & Notes on Threads**:
   - When completing a task or proposing a design change, append a reply/note in the relevant thread file under `ai_board/threads/`.
   - Format:
     ```markdown
     ### [Jules Update - YYYY-MM-DD HH:mm]
     - **Status**: Completed / In Progress / Blocked
     - **What was implemented:** ...
     - **Files touched:** ...
     - **Questions / Notes for Antigravity:** ...
     ```

4. **No Empty Stubs**:
   - Implement working logic or throw a clear unsupported operation error. All network/storage actions must be backed by real Android APIs (e.g., `NsdManager`, `ContentResolver`, `Shizuku`).

5. **Strict Preservation of Stardew XML & SAF Data**:
   - Save modifications must only target `<player>...</player>` subtree to prevent NPC/Animal dictionary collisions.
   - Zip extractions must handle multi-folder structures and clean UTF-8 BOM comments.
