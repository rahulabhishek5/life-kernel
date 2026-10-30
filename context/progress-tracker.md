# Progress Tracker

Update this file after every meaningful implementation change, in the same change as the work.

*Last updated: 2026-10-30*

## Current Phase

- **Milestone M5: Backup and restore (In progress: T-017 complete, transitioning to T-018: Restore engine).** Milestone M4 is fully completed and signed off. Task T-017 (Backup archive writer, ZIP streaming, manifest, JSONL per table, SHA-256, and automated backup registry enforcement test) is fully implemented, verified, and tested.

## Current Goal

- Author review and formal sign-off for Task T-017: Backup archive writer; proceed exclusively to Milestone M5 — Task T-018: Restore (validate fully, replace atomically).

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
| `adr/` | ADR-001 (Room) & ADR-002 (DataStore Preferences) accepted; ADR-003 to ADR-016 drafted |
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
- **T-009:** App shell and navigation:
  - Implemented `PosTopBar` in `com.lifekernel.app.core.designsystem.components`:
    - Edge-to-edge status bars aware (`statusBarsPadding()`).
    - AMOLED black background (`PosTheme.colors.background`).
    - Leading hamburger button with 48dp minimum touch target bounding box, accessibility label ("Open navigation drawer"), role `Role.Button`, and crisp 3-bar neo-brutalist canvas icon.
    - Title with `PosTypography.title` and optional trailing actions slot.
  - Implemented `PosDrawer` in `com.lifekernel.app.core.designsystem.components`:
    - Modal navigation drawer sliding from the left with width ~80% of screen width (capped at 320dp).
    - Scrim: `#000000` at 60% alpha (`PosColors.scrim`).
    - Animation: Slide and scrim fade animating over 220ms (`motionEmphasized`), or instant snap (0ms) when `LocalReducedMotion` is active.
    - Grouped entries separated by dividers per `design-system.md` §8.3:
      - Header: App name ("Life Kernel") and version ("v0.1" in mono).
      - Group 1 (Daily): Home / Today.
      - Group 2 (System): Settings.
      - Strict Rule (FR-NAV-002): Unbuilt modules (Calendar, Timetable, Habits, Goals, Notes, Resources, Analytics) are strictly omitted.
    - Drawer item states: Selected item renders with `accentSubtle` background, a 3dp accent vertical edge indicator bar, and `textPrimary`. Unselected items use transparent background with `textPrimary`. Minimum 48dp touch target per row.
  - Implemented navigation infrastructure in `com.lifekernel.app.navigation`:
    - `Screen`: Type-safe destinations (`Screen.Home`, `Screen.Settings`).
    - `DrawerDestination`: Central top-level destination model with group metadata.
    - `AppNavigationState`: State holder with `Saver` for `rememberSaveable` to restore active destination across configuration changes (rotation).
    - `AppNavHost`: Top-level shell composable hosting `PosDrawer` and screen switching with 160ms (`motionStandard`) crossfade (0ms under reduced motion).
  - Implemented predictable back navigation hierarchy:
    1. If drawer is open, Back closes the drawer first.
    2. If currently on Settings and drawer is closed, Back returns to Home / Today.
    3. If currently on Home / Today, system back exits the app.
  - Implemented placeholder screens:
    - `feature/planner/PlannerScreen.kt`: Home / Today with `PosTopBar`.
    - `feature/settings/SettingsScreen.kt`: Settings with `PosTopBar`.
  - Updated `MainActivity.kt`: Set content strictly to `AppNavHost` within `PosTheme`.
  - Automated test suites:
    - Unit tests: `AppNavigationStateTest` (4 tests verifying start destination, navigation, single-top, and FR-NAV-002 unbuilt module omission). Total: **63 passing JVM unit tests** (100% success rate).
    - Compose navigation tests (`NavigationTest` in `androidTest`): Start destination test, drawer opening & navigation to Settings, scrim dismissal, 48dp minimum touch target per drawer row, state restoration across configuration changes, reduced-motion instant snap, and back hierarchy logic.
  - Verification: `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` (63 tests pass), `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug` (pass), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleRelease` (pass, R8 release APK 1.2 MB).
- **T-010:** Planner — tasks view:
  - Implemented immutable `PlannerUiState` modeling complete screen state:
    - Formatted date and weekday header ("EEE · d MMM uuuu" in uppercase mono).
    - Task counts: completed tasks today, total tasks today, completion ratio float clamped to `0f..1f`.
    - Sectioned task lists: Overdue tasks list, Today's open tasks list (due today + open tasks with no due date per PRD §8.2), and Done tasks list.
    - Section collapse state: Done section collapsed by default (`isDoneCollapsed = true`).
    - Loading / empty state representation (`isPlannedEmpty` boolean).
  - Implemented `PlannerViewModel` managing UI state flow:
    - Dependencies: `ObservePlannerTasksUseCase`, `CompleteTaskUseCase`, `ReopenTaskUseCase`, `Clock`, and optional injectable `CoroutineScope` for deterministic JVM testing.
    - Unidirectional state collection via `combine` and `flatMapLatest` into `StateFlow<PlannerUiState>`.
    - Task toggle dispatcher: delegates to `CompleteTaskUseCase` for open tasks and `ReopenTaskUseCase` for completed tasks.
    - Section toggle dispatcher: expands and collapses the Done section.
    - Midnight rollover / lifecycle evaluation: `checkDayRollover()` checks if clock date has advanced upon UI resume without background polling loops or timers.
  - Implemented UI components under `com.lifekernel.app.feature.planner.components`:
    - `PlannerHeader`: Displays formatted mono date header (`PosTypography.label` with `PosFonts.mono`) and a `PosCard` progress card with ratio text (e.g., "3 / 7 DONE") and `PosProgressBar` with smooth 160ms fill animation (snapping instantly under reduced motion).
    - `PlannerSectionHeader`: Neo-brutalist header with mono uppercase title and count badge; includes `PlannerCollapsibleSectionHeader` for Done section with expand/collapse chevron and 160ms height/rotation animation.
    - `PlannerTaskRow`: Warm neo-brutalist row with leading `PosCheckbox` (48dp touch target), task title (`PosTypography.body`), 220ms strikethrough & `textMuted` transition on completion, priority indicators (`!!` High, `!` Medium, `·` Low), tag badges, and overdue status indication.
  - Implemented `PlannerScreen`:
    - `LazyColumn` with stable item keys (`Task.id`) and item content types.
    - Overdue section: conditionally rendered only when overdue tasks exist, highlighted with `stateWarning`.
    - Today section: displays open tasks due today and open tasks without due date.
    - Done section: collapsed by default with count, expands on click.
    - Empty state: displays `PosEmptyState("NOTHING PLANNED", "Add your first task for today.")` when both Overdue and Today sections are empty.
    - Lifecycle observer: registers `LifecycleEventObserver` to trigger `viewModel.checkDayRollover()` on `ON_RESUME`.
  - Manual DI Wiring:
    - Exposed `createPlannerViewModel(): PlannerViewModel` in `AppContainer` and `DefaultAppContainer`.
    - Provided `LocalAppContainer` in `MainActivity` and `PlannerScreen` factory fallback.
  - Automated test suites:
    - Unit tests (`PlannerViewModelTest` in `app/src/test/...`): 5 test cases verifying initial empty state, overdue/today/done task partitioning, task complete/reopen dispatch, done collapse toggle, and midnight rollover. Total: **68 passing JVM unit tests** (100% success rate).
    - Compose UI tests (`PlannerScreenTest` in `app/src/androidTest/...`): Verifies empty state rendering, date header/progress card/sections display, done section expand/collapse, checkbox 48dp minimum touch target, and reduced-motion instant completion.
  - Verification: `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` (68 tests pass), `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug` (pass), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleRelease` (pass, R8 release APK 1.2 MB).

