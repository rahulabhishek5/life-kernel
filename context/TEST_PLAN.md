# Test Plan

| | |
|---|---|
| **Version** | 0.1 (Draft) |
| **Status** | Plan. Test results are recorded in section 14 as they happen; nothing is claimed as passed until recorded there. |
| **Last updated** | 2026-10-25 |

Targets in this document (timings, sizes, counts) are **initial budgets**, not measurements. A budget becomes a requirement only after the T-004 baseline and a real-device measurement.

---

## 1. Principles

1. Business rules and data code are tested **before** the UI that depends on them.
2. A task is not done without its tests (`ai-workflow-rules.md`). Tests are never weakened to pass.
3. Tests use a **fixed clock and fixed time zone** and synthetic data that does not resemble real personal records.
4. Anything involving data loss, money, time, or privacy gets edge-case tests, not only happy paths.
5. Real-device checks cover what automated tests cannot: AMOLED appearance, lock behavior, performance, airplane mode.
6. A bug found later gets a regression test before it is fixed.

---

## 2. Test types and tools

| Type | Scope | Tooling | Runs |
|---|---|---|---|
| Unit | Domain rules, validators, mappers, backup format parsing, analytics | JUnit (local JVM) | `./gradlew testDebugUnitTest` |
| Database | DAOs, constraints, indexes, cascades, transactions | Room with in-memory database on device/emulator (or JVM where supported) | `./gradlew connectedDebugAndroidTest` |
| Repository | Repository behavior over a real in-memory Room, with fixed `Clock` | Same | Same |
| Migration | Each schema version upgrade with data present | Room migration test helper using committed schema JSON | Same |
| Compose UI | Key flows, semantics, states | Compose UI test APIs | Same |
| Navigation and lifecycle | Start destination, back, drawer, recreation, state restoration | Compose navigation tests, activity recreation | Same |
| Accessibility | Touch targets, semantics, font scale, contrast | Compose semantics tests, accessibility checks, manual TalkBack pass | Per screen |
| Performance | Start-up, list scroll, large data, analytics time, memory, size | Manual measurement on device, optionally macrobenchmarks if adopted by ADR | Per milestone |
| Privacy and manifest | Permissions, exported components, dependency audit | Automated manifest check plus manual audit | T-034 and every release |
| Offline | Airplane-mode run of every feature | Manual checklist | Every release candidate |

Static checks: Android Lint (`./gradlew lintDebug`) on every task.

---

## 3. Test data

Generators live in test sources only and are deterministic (fixed seed).

| Dataset | Contents | Use |
|---|---|---|
| **S0** | Empty database | Empty states, first-run behavior |
| **S1 Small** | 20 tasks (mix of overdue, today, future, done, recurring), 5 tags, 10 blocks | Default for UI tests |
| **S2 Medium** | 1,000 tasks, 2,000 completions, 500 blocks | Query and list behavior |
| **S3 Large (10x)** | 10,000 tasks, 40,000 completions, 5,000 blocks, later 50,000 habit entries and 5,000 notes | Scroll, query time, analytics, backup size |
| **S4 Edge dates** | Records on leap day, month ends, year end, DST-change dates, week-boundary dates, midnight-spanning blocks | Date logic |
| **S5 Hostile** | Very long titles at the limit, unusual characters, emoji, right-to-left text, strings that look like SQL or JSON | Validation and backup robustness |
| **B-set** | Stored backup archives: valid v1, truncated, bit-flipped, wrong manifest version, missing file, invalid row values, oversized, encrypted valid, encrypted tampered, encrypted wrong password | Backup and restore |
| **M-set** | A database file at each released schema version, populated with S1 | Migration tests |

Real personal data is never used in tests, previews, or screenshots.

---

## 4. Unit testing

Required suites (each includes empty-input, boundary, and invalid-input cases):

- **Clock and time:** local date from instant across zones; week start Monday and Sunday; month and year boundaries; leap years.
- **Validation:** every limit in `DATABASE_SCHEMA.md` section 2.4, exactly at, below, and above the limit.
- **Task rules:** subtask depth, recurring-task constraints, complete and reopen semantics, overdue definition.
- **Recurrence:** daily, weekly with masks, monthly with month-end clamping, intervals, `recur_until`, completing late, undo; large intervals; leap day.
- **Overlap:** touching blocks are not overlaps; midnight-spanning blocks; recurring expansion.
- **Habit schedule and streaks:** scheduled-date rules for every schedule type; streak table in `ANALYTICS_SPEC.md` section 10; skip transparency; today grace; edits to past entries.
- **Goal progress:** manual, numeric (increasing and decreasing targets, start equals target), milestone (zero milestones), task-based only when selected.
- **Analytics:** every metric and insight with the examples in `ANALYTICS_SPEC.md` section 10, plus not-enough-data results.
- **Backup format:** manifest parsing, checksum verification, per-version upgraders, row validation, encrypted file header parsing (bad magic, bad lengths, out-of-range iteration counts).
- **Theme:** palette contrast thresholds, unknown palette fallback.
- **Layer boundary:** `domain/` contains no `android.*`, Compose, Room, or DataStore imports.

