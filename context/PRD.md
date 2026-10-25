# Life Kernel — Product Requirements Document

| | |
|---|---|
| **Document** | `docs/PRD.md` |
| **Version** | 0.1 (Draft, awaiting approval) |
| **Status** | Documentation phase. No implementation has started. |
| **Last updated** | 2026-10-24 |
| **Working title** | Life Kernel |
| **Platform** | Native Android (Kotlin, Jetpack Compose) |

> This PRD is the source of truth for scope and requirements. Where it conflicts with later documents, this PRD wins until it is explicitly amended. Technical detail lives in `ARCHITECTURE.md`, `DATABASE_SCHEMA.md`, `DESIGN_SYSTEM.md`, `PRIVACY_SECURITY.md`, and `ANALYTICS_SPEC.md`.

---

## 1. Product vision

Life Kernel is a private, offline-first, mobile-first life-management app. It combines daily planning, tasks, timetable, habits, goals, notes, saved resources, and local analytics in one fast, distinctive app, designed as a personal command center with an AMOLED-black, warm neo-brutalist, Linux-inspired look.

It is built first for the author's daily use, then shared with friends, and possibly published on the Google Play Store.

## 2. Problem statement

Daily life is spread across a to-do app, a calendar, a notes app, a habit tracker, browser bookmarks, and spreadsheets. Each is a separate silo, and most are cloud-first, account-bound, ad-supported, or subscription-gated. The result is lost context (what did I plan versus do?), privacy exposure, and constant app switching.

Life Kernel puts these in one local app, with one data model and one backup, under the user's full control.

## 3. Target user

| Phase | User | Notes |
|---|---|---|
| Primary | The author (student with a part-time job) | Daily use, on a personal Android phone |
| Secondary | Friends | Installed from a signed APK |
| Tertiary | General public | Possible Play Store release after the app is proven |

Assumed traits: comfortable with manual entry, values privacy and control, uses a mid-range Android device (Android 8 or newer), and has no need for multi-device sync in v1.

## 4. Product principles

1. Privacy by default; no accounts, telemetry, ads, or subscriptions.
2. Offline-first; every core feature works with no connection.
3. Manual data entry and explicit user control over data.
4. Local, deterministic analytics; no AI inside the app.
5. Minimal, purposeful, fast interactions.
6. Lightweight in size, memory, and background activity.
7. Modular architecture that supports later phases.
8. No unnecessary dependencies or background processing.
9. User-controlled backup, export, import, and deletion.
10. Prefer in-app surfaces over system notifications.

## 5. Goals and non-goals

### 5.1 Goals
- **G1.** A fast daily planner that the user opens every day and that is quick to capture into.
- **G2.** One consistent data model across tasks, time, habits, goals, notes, and resources.
- **G3.** Reliable user-controlled backup and restore, with a nudge when a backup is overdue.
- **G4.** A readable data export that can be given to external AI tools by the user's choice.
- **G5.** Transparent, explainable, rule-based insights.
- **G6.** A distinctive, accessible, consistently themed interface.

### 5.2 Non-goals (v1)
- Any AI model, AI API, chatbot, or AI-dependent feature inside the app.
- Accounts, login, cloud storage, remote database, or automatic sync.
- Multi-device support, collaboration, or sharing of records between users.
- Telemetry, advertising, crash-reporting SDKs that transmit data, or subscriptions.
- Alarms, exact-time reminders, and a full reminder system.
- Linking local files or documents into the app (resources are URLs only).
- Banking or investment integrations.
- iOS, web, or desktop clients.
- The full finance system (see section 6; deferred, not cancelled).

## 6. Scope and phases

Scope is delivered in phases so a small, reliable daily workflow exists before the product expands.

| Phase | Name | Contents | Release label |
|---|---|---|---|
| **P1** | Planner | Navigation drawer, daily planner (tasks + timetable), calendar views, quick capture, settings, theme selector, in-app nudges, backup and restore, optional app lock | v1.0 (MVP) |
| **P2** | Daily life | Habits and trackers, goals and projects, notes and journal, resources (URLs) | v1.x |
| **P3** | Insights | Analytics engine, rule-based insights, charts and heatmaps, AI-readable export | v1.x |
| **P4** | Finance | Full personal finance system (accounts, transactions, transfers, budgets, savings, reports) | Later update |