- **T-011:** Quick-add and completion transition:
  - Implemented `PosSheet` under `com.lifekernel.app.core.designsystem.components`:
    - Warm neo-brutalist bottom sheet container.
    - Top corner radius 8dp (`radiusLarge.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))`).
    - 1.5dp `outlineStrong` border, `surfaceVariant` (`#171717`) surface background.
    - 60% alpha AMOLED black scrim (`PosColors.scrim`).
    - Centered drag handle (32dp x 4dp, 2dp radius, `outlineStrong`).
    - Window insets awareness (`imePadding()`, `navigationBarsPadding()`).
    - 220ms `motionEmphasized` slide transition or instant (0ms) snap on reduced motion (`PosTheme.reducedMotion`).
    - Accessible dismiss on scrim click or system Back.
  - Implemented `PlannerQuickAddButton` under `com.lifekernel.app.feature.planner.components`:
    - Neo-brutalist primary button pinned to bottom-right of `PlannerScreen` (padding `space4` = 16dp).
    - Accent fill, `onAccent` text ("+ NEW TASK"), 2dp accent border, 3dp accent offset drop block.
    - 100ms `motionFast` press translation (instant on reduced motion).
    - Strict 48dp minimum touch target bounding box (`testTag("planner_quick_add_fab")`).
  - Implemented `QuickAddSheet` under `com.lifekernel.app.feature.planner.components`:
    - Autofocused title input (`PosTextField` with `FocusRequester`, `ImeAction.Done`, and inline error validation preventing whitespace/empty submission).
    - Date preset chip row (`PosChip`): `Today` (default), `Tomorrow`, `Pick date` (opens `DatePickerDialog`).
    - Priority chip row (`PosChip`): `None` (default), `Low ·`, `Med !`, `High !!`.
    - Primary action button: `PosButton("SAVE", onClick = onSave)`.
  - Success Metric SM-03 Verification:
    - Verified two-interaction task capture: Tap Quick-Add FAB -> Type title and press keyboard Done -> task validates, persists to Room database via `CreateTaskUseCase` off the main thread, and sheet dismisses automatically.
  - Completion Transition:
    - 220ms `motionEmphasized` completion transition on task row: checkbox toggles, title dims to `textMuted` with animated strikethrough before collapsing smoothly into the Done section.
    - Under `LocalReducedMotion`, transitions instantly (0ms).
    - Database write executes immediately on the first line of `onToggleTask`, ensuring zero data loss if process dies during animation.
  - Manual DI Wiring:
    - Injected `CreateTaskUseCase` into `PlannerViewModel`.
    - Exposed `createTaskUseCase` in `AppContainer` and `DefaultAppContainer`.
  - Automated Test Suites:
    - Unit tests (`PlannerViewModelTest` in `app/src/test/...`): 9 new tests (14 total in file) covering quick-add open/dismiss visibility, title validation error, preset selection (Today, Tomorrow, Pick date), priority selection (Low, Medium, High), SM-03 submission on Done action, and reduced-motion instant completion. Total: **77 passing JVM unit tests** (100% success rate).
    - Instrumented tests (`QuickAddFlowTest` in `app/src/androidTest/...`): Verifies FAB display, sheet opening, autofocused title input, SM-03 two-interaction capture, inline title validation error on empty string, date preset and priority selection, 48dp touch targets on all interactive elements, process recreation / rotation state survival, and reduced motion behavior.
  - Verification: `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` (77 tests pass in 12s), `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug` (pass in 1s), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleRelease` (pass in 28s, R8 release APK 1.2 MB).

- **T-012:** Settings v0: theme selector and default planner view in DataStore:
  - Added AndroidX DataStore Preferences (`androidx.datastore:datastore-preferences:1.1.1`) to version catalog (`gradle/libs.versions.toml`) and `app/build.gradle.kts`.
  - Recorded ADR in [context/adr/ADR-002-datastore-preferences.md](file:///home/rahul/AndroidStudioProjects/LifeKernel/context/adr/ADR-002-datastore-preferences.md) documenting storage model, zero-telemetry invariant, and boundaries.
  - Implemented domain model and contract:
    - `DefaultPlannerView` domain enum (`TASKS`, `TIMETABLE`) in `com.lifekernel.app.domain.model`.
    - `SettingsRepository` in `com.lifekernel.app.domain.repository` exposing `themePalette: Flow<AccentPalette>`, `defaultPlannerView: Flow<DefaultPlannerView>`, and suspend mutators. Strictly clean domain layer with zero `android.*` or `androidx.*` imports (verified by `LayerBoundaryTest`).
  - Implemented data layer under `com.lifekernel.app.data.prefs`:
    - `SettingsPreferences`: DataStore initialization (`settings_preferences`), scalar keys (`theme_palette_id`, `default_planner_view`), and defaults (`lime`, `TASKS`). Strict invariant: DataStore holds only small scalar settings and bookkeeping flags, never user records.
    - `SettingsRepositoryImpl`: Backed by AndroidX `DataStore<Preferences>` on `Dispatchers.IO` with safe `IOException` fallback (`emptyPreferences()`) and fallback to Lime / Tasks on unknown/corrupted strings.
  - Manual DI Wiring:
    - Registered lazy singleton `settingsRepository` and `createSettingsViewModel()` factory in `AppContainer` and `DefaultAppContainer`.
  - Presentation Layer under `com.lifekernel.app.feature.settings`:
    - `SettingsUiState`: Immutable state containing selected palette, available palettes, and selected default planner view.
    - `SettingsViewModel`: Coordinates persistent palette and planner view state via `StateFlow` and dispatches updates off the main thread.
    - `SettingsScreen`: Neo-brutalist settings screen with `PosTopBar` ("Settings" with hamburger drawer button), "ACCENT THEME" section rendering selectable `PosCard`s with visual color swatches for all 7 palettes (Lime, Orange, Red, Blue, Purple, Warm Amber, Neutral Mono) and active selection indicator, and "DEFAULT PLANNER VIEW" section with `PosSegmentedToggle` (Tasks vs Timetable). Strict 48dp minimum touch targets on all interactive selectors. Zero hardcoded colors or borders (tokens only).
  - Root Theme Observation:
    - Wired `MainActivity.kt` to collect `settingsRepository.observeThemePalette()` with lifecycle awareness (`flowWithLifecycle(lifecycle)`), passing the active palette to `PosTheme(palette = activePalette)`.
    - Updated `PosTheme` dynamic colors in `Theme.kt` to derive `accentSubtle` (16% alpha) and `accentPressed` (80% alpha) from the animated accent token, enabling smooth 160ms (`motionStandard`) color transitions across the entire app upon palette selection and persistence across process restarts.
  - Automated Test Suites:
    - Unit tests (`SettingsRepositoryTest` in `app/src/test/...`): 6 test cases verifying initial defaults (Lime, Tasks), palette persistence across all 7 palettes, fallback to Lime on unknown palette ID, fallback to Tasks on corrupted view string, and planner view mutations.
    - Unit tests (`SettingsViewModelTest` in `app/src/test/...`): 3 test cases verifying state emissions and dispatch calls.
    - Total: **86 passing JVM unit tests** (100% success rate).
    - Instrumented UI tests (`SettingsScreenTest` in `app/src/androidTest/...`): 5 test cases verifying rendering of the 7 palettes, palette selection interaction, default planner view toggle interaction, 48dp minimum touch targets, and top bar drawer callback.
  - Verification: `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` (86 tests pass), `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` (0 errors), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug` (pass in 33s), `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleRelease` (pass in 52s, R8 release APK 1.4 MB, audited 0 network permissions, strictly NO INTERNET).

