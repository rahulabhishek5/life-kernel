# AI Workflow Rules

*These rules also serve as the development-agent instructions (`AGENTS.md` from the master plan). There is no separate `AGENTS.md`.*

## Approach

Build this project incrementally using a spec-driven workflow. The context files define what to build, how to build it, and the current state of progress. Always implement against these specs. Do not infer or invent behavior from scratch.

**Read, in this order, at the start of every session:**

1. `progress-tracker.md` (current phase, goal, open questions, session notes)
2. `project-overview.md` and `PRD.md` (what and why; requirement IDs)
3. `ARCHITECTURE.md` (boundaries, storage model, invariants)
4. `code-standards.md` (how to write the code)
5. `ui-context.md` and `design-system.md` when touching UI
6. `DATABASE_SCHEMA.md` when touching data
7. `PRIVACY_SECURITY.md` when touching storage, backup, app lock, links, permissions, or dependencies
8. `ANALYTICS_SPEC.md` when touching analytics or insights
9. `TEST_PLAN.md` for the tests a task needs, and `IMPLEMENTATION_PLAN.md` for the current task card
10. `adr/` for decisions; add an ADR before any new dependency

The author is new to Kotlin and Compose. Keep changes small enough to review and understand. For each unit, state in plain language what was built, why it is built that way, and which requirement IDs it satisfies.

## Hard Prohibitions

Never do any of the following without an explicit, written approval from the author and a matching ADR and doc update first:

- Add any AI model, AI API, chatbot, or AI-dependent feature to the app.
- Add cloud sync, accounts, login, a backend, or any remote storage.
- Add telemetry, analytics SDKs, crash-reporting SDKs, advertising, or tracking.
- Request the INTERNET permission, add a networking library, or make any network request.
- Add background work (WorkManager, services, alarms, boot receivers) or system notifications.
- Add any permission not already listed in `PRIVACY_SECURITY.md`.
- Store file paths or link local documents for resources (URLs only).
- Implement finance (P4), widgets, or any feature outside the current phase.
- Add a dependency without an ADR.
- Write custom cryptography or hardcode keys.
- Describe the app lock as encryption.

If a task seems to require one of these, stop and ask.

## Scoping Rules

- Work on one feature unit at a time.
- Prefer small, verifiable increments over large speculative changes.
- Do not combine unrelated system boundaries in a single implementation step.
- Never generate the whole app or many unrelated files at once.
- Do not start a phase until the previous phase's units are done and recorded in `progress-tracker.md`.
## Task Evidence and Build Files

- Only the author marks a task approved. Do not describe a task as approved or complete without listing the commands run and their results.
- At the end of every task, list every file changed, with one line on why. Build-configuration files (`build.gradle.kts`, `gradle.properties`, `libs.versions.toml`, `settings.gradle.kts`, the manifest, ProGuard or R8 rules) get an explicit reason and the exact change.
- Do not change build configuration, versions, or dependencies unless the task requires it. If a build problem forces a change, state the problem, the change, and the alternatives.
- Record verification results (test output, device checks, measurements) in `progress-tracker.md` or `TEST_PLAN.md` section 14, not only in the chat.
- Build and test with the project's established command form: `JAVA_HOME=/opt/android-studio/jbr ./gradlew <task>`.

## When to Split Work

Split an implementation step if it combines:

- **UI and data-model changes** (for example, a new screen and a new Room table and migration).
- **Domain rules and presentation** (build and test the rule in `domain/` first, then the UI that uses it).
- **More than one module or unrelated screen** (for example, tasks and habits).
- **Backup/restore, export, or app-lock changes with feature work.**
- **Behavior not clearly defined in the context files.**

If a change cannot be verified end to end quickly, the scope is too broad. Split it.

## Handling Missing Requirements

- Do not invent product behavior not defined in the context files.
- If a requirement is ambiguous, resolve it in the relevant context file before implementing.
- If a requirement is missing, add it as an open question in `progress-tracker.md` before continuing.
- Version numbers, API behavior, and platform limits must be checked against current official Android documentation, not guessed from memory.
- Performance, size, and security claims require measurement or audit. Until then, call them targets, not facts.

## Dependency and Platform Rules

- Version catalog only; no ranges or "latest".
- Verify compatibility of Kotlin, Compose, Room, Navigation, and Gradle versions before first build and record the chosen versions in the tracker.
- Re-run the merged-manifest and dependency audit after any dependency change.

## Protected Files

Do not modify the following unless explicitly instructed:

- Exported Room schema JSON files (generated; commit them, do not hand-edit).
- Migrations that have already been released (add a new migration instead).
- Generated build output, Gradle wrapper files, and signing keys or keystores (never commit keys).
- `PRD.md` decisions D-01 to D-11 and `ARCHITECTURE.md` invariants (change only through the documented process).
- Raw Material components restyled inside features; styling changes belong in `core/designsystem`.
- Existing ADRs once accepted (supersede with a new ADR instead of editing).

## Keeping Docs in Sync

Update the relevant context file whenever implementation changes:

- System architecture or boundaries (`ARCHITECTURE.md`)
- Storage model or schema decisions (`ARCHITECTURE.md`, `DATABASE_SCHEMA.md`)
- Code conventions or standards (`code-standards.md`)
- Design tokens or components (`ui-context.md`, `design-system.md`)
- Feature scope or requirements (`PRD.md`, with a revision-history entry)
- Progress, open questions, and decisions (`progress-tracker.md`)

Documentation updates ship in the same change as the code.

## Before Moving to the Next Unit

1. The current unit works end to end within its defined scope.
2. No invariant defined in `ARCHITECTURE.md` was violated.
3. Tests for the unit exist and pass (`./gradlew testDebugUnitTest`; instrumentation or UI tests where the unit touches data or UI).
4. `./gradlew assembleDebug` and `./gradlew lintDebug` pass with no new errors.
5. For units touching data, a migration exists if the schema changed, and the migration test passes.
6. For units touching UI: tokens only (no hardcoded values), 48dp touch targets, accessibility descriptions, behavior with animations off.
7. For units touching storage, backup, or links: the unit works with airplane mode on, and the merged manifest still has no INTERNET permission.
8. `progress-tracker.md` reflects the completed work, new decisions, and any new open questions.
9. The author has reviewed and understood the change.

## Session Routine

- **Start:** read the files above, restate the current goal in one sentence, confirm the unit's scope and acceptance criteria before writing code.
- **End:** update `progress-tracker.md` (completed, in progress, next up, session notes) so the next session can resume without re-deriving context.
