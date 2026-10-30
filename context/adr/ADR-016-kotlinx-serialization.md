# ADR-016: kotlinx.serialization for Backup Manifest, Portable Settings, and Table JSONL

## Status
Accepted

## Date
2026-10-30

## Context
Milestone M5 introduces on-demand backup archive creation and atomic restoration (`FR-BKP-001`, `FR-BKP-002`, `ADR-008`). The backup archive format (`DATABASE_SCHEMA.md` §11) consists of:
1. `manifest.json`: Root metadata containing app version, backup format version, Room schema version, creation timestamp (UTC ms), device zone ID, and file table array with row counts and SHA-256 hashes.
2. `settings.json`: Portable user preferences exported from DataStore (e.g., active palette ID, default planner view).
3. `data/<table>.jsonl`: Streaming JSON Lines records per database table (`tasks.jsonl`, `tags.jsonl`, `task_tags.jsonl`, `task_completions.jsonl`).

Requirements:
1. **Zero Network / Privacy:** Zero telemetry, no network calls, strictly local serialization and deserialization.
2. **Type Safety & Schema Integrity:** Strict parsing, validation of schema versions, and typed model handling.
3. **Pure Kotlin Compatibility:** Seamless JVM unit testing and Android runtime execution without Android platform stubs (unlike `org.json` which stubs out on host JVM without Robolectric).
4. **Lightweight Footprint:** Compatible with R8 shrinking, adding negligible APK size overhead.

## Decision
We adopt **`kotlinx.serialization` (JSON module `1.6.3`)** integrated via the official Kotlin serialization compiler plugin (`org.jetbrains.kotlin.plugin.serialization`).

Key architectural rules:
1. **Scope:** `kotlinx.serialization` is used exclusively in infrastructure/backup layers (`infra/backup/`) for manifest, settings, and table JSON streaming.
2. **Pure Domain Boundary:** Serialization annotations (`@Serializable`, `@SerialName`) must never leak into pure domain entities (`domain/model/`). Backup DTOs and serializers are encapsulated within `infra/backup/model/` and `infra/backup/serializer/`.
3. **Streaming & Memory Efficiency:** Records are streamed to `ZipOutputStream` using buffered line writers, avoiding holding entire table datasets in memory.
4. **Zero Networking:** The library operates in pure JSON string/stream parsing mode with zero network or external dependencies.

## Consequences
- Fast, reflection-free, compile-time generated serializers for backup manifest and table entries.
- Manifest and settings JSON structures can be validated and round-trip tested cleanly in JVM unit tests.
- Preserves domain purity invariant (Invariant #1) and data encapsulation invariant (Invariant #2).
- Zero external network traffic or analytics footprint (Invariant #3).