**Scope decisions agreed:**
- Finance is built after the analytics and visualization phase. No finance tables, screens, or placeholders are created before P4. The data layer will support adding them through normal migrations.
- The v1 definition of done covers P1 to P3. Public release is decided after real use of P1.
- Each phase must be usable on its own before the next begins.

## 7. Decisions already made

| ID | Decision |
|---|---|
| D-01 | Native Kotlin and Jetpack Compose (Flutter rejected) |
| D-02 | Minimum Android version 8.0 (API 26) |
| D-03 | Room over SQLite in app-private storage; no database encryption in v1 |
| D-04 | Optional app lock, labeled honestly as a lock and not as encryption |
| D-05 | Optional password-encrypted backup file |
| D-06 | No system notifications or reminders in v1; in-app surfaces instead (a single opt-in daily notification may be reconsidered after real use) |
| D-07 | Backup nudge shown in-app when more than 5 days have passed since the last backup and data has changed |
| D-08 | Resources are URLs only; the app stores no document paths or linked files |
| D-09 | AI-readable export is a separate feature from backup |
| D-10 | Hamburger (modal drawer) navigation; app opens on the daily planner, task view by default |
| D-11 | Distribution: personal use, then friends via signed APK, then possibly Play Store |
| D-12 | Finance is built after analytics and charts (P4), with no finance tables before then |
| D-13 | Backup is a logical archive restored replace-all in one transaction; encryption optional (PBKDF2-HMAC-SHA256 plus AES-256-GCM) |
| D-14 | Android automatic backup disabled |
| D-15 | Time blocks use floating local time |
| D-16 | App lock uses the system authentication prompt |
| D-17 | Keep the last 5 backups; nudge snooze 24 hours; planner upcoming-deadlines window 7 days |

## 8. Functional requirements

Requirement IDs are stable. Priority: **M** = must, **S** = should, **C** = could. Each module lists acceptance criteria (AC) that define when it is done.

### 8.1 Navigation and quick capture (FR-NAV)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-NAV-001 | The app uses a modal navigation drawer opened from a hamburger icon; no persistent bottom bar. | M | P1 |
| FR-NAV-002 | Drawer entries: Home/Today, Calendar, Tasks, Timetable, Habits, Goals and Projects, Notes and Journal, Resources, Analytics, Settings. Entries for modules not yet built are hidden, not shown disabled. | M | P1 |
| FR-NAV-003 | The app opens on the daily planner with the task view by default. | M | P1 |
| FR-NAV-004 | A quick-add action is reachable from the planner and from relevant screens. Quick actions: add task, add note, mark habit complete, add resource (in the phase where each module exists). | M | P1 |
| FR-NAV-005 | System back navigation behaves predictably; the drawer closes first, then the back stack pops. | M | P1 |
| FR-NAV-006 | Scroll position and screen state are preserved when navigating between top-level destinations and across configuration changes. | M | P1 |
| FR-NAV-007 | Form complexity decides presentation: simple adds use a sheet or dialog, complex records use a dedicated screen. | S | P1 |

**AC-NAV:** Cold start lands on the planner task view. Switching between three top-level destinations and back restores each scroll position. Rotating the device mid-edit keeps the entered text. Back from a top-level destination returns to the planner, then exits.

### 8.2 Daily planner / Home (FR-PLAN)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-PLAN-001 | The planner shows the current date and weekday. | M | P1 |
| FR-PLAN-002 | A clear control switches between the Tasks view and the Timetable view. | M | P1 |
| FR-PLAN-003 | The Tasks view shows today's tasks, with completed and remaining counts and overdue tasks surfaced separately. | M | P1 |
| FR-PLAN-004 | The Timetable view shows today's time blocks. | M | P1 |
| FR-PLAN-005 | A compact "Now / Next" strip shows the current and next time block. | S | P1 |
| FR-PLAN-006 | An upcoming-deadlines section shows tasks due in the next N days (N configurable, default to be set in the design phase). | S | P1 |
| FR-PLAN-007 | A small daily progress indicator (for example, tasks completed today) is shown. | S | P1 |
| FR-PLAN-008 | The user can set the default planner view (Tasks or Timetable) in Settings. | S | P1 |
| FR-PLAN-009 | The planner does not host charts or full analytics. | M | P1 |

