# ADR-001: Room Persistence Library for Offline Local Storage

## Status
Accepted

## Date
2026-10-25

## Context
Life Kernel (Life kernel) is an offline-first, private personal command center. It requires relational storage with foreign key constraints, cascading deletes, index support, and reactive query capabilities for core entities including tasks, completions, tags, and time blocks.

Requirements:
1. **Zero Network / Privacy:** Zero telemetry, no cloud synchronization backends, strictly local SQLite file in app-private sandboxed storage.
2. **Compile-Time Safety:** Strong compile-time validation of SQLite queries, schema definitions, and migration paths.
3. **Reactive Observation:** First-class Kotlin Coroutines `Flow` streaming for auto-updating UI.
4. **Tooling & Stability:** Long-term maintenance by Google AndroidX, compatibility with Android minSdk 26 and targetSdk 37, and integration with Kotlin 2.2 / KSP.

## Decision
We adopt **AndroidX Room version 2.6.1** via KSP (`2.2.10-2.0.2`) and the official `androidx.room` Gradle plugin.

Key configuration rules:
1. **Schema Export:** Enabled via `room { schemaDirectory("$projectDir/schemas") }`. Schema JSON files are committed to git to ensure exact schema tracking across milestones and prevent silent migration drift.
2. **Foreign Keys:** Explicitly enforced via `PRAGMA foreign_keys = ON;` in database configuration callbacks.
3. **No Destructive Migration:** Destructive migration fallbacks are strictly prohibited (`fallbackToDestructiveMigration` is never called). All schema changes across milestone versions will use explicit `Migration` implementations with unit and instrumentation tests.
4. **Data Isolation:** Room entities, DAOs, and the database class reside strictly within `com.lifekernel.app.data.local.*` and are never exposed to presentation or domain layers.

## Consequences
- Compile-time verification of all SQL queries at build time with KSP.
- Native SQLite database stored locally in private application sandbox (`/data/data/com.lifekernel.app/databases/lifekernel.db`).
- Zero external network traffic or analytics footprint.
- Fast execution with full R8 code shrinking and resource optimization compatibility.