---

## 5. Database and repository testing

- Every table: insert, read, update, delete; NOT NULL and unique constraints reject bad data.
- **Foreign keys:** cascades and SET NULL actions fire as listed in `DATABASE_SCHEMA.md` section 8 (DB-V1).
- Indexes exist (schema JSON check) and the key queries use them (spot-check with query plans on S3).
- Transactions: complete-task, undo, delete-with-children, and restore are atomic: inject a failure mid-operation and verify nothing partial remains.
- Tag orphan cleanup in the same transaction as the last removal.
- Flow re-emission after writes; no emission storms from a single transaction.
- Concurrency: rapid repeated writes (double-tap complete) produce a consistent result.
- Empty database, duplicate attempts, and deleting linked records.
- Time-zone change: `completed_on` stays stable after the zone changes.

---

## 6. Migration testing

- A migration test exists for every schema version change, **from every earlier version**, with S1 data present, run with Room's migration test helper against the committed schema JSON.
- After migration: row counts equal, key values equal, foreign keys valid, Room's schema validation passes.
- Data-rewriting migrations also run on S3 volume and must complete in one transaction.
- Destructive fallback is forbidden; a test asserts it is not configured.
- The backup registry test fails if any Room table lacks a backup entry.
- Before shipping any migration to real data: backup, upgrade, verify (release checklist).

---

## 7. Integration testing

End-to-end flows through repository, domain, and database with a fake `Clock`:

- Add task, complete, reopen, delete; counts and lists match throughout.
- Recurring task across several completions, a late completion, and undo.
- Time block creation with category and task link; deleting the task; skipping a recurring occurrence; planned versus actual.
- Create data in every module, back up, wipe, restore, compare (full round trip, per phase as modules arrive).
- Settings persistence across process restart (theme, default view, week start).
- Backup dirty flag: changes set it; restore and backup clear it appropriately (AQ-01).

---

## 8. UI testing

- **Per screen:** renders empty, loading, populated, and error states; semantics are correct.
- **Key flows:** quick-add (two interactions), complete from the planner, drawer navigation, theme change, backup and restore screens, app lock gate.
- **Navigation and lifecycle:** start destination; drawer then back; back from a top-level destination; state kept on rotation; state restored after process death (in-progress quick-add text, scroll position); rapid repeated navigation does not stack duplicates.
- **Reduced motion:** with animations disabled, state changes are immediate and every flow still works.
- **Theme:** all seven palettes render without clipped or unreadable text (gallery screen).
- **Hostile text:** very long titles and large font scale wrap or scroll without clipping.

---

## 9. Accessibility checks

For every screen before its task is done:

- Touch targets are at least 48dp x 48dp.
- Every control has a role, state, and label; decorative icons are hidden from accessibility.
- Contrast meets the thresholds in `design-system.md` in all palettes (automated for tokens, visual check on device).
- Large font scale (at least the largest system setting) and large display size: no clipped or overlapping text.
- Meaning is never color-only (overdue, selected, error, overlap).
- Manual TalkBack pass on: planner, quick-add, drawer, backup flow, settings. Focus order matches visual order; sheets and dialogs trap and restore focus.
- Heatmap cells expose date and count.

---

## 10. Privacy and security testing

- Automated manifest check (T-034): fails if the release merged manifest contains `INTERNET` or any permission outside `PRIVACY_SECURITY.md` section 4.
- Exported-component check: only `MainActivity`.
- Dependency audit per `PRIVACY_SECURITY.md` section 6 on every dependency change.
- Log check: run flows with a distinctive marker text in titles and notes; confirm the marker never appears in logcat in a release build.
- Backup encryption: round trip; wrong password leaves data unchanged; one flipped bit anywhere is rejected; tampered header rejected; oversized iteration count rejected; archive above the size ceiling gives the documented message.
- App lock: locked on cold start; locks after the grace period; unlock works with biometric and device credential; behavior with no enrolled lock screen; secure-window behavior in recents (SV-02, SV-06).
- Delete-all: no database files remain; backups in the user's folder are untouched and the message says so (SV-05).
- Restore hostility: S5 and B-set archives never crash the app or alter data on failure.

---

## 11. Offline (airplane-mode) checklist

Run on a real device with airplane mode on, Wi-Fi and mobile data off, for every release candidate. Every item passes, except opening an external link, which shows the browser's offline behavior.