**AC-PLAN:** Completing a task updates the counts immediately. With no tasks and no blocks, a helpful empty state is shown. Changing the default view in Settings takes effect on the next launch.

### 8.3 Tasks (FR-TASK)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-TASK-001 | Create, edit, complete, reopen, and delete tasks. | M | P1 |
| FR-TASK-002 | Fields: title (required), description, priority, due date, optional start date, completion timestamp. | M | P1 |
| FR-TASK-003 | Priority levels: a small fixed set (final set defined in the design phase). | M | P1 |
| FR-TASK-004 | Subtasks. | S | P1 |
| FR-TASK-005 | Recurring tasks (daily, selected weekdays, weekly, monthly). Completing one occurrence does not alter other occurrences. | S | P1 |
| FR-TASK-006 | Tags or categories. | S | P1 |
| FR-TASK-007 | Optional links to a project and a goal (activated when those modules exist). | S | P2 |
| FR-TASK-008 | Views: Today, Upcoming, Overdue, Completed, All. | M | P1 |
| FR-TASK-009 | Sort and filter by priority, due date, tag, and status. | S | P1 |
| FR-TASK-010 | Search by title and description. | S | P1 |
| FR-TASK-011 | Quick-add flow creates a task with only a title in one step. | M | P1 |
| FR-TASK-012 | A task may exist without a time block; time blocks are optional. | M | P1 |

**AC-TASK:** A task can be created with only a title in under two interactions after opening quick-add. Overdue means due date before today and not completed. Reopening a completed task clears its completion timestamp. Deleting a task with subtasks asks for confirmation and states what will be removed.

### 8.4 Timetable and calendar (FR-TIME)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-TIME-001 | Daily schedule of time blocks (title, start, end, optional category). | M | P1 |
| FR-TIME-002 | Create, edit, move, and delete time blocks. | M | P1 |
| FR-TIME-003 | Link a time block to a task (optional). | S | P1 |
| FR-TIME-004 | Weekly and monthly calendar views with navigation between dates and a quick return to today. | M | P1 |
| FR-TIME-005 | Overlap detection: overlapping blocks are flagged, not blocked. | S | P1 |
| FR-TIME-006 | Record actual duration against a block for planned-versus-actual comparison. | S | P1 |
| FR-TIME-007 | Optional recurring schedule entries (for example, weekly classes). | S | P1 |
| FR-TIME-008 | Week start day (Monday or Sunday) follows the user setting. | M | P1 |
| FR-TIME-009 | Time zone and DST behavior follow the rules in section 11 and `DATABASE_SCHEMA.md`. | M | P1 |

**AC-TIME:** Two blocks that overlap show a visible conflict indicator. A block that crosses midnight is handled by a documented rule. Changing the device time zone does not shift date-only items. Month and week views both mark days that have items.

### 8.5 Habits and trackers (FR-HAB)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-HAB-001 | Create habits with a schedule: daily, selected weekdays, or every N days. | M | P2 |
| FR-HAB-002 | Record completion for a scheduled date. | M | P2 |
| FR-HAB-003 | Measurable habits: numeric target or duration target. | S | P2 |
| FR-HAB-004 | Entry states: completed, skipped (intentional), missed (explicitly recorded as not done), and not recorded (no entry). | M | P2 |
| FR-HAB-005 | Current streak, longest streak, and completion rate. | M | P2 |
| FR-HAB-006 | An unscheduled day is never counted as missed. | M | P2 |
| FR-HAB-007 | Custom trackers support boolean, numeric, and duration types only. | C | P2 |
| FR-HAB-008 | Weekly and monthly comparisons of completion. | C | P3 |
| FR-HAB-009 | Mark a habit complete from quick capture. | M | P2 |


**AC-HAB:** A weekday-only habit shows no miss on weekends. A skipped day does not break a streak if documented as such in `ANALYTICS_SPEC.md`. Streaks recompute correctly after editing a past entry.

