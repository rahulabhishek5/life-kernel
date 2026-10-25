# Progress Tracker

Update this file after every meaningful implementation change, in the same change as the work.

*Last updated: 2026-10-25*

## Current Phase

- **Milestone M2: Database and Task Data Layer (T-007 and T-008 completed).** Milestone M2 complete; ready for Milestone M3 (Planner task list).

## Current Goal

- Author review and approval of Task T-008; transition to Milestone M3: Task T-009 (App shell and navigation).

## Phase Plan (from PRD v0.1)

| Phase | Contents | Release |
|---|---|---|
| P1 Planner | Drawer navigation, daily planner (tasks + timetable), calendar views, quick capture, settings, accent themes, in-app nudges, backup and restore, optional app lock | v1.0 (MVP) |
| P2 Daily life | Habits and trackers, goals and projects, notes and journal, resources (URLs only) | v1.x |
| P3 Insights | Analytics engine, rule-based insights, charts and heatmaps, AI-readable export | v1.x |
| P4 Finance | Full finance system | Later update |

## Documentation Status

| Document | State |
|---|---|
| `PRD.md` | Approved (v0.1) |
| `ARCHITECTURE.md` | Drafted, reviewed (AQ-06 resolved) |
| `code-standards.md` | Drafted, awaiting review |
| `ui-context.md` | Drafted, tokens verified in T-005 |
| `design-system.md` | Drafted, tokens verified in T-005 |
| `progress-tracker.md` | This file |
| `project-overview.md` | Drafted, awaiting review |
| `ai-workflow-rules.md` | Drafted, awaiting review (also serves as `AGENTS.md`) |
| `DATABASE_SCHEMA.md` | Drafted, awaiting review |
| `PRIVACY_SECURITY.md` | Drafted |
| `ANALYTICS_SPEC.md` | Drafted |
| `TEST_PLAN.md` | Drafted (includes per-ID traceability matrix) |
| `ROADMAP.md` | Drafted |
| `RISK_REGISTER.md` | Drafted (43 risks) |
| `adr/` | ADR-001 (Room) exists; ADR-002 to ADR-016 drafted |
| `DOCUMENTATION_VALIDATION_REPORT.md` | Drafted; lists 14 consistency issues and their fixes |
| `UPDATES_TO_EXISTING_FILES.md` | Apply, then this file can be archived |
| `TEST_PLAN.md` | Not started |
| `adr/` (decision records) | Not started |
| `RISK_REGISTER.md` | Not started |
| `AGENTS.md` | Merged into `ai-workflow-rules.md`; no separate file |
| `IMPLEMENTATION_PLAN.md` | Drafted, awaiting review (T-001 to T-011 in full; later tasks compact) |
| Documentation validation report | Not started |

## Baseline Measurements (T-004)

| Metric | Target | Baseline (M0) | Measurement Method | Device / Environment |
|---|---|---|---|---|
| Release APK size | <= 15-25 MB | **804 KB** (822,581 bytes) | File size of optimized release APK (`ls -lh app-release-unsigned.apk`) | Linux / AGP 9.4.1 / R8 full shrinking |
| Debug APK size | N/A | **11.7 MB** (11,724,282 bytes) | File size of unminified debug APK | Local build output |
| Cold start time | Fast (< 500ms) | Pending physical device test | `adb shell am start-activity -W -n com.lifekernel.app/.MainActivity` | Author phone (T-004 verification) |
| Idle RAM usage | Low (< 50 MB) | Pending physical device test | `adb shell dumpsys meminfo com.lifekernel.app` | Author phone (T-004 verification) |

## Completed

