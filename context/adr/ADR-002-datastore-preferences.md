# ADR-002: AndroidX DataStore (Preferences) for App Settings and Bookkeeping

## Status
Accepted

## Date
2026-10-25

## Context
Life Kernel requires persistent storage for scalar user settings (e.g., active accent palette id, default planner view) and non-relational device bookkeeping flags (e.g., last backup timestamp, dirty-since-backup flag, folder tree URI, notification snooze).

Per `DATABASE_SCHEMA.md` Decision DB-15, `ARCHITECTURE.md` Section 1 & Section 14 (Invariant #6), and `code-standards.md`, user records (tasks, tags, notes, time blocks) are strictly stored in Room SQLite. There is no `app_meta` table in SQLite. Settings and device bookkeeping are strictly kept in DataStore to maintain clean separation between portable user data and device-local preferences.

Requirements:
1. **Zero Network / Privacy:** Zero telemetry, no cloud sync, local storage within the application private sandbox directory (`/data/data/com.lifekernel.app/files/datastore/`).
2. **Safe Concurrency & Reactive APIs:** Asynchronous, non-blocking disk I/O on `Dispatchers.IO` using Kotlin Coroutines and `Flow`.
3. **Data Integrity:** Transactional updates preventing partial/corrupt writes; safe fallback on unexpected or missing values.
4. **Minimal Footprint:** No extra permissions, small APK overhead, full R8/ProGuard compatibility.

## Decision
We adopt **AndroidX DataStore Preferences version 1.1.1** (`androidx.datastore:datastore-preferences`).

Key architecture and operational rules:
1. **DataStore Invariant (#6):** DataStore holds only small scalar preferences and bookkeeping flags. User records (tasks, tags, notes) must never be stored here.
2. **Repository Boundary:** All DataStore interactions are encapsulated within `SettingsRepositoryImpl` under `com.lifekernel.app.data.prefs.*`. Neither Compose UI nor Domain logic interacts directly with DataStore APIs or keys.
3. **Domain Purity:** The domain layer defines the `SettingsRepository` contract and domain models (e.g. `DefaultPlannerView`) without importing any DataStore or Android dependencies (enforced by `LayerBoundaryTest`).
4. **Fallback Handling:** Corrupted or unrecognized preference strings (e.g., an invalid palette ID) fall back safely to deterministic defaults (e.g., `AccentPalettes.Default` / Lime) without throwing exceptions.
5. **Thread Safety:** All reads and writes execute off the main thread via injected coroutine dispatchers (`Dispatchers.IO`).

## Consequences
- Clean separation of device settings and bookkeeping from relational user records.
- First-class reactive observation via Kotlin `Flow`.
- Zero network footprint and zero new Android permissions.
- Verified with unit tests using temporary files and Robolectric/in-memory test scopes.