### 8.6 Goals and projects (FR-GOAL)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-GOAL-001 | Goals with horizon (short, medium, long), category, target date, status, and review date. | M | P2 |
| FR-GOAL-002 | Progress methods per goal: **manual**, **numeric** (current versus target), **milestone-based** (completed over total milestones). | M | P2 |
| FR-GOAL-003 | Progress is never derived from linked task counts unless the user explicitly selects a "task-based" method for that goal. | M | P2 |
| FR-GOAL-004 | Milestones on a goal. | M | P2 |
| FR-GOAL-005 | Projects optionally belong to a goal; tasks optionally belong to a project and/or goal. | S | P2 |
| FR-GOAL-006 | Completion history is retained. | S | P2 |

**AC-GOAL:** Changing the progress method does not delete previous data. A goal with zero milestones using the milestone method shows "no milestones", not 0 percent or 100 percent.

### 8.7 Notes and journal (FR-NOTE)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-NOTE-001 | Quick notes and daily journal entries (one journal entry per date by default). | M | P2 |
| FR-NOTE-002 | Edit, archive, delete, search. | M | P2 |
| FR-NOTE-003 | Tags and optional folders. | S | P2 |
| FR-NOTE-004 | Pin important notes. | S | P2 |
| FR-NOTE-005 | Optional association with a date, goal, task, project, or resource. | S | P2 |
| FR-NOTE-006 | Lightweight Markdown support, as plain-text editing with simple rendering; no heavy rich-text editor. | C | P2 |
| FR-NOTE-007 | Plain-text export of a note. | C | P2 |
| FR-NOTE-008 | Edits are autosaved; no work is lost on process death. | M | P2 |

### 8.8 Resources and bookmarks (FR-RES)

Resources are **URLs only**. The app never stores file paths or document references.

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-RES-001 | Save a resource with: custom title, URL, optional description, type, collection, tags, accent color, icon, pinned flag, notes, created and updated timestamps. | M | P2 |
| FR-RES-002 | Paste a URL; the app does not fetch the page or scrape metadata. | M | P2 |
| FR-RES-003 | Open a URL through Android's external view intent. The app requests no network permission for this. | M | P2 |
| FR-RES-004 | Organize by collections; filter and search by title, tag, type, and collection. | M | P2 |
| FR-RES-005 | Copy a URL; edit; archive; delete. | M | P2 |
| FR-RES-006 | Basic URL validation with a clear message; the user can still save a non-standard link after confirmation. | S | P2 |
| FR-RES-007 | Saving, browsing, and searching work offline. Opening the link needs connectivity (stated in-app). | M | P2 |
| FR-RES-008 | No local documents, file pickers, or app-managed file copies. | M | P2 |

**AC-RES:** A resource saved in airplane mode appears in search. Opening a link with no connection fails gracefully through the browser, not through an app crash. The manifest contains no INTERNET permission.

### 8.9 Analytics and insights (FR-ANL)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-ANL-001 | Metrics: daily activity count, active days, task completion rate, overdue percentage, habit completion rate, streaks, planned-versus-completed time, time by category, tracker trends, goal progress over time. | M | P3 |
| FR-ANL-002 | Calendar heatmap and weekly/monthly trend charts using lightweight native charts. | M | P3 |
| FR-ANL-003 | Comparison with the previous equivalent period. | S | P3 |
| FR-ANL-004 | Rule-based insights, each with a plain explanation, calculation period, and supporting data. | M | P3 |
| FR-ANL-005 | Insight thresholds are configurable where useful, with documented defaults. | S | P3 |
| FR-ANL-006 | Missing records are distinguished from recorded inactivity. | M | P3 |
| FR-ANL-007 | Insights state correlations as observations, never as causes. | M | P3 |
| FR-ANL-008 | Minimum sample sizes apply; with insufficient data the app says so rather than showing a figure. | M | P3 |
| FR-ANL-009 | All calculations run off the main thread and live outside Compose UI code. | M | P3 |

Full definitions live in `ANALYTICS_SPEC.md`.

### 8.10 AI-readable export (FR-EXP)

