# Code Standards

| | |
|---|---|
| **Version** | 0.1 (Draft) |
| **Applies to** | All Kotlin, Compose, Room, and Gradle code in this project |
| **Companion files** | `ARCHITECTURE.md` (boundaries and invariants), `ui-context.md` (tokens), `ai-workflow-rules.md` (process) |

Placeholders such as `{base-package}` are replaced once the application ID is chosen (OQ-01).

---

## General

- Keep classes and functions small and single-purpose. If a name needs "And", split it.
- Fix root causes. Do not layer workarounds on a bug.
- Do not mix unrelated concerns in one composable, ViewModel, or repository.
- Prefer the simplest design that meets the requirement. No speculative abstractions, no "framework" code for needs that do not exist yet.
- Do not implement anything outside the current phase in `progress-tracker.md` or outside `PRD.md` scope.
- Every feature integrates with the shared data model, navigation, design tokens, error handling, and backup. No standalone mini-apps.
- Comments explain *why*, not *what*. Delete commented-out code.

## Kotlin

- Follow the official Kotlin code style.
- Prefer `val` and immutable data classes; mutable state stays inside ViewModels and is exposed read-only.
- No `!!`. Handle nullability explicitly; use `requireNotNull` or `checkNotNull` only for true invariants, with a message.
- No `GlobalScope`. Use `viewModelScope`, an injected application scope, or a scope passed in from the caller.
- Model closed sets (priority, habit entry state, goal progress method) as enums or sealed types, never as raw strings or ints in domain code.
- Validate untrusted input at boundaries: user text, pasted URLs, and especially backup files. Treat a backup archive as untrusted until validated.
- Public functions in `domain/` and `data/` have clear, tested contracts. Avoid boolean flag parameters; use named types.
- Avoid platform types and Java-style utility classes where a Kotlin extension or small function is clearer.

## Architecture rules

- `domain/` imports no `android.*`, Compose, Room, or DataStore. This is enforced by a unit test that scans imports (see Testing).
- Room entities and DAOs never leave `data/`. Repositories map to domain models.
- Composables never call DAOs or repositories directly; they receive state and callbacks from a ViewModel.
- Features do not import other features. Shared UI goes to `core/designsystem`, shared logic goes to `domain`.
- Business rules (recurrence, streaks, overlap, analytics, insights) live in `domain/rules` and are covered by unit tests before UI is built on them.
- Manual DI only: new dependencies are wired in `AppContainer`. No service locators hidden elsewhere, no singletons outside the container.

## Compose

- Stateless composables by default: state flows down, events flow up (state hoisting).
- One `UiState` data class per screen, immutable. The screen composable takes `uiState` and lambdas; a thin route-level composable obtains the ViewModel and collects state with lifecycle awareness.
- Lazy lists always provide stable `key`s (record ids) and `contentType` where lists are heterogeneous.
- Do not do work in composition: no I/O, no sorting or filtering of large lists inline, no creating objects that should be remembered. Use `remember`, `derivedStateOf`, or compute in the ViewModel.
- Keep composables short. Extract when a function exceeds roughly 60 lines or has more than one responsibility.
- Every interactive element has a minimum 48dp touch target and an accessibility description (see `design-system.md`).
- Previews are optional but must not contain realistic personal data.
- Respect reduced motion through the shared `LocalReducedMotion` value; never read system animation settings in feature code.

## Styling

- Use design tokens from `core/designsystem` only. **No hardcoded colors, dp values for spacing/radius, or text sizes in feature code.** Tokens are defined in `ui-context.md`.
- Use the `Pos*` wrapper components (button, card, text field, checkbox, chip, sheet, dialog, top bar, drawer) for anything styled. Do not restyle raw Material components inside features.
- Colors come from semantic roles (`accent`, `onAccent`, `surface`, `textMuted`, `stateError`, ...), never from palette names. Adding a palette must require no feature-code change.
- Follow the radius, border, and spacing scales in `ui-context.md`.
- Information is never conveyed by color alone: pair state colors with an icon or label.

## Data and Storage

- All user records are stored in Room. DataStore holds small settings and backup bookkeeping only, never user content.
- No other persistence: no files holding user content, no SharedPreferences, no caches of records.
- **Date-only values** (due date, habit date, journal date) use a calendar-date representation with no time zone. **Instants** (created, updated, completed) are stored in UTC. Never mix them. Exact column types are in `DATABASE_SCHEMA.md`.
- All times come from an injected `Clock`, never from `System.currentTimeMillis()` or `LocalDate.now()` in domain or data code, so tests can control time.
- Every table has a stable primary key and, where relevant, `created_at` and `updated_at` instants.
- Foreign keys and unique constraints are declared in the schema, not enforced only in code.
- Any change spanning multiple rows or tables runs in a single Room transaction opened in the repository.
- Never use `fallbackToDestructiveMigration`. Every schema version bump ships with a migration and a migration test. Do not hand-edit exported schema JSON files.
- Resources store URL strings only. Never store file paths, content URIs of user documents, or downloaded content.
- Money (P4 only) uses integer minor units. Floating point is never used for amounts. No finance code before P4.