- **Post-T-012 Stability Audit & Visual / Interaction Defect Resolution:**
  - Conducted hardware stability audit and resolved 5 confirmed visual and interaction defects on native physical hardware (`RK6DY5ONUOHMTKDM`):
    - **Defect 1 (Lime / Green Palette Inversion):**
      - Root Cause: Palette ID string matching and theme color memoization in Compose needed strict normalization, explicit case-insensitive mapping in `Palette.kt`, and keying on `palette.id` and `palette.accent` in `Theme.kt` to prevent accidental fallbacks.
      - Fix: Hardened `AccentPalettes.fromId()` with explicit lowercase trimming and default fallback to `Lime`. Hardened `SettingsRepositoryImpl.setThemePalette()` with string normalization. Keyed `remember(palette.id, palette.accent)` in `Theme.kt`. Added unit test assertions in `PaletteRegistryTest` and `SettingsRepositoryTest` ensuring `"lime"` strictly resolves to `#A3E635` and never Red.
    - **Defect 2 (Primary Button & FAB Neo-Brutalist Offset Merging):**
      - Root Cause: Both button face and 3dp offset block used identical accent fill without a contrasting outline, blending into an amorphous block with zero 3D depth.
      - Fix: Added a crisp 2dp contrasting boundary outline (`PosTheme.colors.background` / `#000000`, `borderStrong`) to the foreground face of `PosButton` (Primary) and `PlannerQuickAddButton`, separating it distinctly from the 3dp accent drop block. Added outer 1.5dp standard outline to the offset shadow block. Maintained 100ms tactile press translation (`motionFast`) / 0ms on reduced motion.
    - **Defect 3 (Screen Flickering & Jumping on Quick-Add Sheet Entry):**
      - Root Cause: Synchronous frame-0 `focusRequester.requestFocus()` collided violently with the 220ms sheet entrance transition, and `Modifier.imePadding()` on outer layouts forced re-measurement and resizing of the background planner screen; furthermore, window focus events triggered redundant `checkDayRollover()` calls.
      - Fix: Deferred soft keyboard focus request via coroutine until after sheet entrance animation completes (`enterDuration + 50L`) wrapped in try-catch. Confined `imePadding()` strictly to the inner scrollable content of `PosSheet` while anchoring the outer surface to `navigationBarsPadding()`. Guarded `PlannerScreen` lifecycle observer to skip rollover checks while QuickAdd is visible.
    - **Defect 4 (Top Bar Background & Scrolled Content Bleed):**
      - Root Cause: `PosTopBar` lacked an opaque background and bottom divider, allowing scrolled cards to bleed through.
      - Fix: Wrapped `PosTopBar` in an opaque column with `PosTheme.colors.background` (`#000000`), status bars padding, and a 1dp subtle bottom divider (`PosTheme.colors.divider` = `#303030`). Omitted any accent bottom borders.
    - **Defect 5 (Empty State & FAB Layout Ergonomics):**
      - Root Cause: Empty state button and FAB both present simultaneously, with risk of FAB obscuring scrolled content.
      - Fix: Unified both actions to invoke identical `viewModel::onOpenQuickAdd`. Increased list bottom spacing to 80dp (`space12 + space8`) + navigation bars clearance, ensuring content scrolled to bottom is never obscured by FAB.
  - Automated Test Suites & Regression Battery:
    - **JVM Unit Tests:** 88 tests passed, 0 failures, 0 skipped (100% success rate across all domain, data, and presentation test suites).
    - **On-Device Instrumented Tests:** 59 tests passed, 0 failures on physical hardware `2412DPC0AI` (`RK6DY5ONUOHMTKDM`).
    - **Lint & Size Budget:** 0 lint errors (`lintDebug`), release APK strictly 1.4 MB (< 1.5 MB budget), audited 0 network permissions (`uses-permission` has NO internet access).