- Brainstorming of advantages, disadvantages, and challenges; scope and phase decisions agreed.
- PRD v0.1 written and approved.
- `ARCHITECTURE.md`, `code-standards.md`, `ui-context.md`, `design-system.md`, `project-overview.md`, and `ai-workflow-rules.md` drafted from the templates (Gradle checks replace the web build check; agent prohibitions merged in).
- Accent palette contrast ratios calculated (not yet checked on a device).
- `DATABASE_SCHEMA.md` drafted (resolves OQ-04: time blocks use floating local time) and `IMPLEMENTATION_PLAN.md` drafted (milestones M0 to M18).
- **T-001 (author):** Wizard project created (Compose Empty Activity, Kotlin 2.2.10, AGP 9.4.1, Gradle 9.8.0, minSdk 26, targetSdk 37, compileSdk 37, application ID `com.lifekernel.app`).
- **T-002:** Build configuration and manifest baseline:
  - Validated SDK versions: `minSdk = 26`, `compileSdk = release(37)`, `targetSdk = 37`.
  - Configured full R8 code and resource shrinking for release build type (`isMinifyEnabled = true`, `isShrinkResources = true`, `proguard-android-optimize.txt`, `app/proguard-rules.pro`).
  - Audited manifests: 0 permissions declared in `AndroidManifest.xml`. Strictly confirmed NO `android.permission.INTERNET` in debug, release, or merged manifests.
  - Set `android:allowBackup="false"` provisionally in `AndroidManifest.xml`.
  - Verified clean builds: `./gradlew assembleDebug`, `./gradlew assembleRelease`, `./gradlew lintDebug`, and `./gradlew testDebugUnitTest` all pass cleanly with 0 errors. Release APK shrunk from 11.7 MB (debug) to 804 KB.
- **T-003:** Package skeleton and core utilities:
  - Created Application class `LifeKernelApp` (registered in `AndroidManifest.xml`) with lazy `AppContainer` manual DI initialization.
  - Implemented manual DI container `AppContainer` and `DefaultAppContainer` in `di/`.
  - Implemented `Clock` abstraction in `core/time/` with `DefaultClock` (UTC) and `FixedClock` (deterministic time control across timezones for testing).
  - Implemented `AppResult` and `AppError` functional outcome wrappers in `core/result/`.
  - Implemented privacy-preserving `AppLogger` in `core/logging/` (no user-content logging in release builds, pluggable `LogSink`).
  - Implemented automated layer-boundary enforcement unit test `LayerBoundaryTest` in `app/src/test/.../architecture/` (resolving **AQ-06**). Verified that seeding a forbidden import causes test failure and clean code passes.
  - All unit tests (`testDebugUnitTest`), debug & release builds (`assembleDebug`, `assembleRelease`), and lint (`lintDebug`) pass cleanly with 0 errors.
- **T-004:** Baseline metrics documented (804 KB release APK recorded in measurements table; ADB commands noted).
- **T-005:** Design tokens and theme:
  - Implemented `PosColors` with AMOLED black (`#000000`), card surface (`#101010`), input surface (`#171717`), typography tokens, outlines (`outlineStrong`, `outlineSubtle`), and state tokens (`stateError`, `stateWarning`, `stateSuccess`, `stateInfo`).
  - Implemented `AccentPalette` registry with 7 palettes (Lime default, Orange, Red, Blue, Purple, Amber, Mono), fallback to Lime for unknown/null IDs, and dynamic token derivations (`accentSubtle` at 16%, `accentPressed` at 80% over surface, and `heat1`..`heat4` heatmap tiers).
  - Implemented `PosTypography` (sans for UI text, mono for dates/stats/data), `PosSpacing` (4dp grid: space1..space12), `PosShapes` (4/6/8dp radii), `PosBorders` (1/1.5/2dp outlines, 3dp offset shadow), and `PosMotion` (100/160/220ms).
  - Implemented `PosTheme` composable with CompositionLocals, reduced motion detection (`LocalReducedMotion`), and Material 3 `ColorScheme` mapping.
  - Replaced wizard template UI in `MainActivity` with `PosTheme`; deleted obsolete template theme files.
  - Verified with 27 unit tests (including `PaletteContrastTest` checking WCAG 2.1 contrast >= 4.5:1 text and >= 3.0:1 UI across all palettes, and `PaletteRegistryTest`), `lintDebug` (0 errors), and successful debug/release builds.