A readable snapshot of the user's data for use with external AI tools, chosen and shared by the user. It is **not** a backup and is not designed to be re-imported.

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-EXP-001 | Generate a single Markdown or plain-text file with a summary header (date range, record counts, active goals and habits) followed by sections per module. | M | P3 |
| FR-EXP-002 | Optional per-module CSV files for large histories. | C | P3 |
| FR-EXP-003 | The user selects modules and a date range before export. | M | P3 |
| FR-EXP-004 | A clear warning states that the file leaves the app's protection once shared and may contain personal information. | M | P3 |
| FR-EXP-005 | The file is saved or shared through Android's standard share and save mechanisms; the app itself sends nothing over the network. | M | P3 |
| FR-EXP-006 | Output is deterministic, with stable field names and an explanation of the format at the top, so external tools can interpret it. | S | P3 |
| FR-EXP-007 | Generation does not block the UI and handles large histories without running out of memory. | M | P3 |

### 8.11 Backup, restore, and data management (FR-BKP)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-BKP-001 | The user can create a full backup on demand. | M | P1 |
| FR-BKP-002 | The backup is a versioned archive with a manifest, schema version, and integrity checksum. | M | P1 |
| FR-BKP-003 | The user chooses a backup destination folder through Android's folder picker; the choice persists. | M | P1 |
| FR-BKP-004 | Optional password encryption of the backup using a standard, vetted scheme (details in `PRIVACY_SECURITY.md`). A forgotten password cannot be recovered; the app says so. | S | P1 |
| FR-BKP-005 | Unencrypted backups show a warning that anyone with the file can read it. | M | P1 |
| FR-BKP-006 | Restore validates the archive (format, version, checksum) before changing anything. Incompatible or corrupted backups are rejected with a clear message. | M | P1 |
| FR-BKP-007 | Restore is atomic: either it fully succeeds or existing data remains untouched. | M | P1 |
| FR-BKP-008 | The app keeps the most recent N backups in the chosen folder (N to be fixed in the design phase) and removes older ones. | S | P1 |
| FR-BKP-009 | Settings shows the date of the last backup. | M | P1 |
| FR-BKP-010 | The user can delete all app data after explicit confirmation. | M | P1 |
| FR-BKP-011 | The app does not rely on Android's automatic cloud backup as the only recovery mechanism. | M | P1 |

**AC-BKP:** Backup then wipe then restore yields identical data. A truncated archive is rejected and leaves data unchanged. A wrong password fails without corrupting anything. Backup works in airplane mode.

### 8.12 In-app nudges (FR-NTF)

The app uses no system notifications in v1.

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-NTF-001 | A dismissible banner appears on the planner when the last backup is more than 5 days old **and** data has changed since that backup. | M | P1 |
| FR-NTF-002 | The banner offers a one-tap path to the backup screen; the backup itself remains a deliberate user action. | M | P1 |
| FR-NTF-003 | A fresh install or an unchanged database never triggers the banner. | M | P1 |
| FR-NTF-004 | Dismissal snoozes the banner for a documented period. | S | P1 |
| FR-NTF-005 | The app performs no background scheduling for nudges; checks happen on app open. | M | P1 |
| FR-NTF-006 | Overdue tasks and the Now/Next block are surfaced in-app, not via system notifications. | M | P1 |

### 8.13 Settings (FR-SET)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-SET-001 | Accent theme selector (see section 8.14). | M | P1 |
| FR-SET-002 | Default planner view; week start day; date and time format preferences. | M | P1 |
| FR-SET-003 | Optional app lock (see SEC-SEC-002). | S | P1 |
| FR-SET-004 | Backup, restore, and data deletion entry points. | M | P1 |
| FR-SET-005 | Storage information: database size and record counts. | C | P1 |
| FR-SET-006 | Privacy information screen stating exactly what the app does and does not do. | M | P1 |
| FR-SET-007 | App version information. | M | P1 |
| FR-SET-008 | Currency preference is **not** included until P4. | M | P1 |

Settings must not grow beyond what the product needs.

### 8.14 Visual identity and theming (FR-UI)