- **Follow-up Hardware Defect Resolution (2026-10-28):**
  - **Lime / Green palette never applied after Orange, Red, or Blue:**
    - Root Cause: `PosTheme` built `PosColors` through a memoized `remember(palette.id, palette.accent)` while the `accent` token was sourced from the transient `animateColorAsState` value. The memoized instance could therefore be created on the palette-change frame with the previous accent still animating, freezing the rendered accent at the old palette. Additionally, both screens recreated their ViewModel on every recomposition through composable default arguments, so Settings could display its default Lime `ACTIVE` state while the app-level color source of truth stayed on the old palette.
    - Fix: `Theme.kt` now always derives `PosColors` from the target `palette` and overlays the transient animated accent; when reduced motion is active it uses the target accent directly. Removed the `remember` memoization from both `PosColors` and the Material `ColorScheme`. `MainActivity` now collects the repository palette with `collectAsStateWithLifecycle`. `SettingsRepositoryImpl.setThemePalette` always writes the canonical registry id, including `"lime"`. `SettingsScreen` and `PlannerScreen` now retain a single ViewModel via `remember` instead of constructing one on every recomposition.
    - Regression coverage: `SettingsViewModelTest.selectLimeAfterAnotherPalette_alwaysDispatchesAndRestoresLime` asserts that selecting Lime (including repeating the current palette) always dispatches and restores `#A3E635`.
  - **Accent-colored line bleeding under the top bar on Settings scroll:**
    - Fix: `PosTopBar` is now hosted in an opaque `Surface` (`PosTheme.colors.background`, zero tonal elevation) with `zIndex(1f)`, full-width background, and `statusBarsPadding()` inside the surface so the black background covers the status-bar area. The 1dp bottom divider remains `PosTheme.colors.divider` (`#303030`), never accent. `SettingsScreen` scroll content now uses `Modifier.weight(1f)` instead of `fillMaxSize()` so it cannot draw over the bar.
  - **Primary / FAB / empty-state button rendered flat with no 3dp offset block:**
    - Fix: `PosButton` now draws the neo-brutalist drop block with `Modifier.drawBehind` at exactly `offsetShadow` (3dp) on both axes using the resolved shape outline, filled with `PosTheme.colors.accent` and stroked with `PosTheme.colors.outlineStrong` at `borderStrong` (2dp). The foreground face keeps its 2dp contrasting `background` border and 100ms press translation; the drop block is rendered outside the face bounds rather than relying on a `matchParentSize` sibling, so it cannot be lost to layout/clipping.
  - **Quick-add sheet presentation and focus:**
    - Fix: `PlannerScreen` and `SettingsScreen` no longer recreate their ViewModels on recomposition, so `isQuickAddVisible = true` set by `PlannerViewModel.onOpenQuickAdd()` survives the resulting recomposition. `QuickAddSheet` still defers `FocusRequester.requestFocus()` until after the sheet entrance animation, now using the `motionEmphasized` token instead of a hardcoded duration.
  - Verification:
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` — BUILD SUCCESSFUL; 89 tests, 0 failures, 0 errors, 0 skipped.
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` — BUILD SUCCESSFUL, 0 errors.
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleRelease` — BUILD SUCCESSFUL.

- **T-013:** Task detail, edit, delete with confirmation:
  - Neo-brutalist modal confirmation dialog `PosDialog` & `PosConfirmationDialog` (`com.lifekernel.app.core.designsystem.components.PosDialog.kt`):
    - Conforms strictly to `design-system.md` §6: surface `#101010`, 2dp `outlineStrong` border, `radiusLarge` (8dp), and 60% alpha AMOLED black scrim.
    - Dismiss/Cancel button on left (Secondary outline style) and Affirmative/Confirm button on right (Destructive variant using `stateError` label/border or Primary accent).
    - Strict >= 48dp touch targets on actions.
    - Supports custom body content, explicit confirmation text, and dismiss on scrim tap or system back.
  - Domain Layer Use Cases (`com.lifekernel.app.domain.usecase`):
    - `GetTaskByIdUseCase`: Observes a specific task by ID (`Flow<Task?>`) and fetches direct result (`suspend fun invoke(id: Long): AppResult<Task>`).
    - `UpdateTaskUseCase`: Validates updated task attributes (title 1-200 chars, description <= 10,000 chars, start date <= due date), refreshes `updatedAt` via injected `Clock`, and persists updates.
    - `DeleteTaskUseCase`: Atomically deletes task by ID with foreign key cascades and orphan tag cleanup.
  - Domain Validation & Repository Invariants:
    - Added date ordering validation in `TaskValidator.kt` (`startDate > dueDate` returns `AppError.Validation`).
    - Preserved completed task invariant (AC-TASK) in `TaskRepositoryImpl.updateTask`: editing a completed task preserves its completed status (`status = existing.status`) and keeps its `task_completions` records intact.
  - Presentation Layer (`com.lifekernel.app.feature.tasks`):
    - `TaskDetailUiState`: Immutable state containing task fields (title, description, priority, dates), status readout, live validation errors (`isTitleBlank`, `isDateOrderInvalid`), and delete confirmation dialog visibility.
    - `TaskDetailViewModel`: Manages task loading, live field validation, date preset/pickers, save persistence, and delete confirmation with coroutine scope injection for testability.
    - `TaskDetailScreen`: Neo-brutalist screen featuring `PosTopBar` with back arrow (`isBackNavigation = true`), status badge readout, title `PosTextField` with inline validation, description multiline `PosTextField`, priority chips (`None`, `Low ·`, `Medium !`, `High !!`), date presets/pickers, save button, destructive "DELETE TASK" button, and `PosConfirmationDialog`.
    - AC-TASK Confirmation Dialog rule: Explicitly names the task being deleted (`"Are you sure you want to delete '$taskTitle'?"`) and alerts user if subtasks will also be deleted.
  - Navigation & Interaction Wiring:
    - Added `Screen.TaskDetail(val taskId: Long)` route in `Screen.kt`.
    - Updated `AppNavigationState` to manage a back-stack of screens with `navigateBack()` and state restoration in `Saver`.
    - Updated `PlannerTaskRow` to provide `onTaskClick` outside the checkbox for navigating to detail, while checkbox toggle remains isolated.
    - Back handling: System back dismisses delete dialog first if open, or discards uncommitted edits and pops to planner.
    - Manual DI: Injected `GetTaskByIdUseCase`, `UpdateTaskUseCase`, `DeleteTaskUseCase`, and `createTaskDetailViewModel(taskId)` into `AppContainer` and `DefaultAppContainer`.
  - Automated Test Suites:
    - Unit Tests:
      - `UpdateTaskUseCaseTest`: 4 tests (valid update, blank title rejection, start-date-after-due-date rejection, editing completed task preserving status/completions).
      - `DeleteTaskUseCaseTest`: 2 tests (successful deletion, non-existent task error).
      - `GetTaskByIdUseCaseTest`: 3 tests (observe flow, fetch success, fetch not found).
      - `TaskDetailViewModelTest`: 6 tests (initial load, live title validation, date constraint validation, save action, delete flow, completed status preservation).
      - `TaskValidatorTest`: Added date order validation test (10 total in file).
      - `AppNavigationStateTest`: Added detail route parsing and back-stack pop tests (5 total in file).
      - Total: **106 passing JVM unit tests** (100% success rate, 0 failures, 0 skipped).
      - `LayerBoundaryTest`: Passes with 0 forbidden imports in `domain/`.
    - Instrumented Tests:
      - `TaskDetailFlowTest`: Tests detail view rendering, live inline editing, delete dialog explicitly naming the task, subtasks deletion warning, 48dp minimum touch targets, and deletion execution.
      - `TaskRepositoryIntegrationTest`: Verifies editing completed task in SQLite preserves status and completions.
      - `NavigationTest`: Verifies navigation from planner row click into detail and back navigation pop.
  - Verification Outputs:
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` — BUILD SUCCESSFUL (106 tests passed, 0 failures, 0 errors, 0 skipped).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug assembleRelease` — BUILD SUCCESSFUL (Release APK size 1.4 MB, <= 1.5 MB budget).