- [ ] Cold start, planner loads
- [ ] Add, edit, complete, reopen, delete a task
- [ ] Subtasks, tags, search
- [ ] Recurring task completion
- [ ] Time block create, move, delete, actuals; calendar views
- [ ] Settings changes and theme switch
- [ ] Backup (encrypted and unencrypted) and restore
- [ ] App lock
- [ ] (P2) Notes, habits, goals, saving and searching resources
- [ ] (P3) Analytics, charts, AI export to a local file
- [ ] Opening a saved link fails gracefully in the browser, with no crash in the app

---

## 12. Performance testing

**Method (recorded after T-004):** release build on the author's phone; record device model and Android version; each measurement repeated at least 5 times; report the median and the worst case. Debug builds are never used for claims.

**Initial budgets (to be confirmed by measurement):**

| Area | Budget |
|---|---|
| Checkbox tap feedback | Visible within one frame; DB write off the main thread |
| Cold start, warm start, idle memory | Baseline from T-004; later builds must not regress without a written reason |
| Planner and task list scroll with S3 data | No visible jank in a manual swipe test; profile for dropped frames if doubt |
| Tasks search on S3 | Results appear without visible freeze; `LIKE` search time recorded (notes search: DB-V4) |
| Analytics on S3 | No visible freeze; compute off the main thread; time recorded per metric |
| Backup and restore on S3 | Completes with visible progress; memory stays within limits; time recorded |
| Release size | Within 15 to 25 MB initial target; measure the optimized release artifact |

Large-history scenarios: S3 with empty, populated, and filtered lists; repeated rapid navigation; low-storage condition during backup (clear error, no partial file kept).

---

## 13. Backup and restore testing

| Scenario | Expected |
|---|---|
| Backup then wipe then restore | Identical data in every table |
| Truncated archive | Rejected; data unchanged |
| Bit-flipped data file | Checksum fails; rejected; data unchanged |
| Newer format or schema version | Rejected with "update the app" |
| Older version archive | Upgrader fills defaults; data valid |
| Missing file listed in manifest | Rejected |
| Row with invalid enum, date, or range | Rejected before any write |
| Foreign-key violation inside archive | Foreign-key check fails; rollback; data unchanged |
| Killed process mid-restore | Database is either entirely old or entirely new |
| Interrupted backup | Temporary file ignored and cleaned; no partial archive counted |
| Retention | Only the last 5 matching files kept; other files untouched |
| Folder permission revoked | Clear message and a way to choose again |
| Low storage | Clear error; no partial archive left |
| Encrypted: correct password | Restores |
| Encrypted: wrong password, tampered file, bad header | Rejected; data unchanged |
| Settings in archive | Portable settings applied after commit; device-local settings untouched |

---

## 14. Test and release records

Record results here (or link to them). Nothing is considered verified until it appears below with a date.

| Date | Item | Result | Notes |
|---|---|---|---|
| | T-004 baselines | | |
| | DB-V1 foreign keys enforced | | |
| | DB-V2 CHECK annotation support | | |
| | AQ-01 dirty flag on device | | |
| | SV-02 biometric behavior on older Android | | |
| | Airplane-mode run (per release candidate) | | |
| | Release audit (per release candidate) | | |

---

## 15. Critical regression scenarios

Re-run before every release candidate:

1. Complete a recurring task late; the next due date is computed from the occurrence, not from today.
2. Complete a task, then change the device time zone; the completion's `completed_on` is unchanged.
3. A midnight-spanning block appears on both days correctly and does not overlap incorrectly.
4. Delete a task that has subtasks, completions, tags, and a linked time block; the block remains with no link; counts are consistent.
5. Backup, add data, restore; the added data is gone and nothing else is.
6. Restore a corrupted archive; data unchanged.
7. Upgrade from the previous released version with real-size data; verify counts.
8. Force-stop the app while saving; relaunch with no corruption.
9. Rotate and recreate the activity while quick-add text is entered.
10. Large font scale on planner, quick-add, and settings.

---

## 16. Release acceptance criteria

A build may be shared only if all are true:

1. Unit, database, migration, and UI tests pass; lint has no new errors.
2. Release audit (`PRIVACY_SECURITY.md` section 16) passes.
3. Airplane-mode checklist passes.
4. Critical regression scenarios pass.
5. Backup, upgrade, verify has been done with a real backup before the migration runs.
6. No known data-loss bug is open.
7. Size and performance measurements are recorded and not regressed without a written reason.
8. Accessibility pass completed on every changed screen.
9. The signed build installs and runs on a device other than the build machine (T-036).

---

## 17. Requirement traceability matrix

Every requirement ID in `PRD.md` maps to a verification method and the task that delivers it. "Manual" means a recorded manual check in section 14. A requirement is covered only when its method has a recorded pass.