| ID | Requirement | Pri | Phase |
|---|---|---|---|
| FR-UI-001 | AMOLED-black (or near-black) foundation in the default appearance. | M | P1 |
| FR-UI-002 | Warm neo-brutalist component language adapted to a dark foundation: defined outlines, compact cards, clear hierarchy, simple geometric controls, restrained offset effects. | M | P1 |
| FR-UI-003 | Seven accent palettes: Lime/Green (default), Orange, Red, Blue, Purple, Warm Amber, Neutral Monochrome. | M | P1 |
| FR-UI-004 | Palettes are applied through semantic design tokens; no screen hardcodes accent colors. | M | P1 |
| FR-UI-005 | The chosen palette persists and applies to actions, completion states, selection, progress indicators, charts, and heatmaps. | M | P1 |
| FR-UI-006 | System typography for body text; monospaced type used selectively for dates, stats, and technical labels. | M | P1 |
| FR-UI-007 | Subtle, short, interruptible animations (generally 100 to 220 ms) built with built-in Compose APIs. | M | P1 |
| FR-UI-008 | Reduced-motion and system animation settings are respected; the app remains pleasant with animation off. | M | P1 |
| FR-UI-009 | Error, warning, success, disabled, and selected states are distinguishable without color alone. | M | P1 |

## 9. Non-functional requirements

Targets below are **initial budgets**, to be verified by measurement on real devices before being treated as met.

### 9.1 Performance (NFR-PERF)
| ID | Requirement |
|---|---|
| NFR-PERF-001 | Tapping a task checkbox gives visible feedback immediately; the database write happens off the main thread. |
| NFR-PERF-002 | Long task lists and histories scroll smoothly using lazy rendering and indexed queries. |
| NFR-PERF-003 | Analytics queries never freeze the UI; heavy work runs off the main thread. |
| NFR-PERF-004 | Cold start, warm start, memory use, and release package size are measured and recorded in the test plan, with numeric budgets set once a baseline exists. |
| NFR-PERF-005 | No continuous polling or background work without a documented requirement. |

### 9.2 Size (NFR-SIZE)
| ID | Requirement |
|---|---|
| NFR-SIZE-001 | Initial release-artifact target of roughly 15 to 25 MB or less, measured on the optimized release build, not debug. |
| NFR-SIZE-002 | Every dependency is justified in `ARCHITECTURE.md` by purpose, maintenance status, and size impact. |

### 9.3 Compatibility (NFR-COMPAT)
| ID | Requirement |
|---|---|
| NFR-COMPAT-001 | Minimum Android 8.0 (API 26); target and compile versions chosen against current official guidance at build time. |
| NFR-COMPAT-002 | Handles phones of varied screen size, font scale, and density; tablet-optimized layouts are out of scope. |
| NFR-COMPAT-003 | Supports configuration changes and process recreation without data loss. |

### 9.4 Reliability and data integrity (NFR-REL)
| ID | Requirement |
|---|---|
| NFR-REL-001 | Multi-record changes use database transactions. |
| NFR-REL-002 | Schema changes ship with tested Room migrations; no destructive migration on user data. |
| NFR-REL-003 | An interrupted write or restore never leaves the database in a partial state. |
| NFR-REL-004 | Low-storage conditions produce a clear error, not silent data loss. |

### 9.5 Accessibility (NFR-A11Y)
| ID | Requirement |
|---|---|
| NFR-A11Y-001 | Touch targets meet Android's recommended minimum size. |
| NFR-A11Y-002 | Text and controls meet contrast guidance in every accent palette on the black background. |
| NFR-A11Y-003 | Interactive elements have content descriptions for screen readers. |
| NFR-A11Y-004 | The UI respects system font scaling without clipping. |
| NFR-A11Y-005 | Information is never conveyed by color alone. |

### 9.6 Maintainability (NFR-MAINT)
| ID | Requirement |
|---|---|
| NFR-MAINT-001 | Business rules live in a domain layer independent of Compose. |
| NFR-MAINT-002 | A new accent palette can be added without touching feature code. |
| NFR-MAINT-003 | Every feature integrates with the shared data model, navigation, tokens, error handling, and backup. |

## 10. Privacy, security, and offline requirements