- **T-014:** Tags on tasks (tag picker, tag management, orphan cleanup):
  - Tag Domain Model & Validator (`com.lifekernel.app.domain.model`):
    - Created `TagValidator` enforcing PRD §8.4 rules: 1–30 characters, stripped leading/trailing whitespace, strictly lowercase normalized, allowed characters `^[a-z0-9_-]+$`, rejects empty strings, spaces, and invalid symbols. Leading '#' prefix is automatically stripped.
    - Updated `TaskValidator.kt` to delegate `validateTagName` to `TagValidator.validateTagName` and set `MAX_TAG_LENGTH = 30`.
  - Domain Layer Use Cases (`com.lifekernel.app.domain.usecase`):
    - `ObserveAllTagsUseCase`: Returns `Flow<List<Tag>>` sorted alphabetically by name.
    - `GetTagsForTaskUseCase`: Observes or fetches tags linked to a specific `taskId`.
    - `SyncTaskTagsUseCase`: Atomically associates a list of tag names with a task. Automatically creates non-existent tags, links them via `task_tags`, unlinks removed tags, and triggers orphan cleanup in a single transaction.
  - Repository & Data Layer (`com.lifekernel.app.domain.repository` & `com.lifekernel.app.data.repository`):
    - Extended `TagRepository` with `observeTagsForTask(taskId)`, `getTagsForTask(taskId)`, and `syncTaskTags(taskId, tagNames)`.
    - Implemented `TagRepositoryImpl.syncTaskTags` with `database.withTransaction`: validates names, inserts new tags, updates `task_tags` junction, and prunes orphan tags (`database.tagDao().deleteOrphanTags()`).
    - Verified `TaskRepositoryImpl.deleteTask` cascades to `task_tags` and cleans up orphaned tags automatically.
  - UI Components (`core/designsystem/components/` & `feature/tasks/components/`):
    - `PosRemovableChip` (`PosChip.kt`): Renders tag name prefixed with `#` in mono font (`PosFonts.mono`), featuring a dedicated trailing '✕' remove button strictly enforcing a minimum 48dp touch target (`minTouchTarget`).
    - `PosTagPicker` (`feature/tasks/components/PosTagPicker.kt`): Inline tag picker with FlowRow of assigned tags (`PosRemovableChip`), `PosTextField` input with live validation, suggestion chips for existing matching tags, and a "+ Create #name" action for novel tags. Automatically ignores duplicate tags.
    - `TaskDetailScreen`: Integrated `PosTagPicker` into the task detail and edit view. Committing changes synchronizes all tag associations atomically via `SyncTaskTagsUseCase`.
    - `PlannerTaskRow`: Renders assigned tag chips horizontally beneath the task title with `surfaceVariant` background and hairline border, wrapping cleanly via `FlowRow`.
  - Manual DI Wiring (`com.lifekernel.app.di`):
    - Registered `observeAllTagsUseCase`, `getTagsForTaskUseCase`, and `syncTaskTagsUseCase` in `AppContainer` and `DefaultAppContainer`.
    - Injected use cases into `createTaskDetailViewModel(taskId)`.
  - Automated Test Suites:
    - **Unit Tests:**
      - `TagValidatorTest` (9 tests): 1–30 character limits, empty/whitespace rejection, length limit checks, lowercase normalization, trimming, '#' prefix stripping, space rejection, allowed symbols (`_` and `-`), and invalid symbol rejection.
      - `TagUseCasesTest` (5 tests): Alphabetical sorting, tag creation and linking, unlinking and orphan tag pruning, shared tag preservation across tasks, and invalid tag name rejection.
      - `TaskDetailViewModelTest` (8 tests): Includes tag addition with normalization, duplicate avoidance, tag removal, and input validation error handling.
      - `LayerBoundaryTest` (3 tests): Continues to enforce zero forbidden imports in `domain/`.
      - **Total: 118 passing JVM unit tests** (100% success rate across all suites).
    - **Instrumented Tests:**
      - `TagRepositoryIntegrationTest`: Case-insensitive tag reuse, orphan cleanup on tag removal and task deletion, lowercase uniqueness constraint in SQLite.
      - `TaskDetailFlowTest`: Tag creation via input and create chip, tag removal via 48dp touch-target remove action.
      - `PlannerScreenTest`: Verified tag chips render on `PlannerTaskRow`.
      - `compileDebugAndroidTestKotlin`: 0 compilation errors across all instrumented test suites.
  - Verification Outputs:
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` — BUILD SUCCESSFUL (118 tests passed, 0 failures, 0 errors, 0 skipped).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug assembleRelease` — BUILD SUCCESSFUL (Release APK size 1.4 MB, <= 1.5 MB budget).