- **T-006:** Core components and debug preview catalog:
  - Implemented reusable warm neo-brutalist component set under `com.lifekernel.app.core.designsystem.components`:
    - `PosButton`: Primary (accent fill, `onAccent` text, 2dp accent border, 3dp accent offset block translating on press, 100ms `motionFast` / instant snap on reduced motion), Secondary (transparent fill, 1.5dp `outlineStrong` border), Text (borderless, accent colored, 48dp minimum touch target), and Destructive (outline styled with `stateError`).
    - `PosCard`: Surface `#101010`, 1.5dp `outlineSubtle` border, 6dp radius; selected variant with 2dp `accent` border and 3dp accent offset shadow; tactile press-translation on click.
    - `PosTextField`: `surfaceVariant` `#171717`, 1.5dp `outlineStrong` border switching to 2dp `accent` on focus and `stateError` on validation error, persistent label above, error icon and message slot.
    - `PosCheckbox`: 24dp visual box inside strict 48dp minimum touch target, animated check draw over 100ms `motionFast` or instant snap under reduced motion, custom state description and role semantics.
    - `PosChip`: 4dp radius, `outlineStrong` border, `accentSubtle` fill with `accent` border and text on selection, 48dp minimum touch target.
    - `PosSegmentedToggle`: 2-3 segment toggle, `Role.Tab` semantics, selected segment in `accent` with `onAccent` text, unselected in `surfaceVariant`.
    - `PosBanner`: Inline message strip for backup nudges and alerts with leading icon, message, optional action button, and 48dp touch-target dismiss button.
    - `PosProgressBar`: Linear progress bar with `surfaceVariant` track and `accent` fill, animated over 160ms `motionStandard` or snapping under reduced motion, clamped 0f..1f.
    - `PosEmptyState` & `PosErrorState`: Empty state with uppercase mono label, guidance text, and call-to-action button; Error state with `stateError` indicator, error message, and retry button.
  - Implemented `ComponentCatalogScreen` under `com.lifekernel.app.core.designsystem.catalog`:
    - Live accent palette selector dynamically switching across all 7 accent palettes (Lime, Orange, Red, Blue, Purple, Warm Amber, Neutral Mono).
    - Live reduced-motion toggle testing animated vs. instantaneous snapping state transitions.
    - Interactive showcase rendering all components across all interactive states (default, pressed, focused, selected, disabled, and error).
  - Wired `MainActivity` to render `ComponentCatalogScreen` for device verification.
  - Automated test suites:
    - Unit tests (`ComponentContractTest` in `app/src/test/...`): 8 test cases verifying 48dp min touch target, checkbox 24dp size token, borders/offset depth tokens, restrained radii, motion duration tokens, reduced motion zero-duration resolution, progress clamping, button variants. (35 total JVM unit tests across project).
    - Instrumented Compose tests (`app/src/androidTest/...`): `TouchTargetSizeTest` (verifying 48dp x 48dp bounding boxes), `CheckboxStateAndSemanticsTest` (verifying `Role.Checkbox`, "Checked"/"Not checked" state descriptions, toggle callbacks, disabled behavior), `SegmentedToggleSelectionTest` (verifying `Role.Tab` and segment selection), and `ReducedMotionTest` (verifying `LocalReducedMotion` handling and 0ms duration resolution).
  - Verification: `./gradlew testDebugUnitTest` (35 tests pass), `./gradlew compileDebugAndroidTestKotlin` (0 errors), `./gradlew lintDebug` (0 errors), `./gradlew assembleDebug` (pass), `./gradlew assembleRelease` (pass, R8 release APK 1.2 MB).