### 10.1 Privacy (SEC-PRIV)
| ID | Requirement |
|---|---|
| SEC-PRIV-001 | All records are stored locally; no account or registration exists. |
| SEC-PRIV-002 | The app collects no personal information and sends no data to any server. |
| SEC-PRIV-003 | No telemetry, advertising, or analytics SDKs; no AI SDKs or calls. |
| SEC-PRIV-004 | The manifest requests no INTERNET permission and the minimum of any other permission. Every permission is documented with a justification. |
| SEC-PRIV-005 | Dependencies and transitive dependencies are audited for network access before each release. |
| SEC-PRIV-006 | Logs never contain user content. |
| SEC-PRIV-007 | The privacy screen states plainly that external links and shared exports leave the app's protection. |

### 10.2 Security (SEC-SEC)
| ID | Requirement |
|---|---|
| SEC-SEC-001 | Data lives in Android app-private storage on an unencrypted Room database. The documentation states this plainly. |
| SEC-SEC-002 | **App lock** (optional) restricts access to the UI. It is labeled as a lock and never described as encryption. It uses Android's standard authentication facilities and stores no plaintext secrets. |
| SEC-SEC-003 | **Backup encryption** (optional) uses a standard, vetted scheme with a user-supplied password. No custom cryptography; no hardcoded keys. |
| SEC-SEC-004 | Database encryption at rest is explicitly **not** provided in v1. It may be reconsidered later, and the design must not preclude it. |
| SEC-SEC-005 | Exported components in the manifest are minimized and audited. |
| SEC-SEC-006 | Distinguish, in all documentation: app-private storage, app lock, database encryption, backup encryption, and device encryption. |

### 10.3 Offline (SEC-OFF)
| ID | Requirement |
|---|---|
| SEC-OFF-001 | Every feature except opening an external web link works with no connectivity. |
| SEC-OFF-002 | No operation waits on a network response. |
| SEC-OFF-003 | Offline behavior is verified by manual airplane-mode testing per release. |

External websites need an internet connection when opened. This does not violate the offline-first model, because the app's own data and logic remain entirely local and the connection is made by the user's browser, not by the app.

## 11. Date, time, and time zone conventions (summary)

Detailed in `DATABASE_SCHEMA.md`. The PRD-level rules:
- **Date-only** values (due dates, habit dates, journal dates) are stored as calendar dates with no time zone and do not shift when the device time zone changes.
- **Instants** (completion timestamps, created and updated times) are stored in UTC and displayed in the local zone.
- **Time blocks** have a documented rule for how they behave across time zone and DST changes and across midnight.
- Week start is a user setting.

## 12. Excluded from v1

- Full finance system (planned for P4).
- Any AI functionality inside the app.
- Cloud sync, accounts, multi-device support.
- System notifications and alarms (optional single daily notification may be reconsidered).
- Local document linking and file attachments.
- Database encryption at rest.
- Widgets (candidate for after P2).
- Website metadata scraping and favicon fetching.
- Tablet-optimized and foldable-specific layouts.
- Localization beyond English (to be reviewed before Play Store release).

## 13. Assumptions and constraints

- The author is the sole developer, using an AI coding assistant, and works around studies and a job, so schedule estimates must be conservative.
- Kotlin and Compose are new to the author; AI-written code must be reviewed and understood before acceptance.
- No external service is available to the app by design.
- Current stable, mutually compatible versions of Kotlin, Compose, Room, and Gradle will be verified against official documentation when development begins; this PRD intentionally states no version numbers.
- Performance and size figures are targets until measured.
- The app is English-only and single-user per device.

## 14. Key risks and mitigations

Full detail in `RISK_REGISTER.md`.