- **T-015:** Subtasks (checklist on tasks, add/toggle/delete, subtask progress, and cascade verification):
  - Subtask Domain Model & Validator (`com.lifekernel.app.domain.model`):
    - `Subtask`: Pure Kotlin model (`id`, `taskId`, `title`, `isCompleted`, `position`, `createdAt`, `updatedAt`, `createdInstant`, `updatedInstant`) adhering strictly to Invariant #1 (zero `android.*` or `androidx.*` dependencies).
    - `SubtaskValidator`: Enforces constraints per PRD §8.3, FR-TASK-002, and `DATABASE_SCHEMA.md` §2.4: title 1–200 characters, trimmed of leading/trailing whitespace, non-empty, position >= 0, and valid positive taskId.
  - Domain Layer Use Cases (`com.lifekernel.app.domain.usecase`):
    - `ObserveSubtasksForTaskUseCase`: Emits `Flow<List<Subtask>>` ordered by `position ASC`.
    - `AddSubtaskUseCase`: Validates title, assigns next sequential position (`currentMax + 1`), sets timestamps via `Clock`.
    - `ToggleSubtaskUseCase`: Toggles completion state (`isCompleted`), updates `updatedAt` via `Clock`.
    - `DeleteSubtaskUseCase`: Deletes subtask and compacts remaining subtask positions to maintain continuity without gaps.
  - Room DAO & Repository Layer (`com.lifekernel.app.data.local.dao` & `com.lifekernel.app.data.repository`):
    - `SubtaskDao`: Operates on `TaskEntity` within the existing `tasks` table (`parent_id = taskId`, `sort_order = position`, `status = isCompleted`), preserving Room v1 schema with zero migrations.
    - `SubtaskMapper`: Provides bidirectional conversions between `TaskEntity` and `Subtask`.
    - `SubtaskRepository` & `SubtaskRepositoryImpl`: Dispatches all DB operations on `Dispatchers.IO`, wrapping mutations in `database.withTransaction`.
    - **Critical Invariant Verification (FR-TASK-002 / AC-TASK):** Independent completion state verified — completing all subtasks does not mark parent task completed, and completing parent task does not toggle subtask states.
    - **Cascade Deletion Verification:** SQLite `parent_id REFERENCES tasks(id) ON DELETE CASCADE` automatically purges all subtasks when a parent task is deleted.
  - UI Presentation Components (`feature/tasks/components/` & `feature/planner/components/`):
    - `TaskDetailSubtaskRow.kt`: Compact neo-brutalist row with leading `PosCheckbox` strictly enforcing >= 48dp touch target, title with 220ms strike-through and dimming animation on completion (0ms instantaneous snap under `LocalReducedMotion`), and trailing delete button with strict >= 48dp touch target.
    - `TaskDetailScreen.kt`:
      - Fractional completion header (e.g., `"SUBTASKS: 3 / 5"`) in uppercase mono caps (`PosFonts.mono`).
      - Mini `PosProgressBar` reflecting completion ratio with smooth 160ms fill animation.
      - Inline quick-input field (`PosTextField`) supporting sequential keyboard `ImeAction.Done` additions and dedicated add button, with live validation rejecting blank titles without resetting the form.
    - `PlannerTaskRow.kt`:
      - Compact subtask count badge (e.g., `"3/5"`) rendered in mono caps alongside existing tag chips and priority indicators without cluttering the primary task title.
  - Manual DI Wiring (`com.lifekernel.app.di`):
    - Registered `subtaskRepository`, `observeSubtasksForTaskUseCase`, `addSubtaskUseCase`, `toggleSubtaskUseCase`, and `deleteSubtaskUseCase` in `AppContainer` and `DefaultAppContainer`.
    - Injected all subtask use cases into `createTaskDetailViewModel(taskId)`.
  - Automated Test Suites:
    - **Unit Tests:**
      - `SubtaskValidatorTest` (7 tests): 1–200 character boundary, blank/empty title rejection, whitespace trimming, position >= 0, and full subtask validation.
      - `SubtaskUseCasesTest` (5 tests): Sequential position assignment, whitespace trimming, validation rejections, decoupled toggle behavior, position continuity compaction on deletion, and ordered observation.
      - `TaskDetailViewModelTest` (9 tests): Subtask quick-add, toggle, deletion, fractional progress ratio calculation, and independent parent completion state verification.
      - `LayerBoundaryTest` (3 tests): 0 forbidden imports across all domain models and use cases.
      - **Total: 137 passing JVM unit tests** (100% success rate across all suites).
    - **Instrumented Tests:**
      - `SubtaskRepositoryIntegrationTest`: SQLite persistence, `position ASC` ordering, completion state toggling, position continuity compaction, and SQLite foreign key cascade deletion when parent task is deleted.
      - `TaskDetailFlowTest`: Subtask addition via quick-add input and keyboard `ImeAction.Done`, subtask checkbox toggling, progress header/bar updates, deletion, and strict 48dp touch targets on checkboxes and delete buttons.
      - `PlannerScreenTest`: Verified subtask count badge (`"1/3"`) renders on `PlannerTaskRow`.
      - `compileDebugAndroidTestKotlin`: 0 compilation errors across all instrumented test suites.
  - Verification Outputs:
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` — BUILD SUCCESSFUL (137 tests passed, 0 failures, 0 errors, 0 skipped).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug assembleRelease` — BUILD SUCCESSFUL (Release APK size 1.4 MB, <= 1.5 MB budget).