- **T-007:** Room v1 and task DAOs:
  - Added Room runtime, KTX, and compiler dependencies (2.6.1) via Version Catalog (`gradle/libs.versions.toml`).
  - Added KSP plugin (`2.2.10-2.0.2`) with `room.generateKotlin = "true"` and `room.schemaLocation` targeting `schemas/`.
  - Recorded ADR in [context/adr/ADR-001-room-database.md](file:///home/rahul/AndroidStudioProjects/LifeKernel/context/adr/ADR-001-room-database.md) justifying Room local SQLite engine, schema export, and zero network/telemetry footprint.
  - Implemented Room entities under `com.lifekernel.app.data.local.entity`:
    - `TagEntity` (`tags`): `id` (PK auto), `name` (unique `COLLATE NOCASE`), `created_at` (UTC ms).
    - `TaskEntity` (`tasks`): `id` (PK auto), `title` (not null), `description` (nullable), `status` (0=OPEN, 1=DONE), `priority` (0=NONE, 1=LOW, 2=MEDIUM, 3=HIGH resolving **DQ-01 / DB-V6**), `start_date`, `due_date`, `parent_id` (FK `tasks(id)` with `ON DELETE CASCADE`), `sort_order`, `recur_type`, `recur_interval`, `recur_weekdays`, `recur_until`, `created_at`, `updated_at`. Indexes on `(status, due_date)`, `(parent_id)`, `(due_date)`.
    - `TaskCompletionEntity` (`task_completions`): `id` (PK auto), `task_id` (FK `tasks(id)` with `ON DELETE CASCADE`), `occurrence_date` (nullable ISO date), `completed_at` (UTC ms), `completed_on` (local date text). Unique index on `(task_id, occurrence_date)`, indexes on `(completed_on)` and `(task_id)`.
    - `TaskTagEntity` (`task_tags`): Composite PK `(task_id, tag_id)` with cascading foreign keys to `tasks(id)` and `tags(id)`. Index on `(tag_id)`.
  - Implemented `Converters` in `com.lifekernel.app.data.local` for safe ISO date strings and UTC epoch timestamps.
  - Implemented DAOs under `com.lifekernel.app.data.local.dao`: `TagDao`, `TaskDao`, `TaskCompletionDao`, `TaskTagDao` with coroutines and `Flow` queries.
  - Implemented `LifeKernelDatabase` (version 1) with `PRAGMA foreign_keys = ON;` callback, in-memory testing builder, and schema export enabled.
  - Generated and verified committed schema JSON: `app/schemas/com.lifekernel.app.data.local.LifeKernelDatabase/1.json`.
  - Registered lazy `database` singleton in `AppContainer` and `DefaultAppContainer`.
  - Automated test suites:
    - Unit tests: `ConvertersTest` (6 tests for ISO date and epoch round-trip/fallbacks), `CheckConstraintVerificationTest` (2 tests resolving **DB-V2**), `LayerBoundaryTest`, and theme tests. Total: **43 passing JVM unit tests**.
    - Instrumented tests: `RoomDaoTest` (CRUD, queries, orphan deletion, Flow observation), `ForeignKeyCascadeTest` (resolving **DB-V1**: proves cascade deletion of subtasks, completions, tag links, and rejection of invalid foreign keys), and `UniqueConstraintTest` (proves case-insensitive tag uniqueness and task completion occurrence uniqueness).
  - Verification: `./gradlew testDebugUnitTest` (43 tests pass), `./gradlew compileDebugAndroidTestKotlin` (0 errors), `./gradlew lintDebug` (0 errors), `./gradlew assembleDebug` (pass), `./gradlew assembleRelease` (pass, R8 release APK 1.2 MB).

- **T-008:** Task domain, repository, and use cases:
  - Implemented pure Kotlin domain models under `com.lifekernel.app.domain.model`:
    - `Task`: Aggregate root domain model per `DATABASE_SCHEMA.md` §6.2 with `isSubtask` and `isCompleted` properties.
    - `Tag`: Label domain model per §6.1.
    - `TaskPriority`: Enum resolving DQ-01 / DB-V6 (NONE=0, LOW=1, MEDIUM=2, HIGH=3).
    - `TaskStatus`: Enum (OPEN=0, DONE=1).
    - `TaskRecurrence`: Recurrence configuration data class and `RecurrenceType` enum (NONE=0, DAILY=1, WEEKLY=2, MONTHLY=3).
    - `TaskCompletion`: Domain completion model preserving exact local date text `completedOn` alongside UTC instant `completedAt`.
    - `TaskValidator`: Domain validation limits per §2.4 & §6.2 (title 1–200 chars, description <= 10,000 chars, tag 1–40 chars, subtask depth limit 1, subtasks cannot recur, recurring requires due date).
  - Implemented domain repository contracts under `com.lifekernel.app.domain.repository`:
    - `TaskRepository`: CRUD operations, reactive observation `Flow`s (overdue + today, today, no due date, completed today, counts, task detail by ID, subtasks), and atomic completion/reopening contracts.
    - `TagRepository`: CRUD, case-insensitive retrieval, reactive `Flow` observation, and orphan tag cleanup contract.
  - Implemented domain use cases under `com.lifekernel.app.domain.usecase`:
    - `CreateTaskUseCase`: Validates task limits and creates task with injected `Clock` timestamps.
    - `CompleteTaskUseCase`: Atomically records completion and marks task DONE.
    - `ReopenTaskUseCase`: Atomically removes completion and reverts task to OPEN.
    - `DeleteTaskUseCase`: Deletes task with cascading subtask/completion cleanup and orphan tag removal.
    - `UpdateTaskUseCase`: Validates and updates task with refreshed `updatedAt`.
    - `ObservePlannerTasksUseCase`: Observes reactive `PlannerTasksSnapshot` (overdue, today, completed today, and counts).
  - Implemented data mappers under `com.lifekernel.app.data.repository.mappers`:
    - `TaskMapper`: Bidirectional mapping between `TaskEntity` and `Task`.
    - `TagMapper`: Bidirectional mapping between `TagEntity` and `Tag`.
    - `TaskCompletionMapper`: Bidirectional mapping between `TaskCompletionEntity` and `TaskCompletion`.
    - Strictly encapsulated Room entities within `data/` package (zero Room/Android types leaked upward).
  - Implemented repositories under `com.lifekernel.app.data.repository`:
    - `TaskRepositoryImpl`: Backed by `LifeKernelDatabase`, `Clock`, and `Dispatchers.IO`. Uses `database.withTransaction` for multi-step mutations: atomic completion (with `completed_at` UTC ms and `completed_on` local date snapshot, reject double-complete), atomic reopening (delete completion, reset status=0, reject re-opening open tasks), atomic task/subtask deletion with orphan tag cleanup, and tag synchronization.
    - `TagRepositoryImpl`: Backed by `LifeKernelDatabase`, `Clock`, and `Dispatchers.IO`.
  - Wired repositories in manual DI container `AppContainer` and `DefaultAppContainer` (`val taskRepository`, `val tagRepository`).
  - Automated test suites:
    - Unit tests: `TaskValidatorTest` (9 tests for title, description, tag name bounds, subtask depth-1, and recurrence rules), `TaskUseCasesTest` (7 tests using fake repository and `FixedClock`), `LayerBoundaryTest` (3 tests verifying zero platform imports in `domain/`). Total: **59 passing JVM unit tests** (100% success rate).
    - Repository integration tests: `TaskRepositoryIntegrationTest` (in-memory Room + `FixedClock`: atomic completion/reopen transactions, double-complete rejection, already-open rejection, cascading deletion with orphan tag cleanup, `completed_on` timezone stability, empty database defaults, reactive Flow re-emission) and `TagRepositoryIntegrationTest`.
  - Verification: `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` (59 tests pass), `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug` (pass), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleRelease` (pass, R8 release APK 1.2 MB).

## In Progress

- Milestone M2 (Database and Task Data Layer) completed (T-007 and T-008 verified). Ready for author review before advancing to Milestone M3 (Planner task list).

## Next Up

1. Author review and approval of Milestone M2 (T-008).
2. Milestone M3: Task T-009 (App shell and navigation).
3. Milestone M3: Task T-010 (Planner — tasks view).

## Open Questions

| ID | Question | Needed by |
|---|---|---|
| OQ-01 | Final app name and permanent application ID | Before first signed build |
| OQ-02 | Number of backups retained (N); nudge snooze period | Design phase |
| OQ-03 | Final priority levels and default deadline window on the planner | Design phase |
| ~~OQ-04 / AQ-04~~ | Resolved: floating local time, block belongs to start date, may pass midnight, DST ignored for blocks (DB-04 to DB-06) | Done |
| OQ-09 | How Android's automatic cloud backup (`allowBackup`) is handled; set provisionally to false in T-002 | `PRIVACY_SECURITY.md` |
| DB-V1 to DB-V8 | Schema verification tasks (foreign keys enforced, no CHECK support, migration validation, search speed, restore pragma behavior, backup file extension, large-history performance) | See `DATABASE_SCHEMA.md` section 15 |
| ~~DB-V1~~ | Resolved: Foreign key cascades and invalid reference rejections verified via `ForeignKeyCascadeTest` | Done in T-007 |
| ~~DB-V2~~ | Resolved: Verified Room entity annotations do not provide CHECK constraints via `CheckConstraintVerificationTest` | Done in T-007 |
| ~~DB-V6 / DQ-01~~ | Resolved: Priority levels 0..3 (NONE=0, LOW=1, MEDIUM=2, HIGH=3) implemented in `TaskEntity` | Done in T-007 |
| OQ-05 / AQ-03 | Backup password-encryption scheme and parameters | `PRIVACY_SECURITY.md` |
| OQ-06 / AQ-05 | Charting approach: native drawing or a small library | Before P3 |
| OQ-07 | Single opt-in daily notification after real use? | After P1 dogfooding |
| OQ-08 | Play Store requirements for a new developer account | Before any Play Store plan |
| AQ-01 | Confirm `InvalidationTracker`-based dirty flag works as intended on a real device | P1 backup task |
| AQ-02 | App lock mechanism: biometric library or platform-only credential | `PRIVACY_SECURITY.md` |
| ~~AQ-06~~ | Resolved: Automated `LayerBoundaryTest` enforces domain purity (forbids `android.*`, `androidx.*`) | Done in T-003 |
| ~~DQ-01~~ | Resolved: Priority levels 0..3 (NONE, LOW, MEDIUM, HIGH) agreed and implemented in `TaskEntity` | Done in T-007 |
| DQ-03 | Categorical chart colors | Start of P3 |
| DQ-04 | Resource icon set | Start of P2 |
| ~~DQ-05~~ | Resolved: Pure AMOLED black (`#000000`) confirmed on physical device in M1 | Done |


## Architecture Decisions

| ID | Decision | Why |
|---|---|---|
| D-01 | Native Kotlin and Jetpack Compose (Flutter rejected) | Smallest size, best platform fit, fewest layers; Flutter's notification plugin was not a strong enough reason to add an engine |
| D-02 | Minimum Android 8.0 (API 26) | Wide device coverage and native `java.time`, so no extra date library |
| D-03 | Plain Room in app-private storage, no database encryption in v1 (ADR-001) | Keeps size and key-recovery complexity down; schema leaves room to add encryption later; schema JSON committed to git |
| D-04 | Optional app lock, labeled as a lock | Honest security wording; avoids implying encryption |
| D-05 | Optional password-encrypted backup | Protects exported files, the realistic leak path, without custom crypto |
| D-06 | No system notifications or reminders in v1; in-app banners and Now/Next strip | Removes permission prompts, channels, scheduling, and boot handling |
| D-07 | Backup nudge computed on app open (more than 5 days and data changed) | No background work; nudge only when there is something new to protect |
| D-08 | Resources are URLs only | No file-path breakage, no document permissions |
| D-09 | AI-readable export is separate from backup | Backup is exact and restorable; export is readable and lossy |
| D-10 | Hamburger drawer navigation; opens on the planner, task view by default | User preference; planner is the product's core |
| D-11 | Personal use, then friends via signed APK, then possibly Play Store | Validate with real use before store work |
| D-12 | Finance deferred to P4, after analytics and charts | Largest correctness risk; depends on everything else; added later via migration |
| D-13 | Manual DI, single Gradle module | Fewer dependencies and less build overhead for one developer |
| D-14 | No background work (no WorkManager, services, alarms) | Matches privacy and lightweight principles |
| D-15 | Time blocks use floating local time (date, start minute, duration), not instants | A block at 18:00 stays at 18:00 when the zone changes; avoids DST corruption of durations |
| D-16 | Backup is a logical export (ZIP of per-table JSON Lines plus manifest), restored by replace-all inside one transaction after full validation | Independent of SQLite internals; atomic; old backups readable through upgraders |
| D-17 | No soft deletion; no activity-log table (activity derived); trackers merged into habits | Fewer concepts and no duplicated state |
| ADR-001 | AndroidX Room 2.6.1 + KSP with committed schema exports and explicit foreign keys | Fast local SQLite queries, compile-time SQL verification, zero network footprint, pure domain layer boundary |
| D-18 | Backup retention 5; nudge snooze 24 hours | Roughly 25 days of history at a 5-day cadence; one nudge per day at most |
| D-19 | Priorities 0 to 3 with symbol plus text marks | Small fixed set; accessible |
| D-20 | Android automatic backup disabled (ADR-014) | Keeps data local by default |
| D-21 | Encrypted backups: PBKDF2-HMAC-SHA256 plus AES-256-GCM, platform crypto only (ADR-009) | No custom crypto; no new dependency |
| D-22 | App lock via system biometric or device credential, 30 s grace (ADR-009) | Avoids custom PIN code |
| D-23 | Notes get `created_on`; goal milestones get `completed_on` (local-date snapshots) | Analytics stay stable across time-zone changes |
| D-24 | AI export written via system "save as" only | No file-provider component |
| D-25 | Native Canvas charts proposed (ADR-015) | Smaller size; full styling control |
| D-26 | kotlinx.serialization proposed for backup JSON (ADR-016) | Typed, testable parsing; needs approval before T-017 |
### Decision log (replaces separate ADR files; ADR-001 Room is in adr/)

| ID | Decision | Why | Status |
|---|---|---|---|
| ADR-002 | Native Kotlin and Jetpack Compose | Smallest size, fewest layers, platform fit | Accepted |
| ADR-003 | Offline-only v1; no INTERNET permission | Easiest guarantee to audit | Accepted |
| ADR-004 | No AI in the app; rule-based insights; user-triggered AI export | Testable, private | Accepted |
| ADR-005 | Hamburger drawer; planner-first; Tasks/Timetable toggle inside the planner | Fits 10 destinations; instant switching | Accepted |
| ADR-006 | AMOLED black with token-based accent palettes | New palettes need no feature changes | Accepted |
| ADR-007 | Money as integer minor units (P4) | Exact arithmetic; balances derived, never stored | Accepted |
| ADR-008 | Backup: ZIP of per-table JSON Lines plus manifest; validate fully, then replace-all in one transaction | Atomic, versioned, independent of SQLite internals | Accepted |
| ADR-009 | Plain database; optional app lock via system prompt; optional encrypted backup (PBKDF2-HMAC-SHA256 + AES-256-GCM, 32 MiB ceiling) | Covers realistic leak paths without custom crypto | Accepted |
| ADR-010 | No notifications or background work; backup nudge on app open (5 days, 24 h snooze) | No permissions or scheduling | Accepted |
| ADR-011 | Dates as TEXT, instants as UTC ms, time blocks as floating local time, local-date snapshots (`completed_on`, `created_on`) | Stable across time-zone changes | Accepted |
| ADR-012 | Manual DI, single Gradle module | Fewer dependencies for one developer | Accepted |
| ADR-013 | Resources are URLs only | No file-path or permission problems | Accepted |
| ADR-014 | Android automatic backup disabled | Data leaves the device only by user action | Accepted |
| ADR-015 | Native Canvas charts, no charting library | Smaller size, full styling control | Proposed: confirm at P3 start |
| ADR-016 | kotlinx.serialization for backup JSON | Typed, testable parsing | Proposed: approve before T-017 |
| (pending) | AndroidX biometric library for app lock | Avoids custom PIN code | Needs approval before T-033 |

## Session Notes

- The author is a student with a part-time job and is new to Kotlin; AI-written code must be understood before acceptance, so keep milestones small and explained.
- The app is Android-only; the AI-readable export is for pasting into external AI tools by the user's choice and is not an in-app AI feature.
- Docs refer to the architecture file as `ARCHITECTURE.md` (uppercase); keep the casing consistent.
- All color hex values and contrast numbers in the design files were calculated, not measured on a device. Review on a real AMOLED screen at the first UI milestone.
- Toolchain and baseline versions verified in T-001/T-002: Kotlin 2.2.10, AGP 9.4.1, Gradle 9.8.0, JBR/JVM 25.0.3, Android SDK 37 (API 26 minSdk, API 37 target/compileSdk), Compose BOM 2026.02.01, build-tools 36.0.0. Application ID / namespace: `com.lifekernel.app`.
- Repository folder is `LifeKernel`; context files live in `context/` (older documents say `docs/`). The app name and application ID are not yet confirmed (OQ-01).
- IDE and agent: Antigravity IDE with a Gemini agent. Gradle is run with `JAVA_HOME=/opt/android-studio/jbr ./gradlew ...`; keep using that exact form.
- The agent edited `gradle.properties` and `build.gradle.kts` during M1/M2. Build-configuration changes must be explained in each task summary and reviewed.
- Task approval is the author's decision. Record evidence (commands run, test results, device checks) here before marking a task done.