| ID | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| R-01 | Scope creep leaves the app unfinished | High | High | Strict phases; P1 usable before P2 starts; scope change requires PRD amendment |
| R-02 | Data loss from device loss or reset | Medium | High | Backup to a user-chosen folder, 5-day nudge, last-N retention, restore tested |
| R-03 | Faulty recurrence or time zone logic | Medium | Medium | Small recurrence rule set; date-only versus instant rule; heavy unit testing |
| R-04 | Unreviewed AI-generated code causes subtle bugs | Medium | High | Small milestones, strict `AGENTS.md`, tests required per task, author reviews each change |
| R-05 | Backup restore corrupts data | Low | High | Validate before applying; atomic restore; corruption tests |
| R-06 | App lock mistaken for encryption | Medium | Medium | Honest wording everywhere; privacy screen; documentation |
| R-07 | Schema mistakes force painful migrations | Medium | Medium | Review schema before coding; migration tests from the first release |
| R-08 | Users forget backups and never see the nudge | Medium | Medium | Banner on open; last-backup date visible in Settings |
| R-09 | Performance degrades with large histories | Low | Medium | Indexes, lazy lists, paged queries, large-dataset tests |
| R-10 | Burnout from solo maintenance | Medium | High | Lean documentation, small releases, dogfood early |
| R-11 | AI agent changes unrelated files or build settings, or invents behavior | Medium | High | One task at a time; every task lists changed files with reasons; author reviews diffs and approves with evidence |
| R-12 | Data loss from a lost or reset phone (automatic cloud backup is off) | Medium | High | 5-day nudge, last 5 backups kept, restore tested |
| R-13 | A migration corrupts or drops data | Low | High | Migration tests from every version with data; no destructive fallback; backup before upgrading |
| R-14 | Restore replaces good data with an older backup | Medium | Medium | Confirmation showing the backup's date and record counts |
| R-15 | Time-zone, DST, or recurrence errors | Medium | Medium | Date-only versus instant rule; floating time blocks; exhaustive tests |
| R-16 | A dependency adds network access or a permission | Medium | High | Approval per dependency; automated manifest check each release |
| R-17 | Forgotten backup password, or an unencrypted backup leaking | Medium | Medium | Warnings; password typed twice; user keeps a second backup |
| R-18 | App slows down with years of data | Medium | Medium | Indexes, lazy lists, large-dataset tests |
| R-19 | Solo-developer burnout or long gaps | Medium | High | Small milestones that each leave a usable app; session notes in the tracker |
| R-20 | Performance, size, or security claims made without measurement | Medium | Medium | Call them targets until recorded in the tracker or `TEST_PLAN.md` |


## 15. Measurable definitions of success

| ID | Measure | Target |
|---|---|---|
| SM-01 | P1 used as the author's daily planner | At least 14 consecutive days without falling back to another planner |
| SM-02 | Backup then restore integrity | 100 percent identical data in automated tests |
| SM-03 | Task capture speed | A task can be added from the planner in two interactions or fewer |
| SM-04 | Network permission | The release manifest contains no INTERNET permission |
| SM-05 | Release size | Within the target in NFR-SIZE-001, measured |
| SM-06 | Crash-free use during author testing | No data-loss bug open at release |
| SM-07 | Offline | All features except external link opening pass the airplane-mode checklist |
| SM-08 | Friend rollout | At least three friends complete a week of use before any Play Store decision |

## 16. Open questions

| ID | Question | Needed by |
|---|---|---|
| OQ-01 | Final app name and permanent application ID (hard to change after publishing) | Before first signed build |
| OQ-02 | Resolved: 5 backups retained; snooze 24 hours | Done |
| OQ-03 | Resolved: priorities None/Low/Medium/High; deadline window 7 days | Done |
| OQ-04 | Resolved: floating local time (see `DATABASE_SCHEMA.md`) | Done |
| OQ-05 | Resolved: see `PRIVACY_SECURITY.md` section 10 | Done |
| OQ-06 | Proposed: native Canvas charts (ADR-015); confirm at P3 start | P3 |
| OQ-09 | Resolved: Android automatic backup disabled (ADR-014) | Done |

## 17. Traceability

Each requirement ID above will map to implementation tasks in `IMPLEMENTATION_PLAN.md` and to test cases in `TEST_PLAN.md`. The traceability matrix is built when those documents are written, and the documentation validation report will flag any requirement without a task or test.

## 18. Revision history

| Version | Date | Change |
|---|---|---|
| 0.1 | 2026-10-24 | Initial draft from brainstorming decisions |
| 0.2 | 2026-10-25 | Fixed FR-SET-003 reference and FR-HAB-004 states; recorded decisions D-12 to D-17; resolved OQ-02 to OQ-06 and OQ-09 |