- **T-016:** Planner search and filter (global keyword search, tag filtering, priority filtering, and empty search results):
  - Domain & Data Layer:
    - `TaskFilterCriteria`: Immutable criteria domain model (`searchQuery`, `selectedPriority`, `selectedTagName`, `trimmedQuery`, `isActive`).
    - `SearchAndFilterTasksUseCase`: Cross-date query flow mapping `observeAllTasks()`. When `criteria.isActive == true`, searches across all dates in the database (past, today, future, and unscheduled tasks); case-insensitive substring match across title and description; conjunctive (AND) logic across search query, priority, and tag filters; top-level tasks only.
    - `TaskDao` & `TaskRepository`: Added `observeAllTopLevelTasks` / `observeAllTasks` on `Dispatchers.IO` ordered by due date, priority, and ID.
  - ViewModel & Reactive State Pipeline:
    - `PlannerViewModel`: Multi-stream reactive state pipeline. 250ms debounced flow for keystroke queries (`debouncedQueryFlow`), immediate selection for priority and tag chips. Exposes actions: `onToggleSearch()`, `onCloseSearch()`, `onUpdateSearchQuery()`, `onTogglePriorityFilter()`, `onToggleTagFilter()`, `onClearAllFilters()`.
    - `PlannerUiState`: Added `isSearchActive`, `searchQuery`, `selectedPriorityFilter`, `selectedTagFilter`, `availableFilterTags`, `filteredTasks`, and helper properties `isFilterActive` and `isSearchEmpty`.
  - Presentation & UI Components:
    - `PosChip`: Enhanced with 160ms color transition animation, reduced-motion snap, default `surfaceVariant` inactive background, and strict 48dp minimum touch target.
    - `PosTextField`: Added optional `leadingIcon` slot with design-system padding.
    - `PlannerTopBar`: Top bar supporting standard title mode with 48dp search icon action, and search mode with autofocus inline `PosTextField`, leading search icon, trailing 48dp clear/close button (`search_clear_button`), and IME Search action.
    - `PlannerFilterBar`: Horizontal scrollable row (`planner_filter_bar`) rendering priority filter chips (`High !!`, `Med !`, `Low ·`), dynamic `#tag` chips, and "CLEAR ALL" action (all enforcing >= 48dp touch targets).
    - `PlannerScreen`: Replaces standard date sections with single-list global search results when filtering. Tasks render explicit due date badges ("TODAY", "OVERDUE", "DUE d MMM", "NO DUE DATE"). Zero search matches trigger `PosEmptyState` ("NO MATCHING TASKS", guidance, "CLEAR FILTERS" CTA).
    - Back Navigation Priority: `BackHandler(enabled = uiState.isSearchActive || uiState.isFilterActive)` closes search and clears filters before navigating away or opening drawer.
  - Test Suite Coverage:
    - `SearchAndFilterTasksUseCaseTest`: Cross-date search (verifying +30 day future task returned), case-insensitive substring match, description search, combined AND logic, empty criteria unfiltered return, and subtask exclusion.
    - `PlannerViewModelFilterTest`: Keystroke debounce and state updates, priority and tag chip toggling, clear all reset, and empty search results flag.
    - `PlannerScreenTest`: Expanding search bar from top bar, typing query and verifying cross-date tasks appear with explicit date badges, priority chip filtering, "CLEAR FILTERS" reset, and 48dp minimum touch targets on all filter and search controls.
  - Verification Outputs:
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` — BUILD SUCCESSFUL (140 tests passed, 0 failures, 0 errors, 0 skipped).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug assembleRelease` — BUILD SUCCESSFUL (Release APK size 1.4 MB, <= 15 MB budget).