| Requirement IDs | Verification | Task(s) |
|---|---|---|
| FR-NAV-001 to 003, 005, 006 | Compose navigation and lifecycle tests | T-009 |
| FR-NAV-004 | Compose flow tests (task now; note, habit, resource as modules arrive) | T-011, M11 to M13 |
| FR-NAV-007 | Compose tests; design review | T-011 onward |
| FR-PLAN-001 to 003, 007, 009 | ViewModel and Compose tests, fixed clock | T-010 |
| FR-PLAN-004, 005, 008 | ViewModel and Compose tests | T-030, T-012 |
| FR-PLAN-006 | ViewModel tests | T-010 or T-030 (decide window default, see report) |
| FR-TASK-001 to 003, 011, 012 | Repository, ViewModel, flow tests | T-008, T-011, T-013 |
| FR-TASK-004 | Domain and repository tests | T-016 |
| FR-TASK-005 | Recurrence unit tests, repository tests | T-023, T-024 |
| FR-TASK-006 | Repository tests | T-014 |
| FR-TASK-007 | Repository tests | M14 |
| FR-TASK-008 to 010 | Query tests on S2, S3; UI tests | T-015 |
| FR-TIME-001 to 003 | DAO, repository, UI tests | T-025, T-027 |
| FR-TIME-004, 008 | Calendar tests across week-start settings and boundaries | T-031 |
| FR-TIME-005 | Overlap unit tests | T-026 |
| FR-TIME-006 | Repository tests, DST-day test | T-028 |
| FR-TIME-007 | Recurrence expansion tests | T-029 |
| FR-TIME-009 | Time-zone and DST tests on S4 | T-025 to T-028 |
| FR-HAB-001 to 007, 009 | Schedule, streak, entry tests; UI | M13 |
| FR-HAB-008 | Analytics tests | M15 |
| FR-GOAL-001 to 006 | Progress-formula tests; repository; UI | M14 |
| FR-NOTE-001 to 008 | Repository, search measurement (DB-V4), autosave and process-death tests | M12 |
| FR-RES-001 to 008 | Repository, UI, offline, manifest tests | M11, T-034 |
| FR-ANL-001 to 009 | Analytics unit tests; performance on S3; layer test | M15 to M17 |
| FR-EXP-001 to 007 | Export generator tests; large-history memory test; manual save/share check | M18 |
| FR-BKP-001, 002, 006, 007 | Round-trip, hostile archive, atomicity tests | T-017, T-018 |
| FR-BKP-003, 008, 009, 011 | Folder and retention tests; manifest check (`allowBackup`) | T-019, T-002 |
| FR-BKP-004 | Encryption tests (section 10) | T-022 |
| FR-BKP-005, 010 | UI tests; delete-all test | T-021 |
| FR-NTF-001 to 005 | Dirty-flag and banner tests; AQ-01 on device | T-020 |
| FR-NTF-006 | UI tests | T-010, T-030 |
| FR-SET-001, 002, 004 to 007 | DataStore and UI tests | T-012, T-032 |
| FR-SET-003 | App-lock tests | T-033 |
| FR-SET-008 | Review: no currency setting before P4 | T-032 |
| FR-UI-001 to 009 | Theme tests, gallery review, manual AMOLED check | T-005, T-006 |
| NFR-PERF-001 to 005 | Performance measurements (section 12) | T-004, T-035 |
| NFR-SIZE-001, 002 | Release size measurement; dependency ADR review | T-035, each dependency |
| NFR-COMPAT-001 to 003 | Device and emulator runs; recreation tests | T-001, T-009, T-035 |
| NFR-REL-001 to 004 | Transaction, migration, interruption, low-storage tests | T-007, T-008, T-017 to T-019 |
| NFR-A11Y-001 to 005 | Accessibility checks (section 9) | Every UI task, T-035 |
| NFR-MAINT-001 to 003 | Layer test; palette-registry test; code review | T-003, T-005 |
| SEC-PRIV-001 to 007 | Manifest, dependency, log, and privacy-screen checks | T-002, T-032, T-034 |
| SEC-SEC-001, 004, 006 | Documentation and privacy-screen review | T-032 |
| SEC-SEC-002 | App-lock tests | T-033 |
| SEC-SEC-003 | Encryption tests | T-022 |
| SEC-SEC-005 | Exported-component audit | T-034 |
| SEC-OFF-001 to 003 | Airplane-mode checklist | Every release candidate |
| SM-01 | Manual: 14 consecutive days of daily use | After M5 |
| SM-02 | Automated round-trip tests | T-017, T-018 |
| SM-03 | Compose flow test (two interactions) | T-011 |
| SM-04 | Automated manifest check | T-034 |
| SM-05 | Release size measurement | T-035 |
| SM-06 | Open-bug review at release | Every release |
| SM-07 | Airplane-mode checklist | Every release candidate |
| SM-08 | Manual: friends use for a week | After T-036 |