## Concurrency

- All database and file work is off the main thread. Room `suspend` and `Flow` APIs are the default; never enable main-thread queries.
- Dispatchers are injected so tests can replace them.
- Long operations (backup, restore, export) run in the application scope with progress state and are cancellation-safe: a cancelled or killed operation never leaves partial data in place (write to temporary, then rename or commit).
- No polling loops. No timers kept running for visual effect.

## Error handling

- Expected failures are values (sealed `AppResult` or typed errors), not exceptions thrown across layers.
- Every failure the user can cause or encounter (invalid backup, wrong password, full storage, folder permission lost, invalid URL) has a human-readable message and, where possible, an action.
- Never swallow exceptions silently. Catch at a defined boundary and convert to state.
- A write that fails must look like a failure to the user, never like a success.

## Privacy and logging

- No INTERNET permission. Any new permission needs an approved change to `PRIVACY_SECURITY.md` first.
- No telemetry, analytics, ads, crash reporting, AI SDKs, or networking libraries.
- Logs never contain user content (titles, notes, URLs, amounts, tags). Use the logging wrapper; release builds log no content.
- Do not put user content in intent extras, navigation arguments, or file names. Pass record ids.
- No custom cryptography and no hardcoded keys or passwords. Backup encryption uses the scheme in `PRIVACY_SECURITY.md` only.
- The app lock is a lock. Never describe it as encryption in code, strings, or docs.
- Exported manifest components are kept to the minimum the platform requires.

## Dependencies

- Add a dependency only after an ADR records its purpose, maintenance status, size impact, and network behavior.
- Versions live in the Gradle version catalog. Verify compatibility against official documentation; do not guess versions.
- No version-range or "latest" declarations.
- Run the release audit (dependency tree and merged manifest) after any dependency change.

## Testing

- Business rules and repositories are written test-first or alongside their tests. A feature unit is not done without its tests (see `TEST_PLAN.md`).
- Required test types per area:
  - Domain rules: JUnit unit tests with edge cases (empty data, leap years, month boundaries, DST, recurrence, streaks, overlaps).
  - DAOs and repositories: Room tests against an in-memory database.
  - Migrations: tested against the exported schema files.
  - Backup and restore: round-trip, truncated, corrupted, wrong password, incompatible version, interrupted operation.
  - UI: Compose tests for key flows; accessibility checks for new screens.
- A unit test verifies that `domain/` has no forbidden imports.
- Tests use fixed clocks and fixed time zones. Test data is synthetic and never resembles real personal records.
- Do not weaken or delete a test to make a build pass; fix the cause.

## Naming and file organization

- Packages are lowercase. Classes `PascalCase`. Functions and properties `camelCase`. Constants `SCREAMING_SNAKE_CASE`.
- Suffixes: `...Screen`, `...ViewModel`, `...UiState`, `...Repository` (interface in domain, `...RepositoryImpl` in data), `...Dao`, `...Entity`, `...UseCase`.
- One top-level public class per file; the file is named after it.
- Design-system components are prefixed `Pos` (for example `PosButton`).
- Folders:
  - `core/designsystem/` — theme, tokens, `Pos*` components
  - `core/time/` — clock and date helpers
  - `core/result/` — `AppResult` and error types
  - `domain/model|repository|usecase|rules/` — pure Kotlin
  - `data/local|repository|prefs/` — Room, mappers, DataStore
  - `infra/backup|export|lock|storage/` — Android-specific glue
  - `feature/<name>/` — Screen, ViewModel, UiState, `components/`
  - `navigation/` — routes, NavHost, drawer list
  - `di/` — `AppContainer` and ViewModel factories
- User-visible text lives in string resources, not in composables, so wording (including privacy statements) is changed in one place.
- Documentation lives in `context/`; decision records in `context/adr/` named `ADR-NNN-short-title.md`.

## Protected files

Do not modify unless explicitly instructed:
- Exported Room schema JSON files (generated; commit, never hand-edit).
- Generated build output and wrapper files.
- Existing migrations after release (add new ones instead).
- `PRD.md` decisions D-01 to D-11 (amend through the documented process).

## Build and verification

Run before considering a unit complete (task names are the standard Android Gradle ones; confirm they exist when the project is created):

1. `./gradlew assembleDebug` — compiles.
2. `./gradlew testDebugUnitTest` — unit tests pass.
3. `./gradlew lintDebug` — no new lint errors.
4. `./gradlew connectedDebugAndroidTest` — instrumentation and UI tests pass on a device or emulator, when the unit touched data or UI.
5. For release candidates: `./gradlew assembleRelease`, then measure size and inspect the merged manifest for permissions.

Debug builds are never used to make size or performance claims.
Use the project's established command form, `JAVA_HOME=/opt/android-studio/jbr ./gradlew <task>`, and report the exact command and its result when stating that a check passed.

## Version control

- Small, focused commits, one feature unit each, with a message stating what and why.
- Never commit signing keys, keystores, or passwords. Back up the release signing key separately; losing it blocks future updates.
- Update `progress-tracker.md` in the same change as the work it describes.