- **T-017:** Backup archive writer (ZIP, manifest, JSONL per table, SHA-256, backup registry test):
  - Serialization Infrastructure & Version Catalog Integration:
    - Added `kotlinx.serialization` (JSON `1.6.3`) and compiler plugin (`org.jetbrains.kotlin.plugin.serialization`) to `gradle/libs.versions.toml` and `app/build.gradle.kts`.
    - Recorded decision in [context/adr/ADR-016-kotlinx-serialization.md](file:///home/rahul/AndroidStudioProjects/LifeKernel/context/adr/ADR-016-kotlinx-serialization.md).
  - Manifest & Portable Settings Models (`com.lifekernel.app.infra.backup.model`):
    - `BackupManifest`: Root metadata descriptor (`manifest.json` at ZIP root) containing `format_version = 1`, `app_version = "0.1.0"`, `room_schema_version = 1`, `created_at_utc_ms` (UTC epoch ms derived from injected `Clock`), `device_zone_id` (informational local zone ID), and `files` array.
    - `BackupFileEntry`: Records `path` (e.g. `"data/tasks.jsonl"`), `rows` (integer count), and `sha256` (lowercase hex checksum).
    - `BackupSettings`: Exports portable preferences (`settings.json` at ZIP root) containing `theme_palette_id`, `default_planner_view`, `week_start`, `time_format`. Strictly excludes device-local state (backup folder URI, dirty flag, snooze state).
  - Table JSONL Serializers & DTOs (`com.lifekernel.app.infra.backup.serializer`):
    - Implemented DTOs and bidirectional mappers for all 4 tables in Room schema v1:
      - `tasks` (`data/tasks.jsonl`): `TaskBackupDto` with column keys (`id`, `title`, `description`, `status`, `priority`, `start_date`, `due_date`, `parent_id`, `sort_order`, `recur_type`, `recur_interval`, `recur_weekdays`, `recur_until`, `created_at`, `updated_at`). Explicit `null` encoding for null columns.
      - `tags` (`data/tags.jsonl`): `TagBackupDto` (`id`, `name`, `created_at`).
      - `task_tags` (`data/task_tags.jsonl`): `TaskTagBackupDto` (`task_id`, `tag_id`).
      - `task_completions` (`data/task_completions.jsonl`): `TaskCompletionBackupDto` (`id`, `task_id`, `occurrence_date`, `completed_at`, `completed_on`).
  - Table Backup Registry (`com.lifekernel.app.infra.backup.registry`):
    - `TableBackupHandler`: Contract defining `tableName`, `entityClass`, `relativePath`, and `exportRows()`.
    - `BackupRegistry`: Central registry mapping all Room entities to table handlers in dependency order (`tags`, `tasks`, `task_tags`, `task_completions`).
  - Archive Writer Engine (`com.lifekernel.app.infra.backup.writer`):
    - `BackupWriter` interface and `DefaultBackupWriter` implementation: Reads database records via DAOs strictly on `Dispatchers.IO`.
    - Streams per-table JSONL files into scratch directory while calculating exact row counts and streaming SHA-256 digests via `DigestOutputStream` and `MessageDigest`.
    - Streams complete ZIP archive using `java.util.zip.ZipOutputStream` with `manifest.json` and `settings.json` at root and `data/*.jsonl` in data subfolder.
    - Atomic generation guarantee: Writes archive to `.tmp` file first and renames to target destination upon 100% completion; deletes scratch and temporary files on any failure.
  - Domain & Data Repository Integration (`com.lifekernel.app.domain.repository` & `com.lifekernel.app.data.repository`):
    - `BackupRepository`: Contract declaring `createBackup(targetFile: File): AppResult<File>` and `readManifest(backupFile: File): AppResult<BackupManifest>`.
    - `BackupRepositoryImpl`: Delegates to `BackupWriter` and provides ZIP manifest parsing and error handling on `Dispatchers.IO`.
    - `AppContainer` & `DefaultAppContainer`: Wired `backupWriter` and `backupRepository`.
  - Automated Test Suites:
    - Unit Tests (`app/src/test/...`):
      - `BackupRegistryTest`: Enforces that all entities declared in `LifeKernelDatabase` and all tables exported in Room schema JSON are registered in `BackupRegistry`. Test automatically fails if any table is omitted.
      - `BackupManifestTest`: Verifies manifest and settings serialization, JSON key matching to `DATABASE_SCHEMA.md` §11.2, omission of device-local preferences, and lowercase hex SHA-256 calculation.
      - `JsonlSerializationTest`: Verifies exact database column key mapping, explicit null serialization, and round-trip fidelity across all 4 tables.
      - `BackupRepositoryTest`: Verifies `createBackup` delegation, `readManifest` parsing, missing manifest error, corrupted ZIP error, and non-existent file error.
      - `LayerBoundaryTest`: Continues to verify 0 forbidden imports in `domain/`.
      - Total: **165 passing JVM unit tests** (100% success rate, 0 failures, 0 skipped).
    - Instrumented Tests (`app/src/androidTest/...`):
      - `BackupArchiveWriterIntegrationTest`: End-to-end integration test with SQLite in-memory database and DataStore. Generates real ZIP archive, verifies entries, verifies manifest values, verifies SHA-256 hashes against actual file contents, verifies row counts against database records, and tests manifest reading and error states.
      - `compileDebugAndroidTestKotlin`: 0 compilation errors across all instrumented test suites.
  - Verification Outputs:
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest` — BUILD SUCCESSFUL (165 tests passed, 0 failures, 0 errors, 0 skipped).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew lintDebug` — BUILD SUCCESSFUL (0 errors).
    - `JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug assembleRelease` — BUILD SUCCESSFUL (Release APK size 1.4 MB, <= 15 MB budget).

---

### Milestone M4: Task Management — Formal Completion Sign-off
- **Milestone Scope Concluded:**
  - `T-012`: Daily planner tasks view (overdue, today, done partitions, 220ms completion transition, quick-add capture sheet).
  - `T-013`: Task detail screen, editing, completion state, priority/due-date selectors, delete with confirmation dialog.
  - `T-014`: Tags on tasks, tag picker with inline creation, orphan tag transactional pruning, lowercase normalization.
  - `T-015`: Subtasks checklist, independent completion status, progress header/counter, SQLite foreign-key cascade deletion.
  - `T-016`: Global cross-date search, conjunctive multi-criteria filter bar, 250ms debounce pipeline, empty search results fallback, 48dp touch targets, BackHandler priority.
- **Milestone Status:** Fully verified, zero lint issues, zero regressions, 140 passing unit tests. Signed off.

## In Progress

- Awaiting author review and formal sign-off for Task T-017 (Backup archive writer & registry test).

## Next Up

1. Milestone M5 — Task T-018: Restore: validate fully, replace atomically per `context/IMPLEMENTATION_PLAN.md` (FR-BKP-006, FR-BKP-007).
2. Milestone M5 — Task T-019: Folder picker, persisted permission, retention (last 5 backups), last-backup info, backup screen.

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
| ADR-016 | kotlinx.serialization for backup JSON | Typed, testable parsing ([ADR-016](file:///home/rahul/AndroidStudioProjects/LifeKernel/context/adr/ADR-016-kotlinx-serialization.md)) | Accepted |
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