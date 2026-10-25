# Life kernel (working title; repository folder `LifeKernel`)

*Final app name and application ID are open (OQ-01); confirm before sharing any build. Summary of `PRD.md`; if they disagree, the PRD wins.*

## Overview

Life Kernel is a private, offline-first Android app that brings daily planning, tasks, timetable, habits, goals, notes, saved links, and local analytics into one place. It is built first for its author's own daily use, then for friends, and possibly the Play Store later. It solves the scattering of life across separate cloud-bound to-do, calendar, notes, and habit apps by keeping everything in one local data model with no account, no tracking, and a backup the user controls. The look is an AMOLED-black, warm neo-brutalist, Linux-inspired command center with switchable accent colors.

## Goals

1. **Fast daily planner.** A task can be added from the planner in two interactions or fewer, and the app opens directly on today's planner.
2. **Private by construction.** The release app has no INTERNET permission, no accounts, no telemetry, and no AI inside it.
3. **Works fully offline.** Every feature except opening an external link passes an airplane-mode checklist.
4. **Safe data.** Backup then restore returns identical data in automated tests, and the user is nudged when a backup is more than 5 days old and data has changed.
5. **Lightweight.** Release package within the size budget in `PRD.md` (NFR-SIZE-001), measured on the optimized build.
6. **Daily-use proven.** The author uses the P1 planner for 14 consecutive days without falling back to another planner.

## Core User Flow

1. User opens the app and lands on today's planner (task view by default).
2. User adds a task through quick-add and, optionally, schedules it as a time block.
3. User completes tasks during the day and watches remaining and done counts update.
4. User checks the Now / Next strip and overdue items on the planner.
5. User navigates through the hamburger drawer to the calendar, other modules, or settings.
6. When a backup banner appears, the user taps it, backs up to their chosen folder, and carries on.
7. Over time (later phases), the user logs habits, notes, goals, and resources, reviews rule-based insights, and optionally exports a readable summary to share with an external AI tool.

## Features

### Planning (P1)

- Daily planner with a Tasks view and a Timetable view, and a configurable default.
- Tasks: priority, due and start dates, subtasks, tags, recurrence, search, sort, filter.
- Time blocks, day / week / month calendar, overlap detection, planned versus actual duration.
- Quick capture from the planner.

### App foundation (P1)

- Hamburger drawer navigation, seven switchable accent palettes on an AMOLED-black base.
- Settings: theme, default view, week start, privacy information, version.
- Backup and restore to a user-chosen folder, optional password encryption, in-app backup nudge, optional app lock, data deletion.

### Daily life (P2)

- Habits and simple custom trackers (boolean, numeric, duration) with streaks that ignore unscheduled days.
- Goals with manual, numeric, or milestone progress; projects; milestones.
- Notes and daily journal with tags, pinning, search, and lightweight Markdown.
- Resources: saved URLs with title, icon, color, collection, and tags (URLs only).

### Insights (P3)

- Deterministic analytics, heatmaps, trends, and rule-based insights with explanations.
- AI-readable export: a Markdown/TXT (and optional CSV) snapshot the user chooses to share with external AI tools.

### Finance (P4, later)

- Accounts, transactions, transfers, budgets, savings, reports, built after the charts phase.

## Scope

### In Scope

- Native Android app (Kotlin, Jetpack Compose, Room), minimum Android 8.0.
- Phases P1 to P3 as the v1 definition of done; P4 as a later update.
- Local-only data, user-controlled backup, restore, export, and deletion.
- In-app nudges instead of system notifications.

### Out of Scope

- Any AI model, API, or chatbot inside the app.
- Accounts, cloud storage, sync, multi-device support, collaboration.
- Telemetry, ads, subscriptions, analytics or crash-reporting SDKs.
- System notifications, alarms, and a reminder system (a single opt-in daily notification may be reconsidered after real use).
- Linking local files or attachments; resources are URLs only.
- Database encryption at rest in v1.
- iOS, web, desktop, tablet-optimized layouts, and languages other than English.

## Success Criteria

1. The author uses the P1 planner as their daily planner for 14 consecutive days.
2. A task is added from the planner in two interactions or fewer.
3. Backup, wipe, and restore produce identical data in automated tests; corrupted or truncated backups are rejected without changing data.
4. The release manifest contains no INTERNET permission, and the dependency audit finds no network access.
5. Every feature except opening an external link passes the airplane-mode checklist.
6. Release size is within the target, measured on the optimized release build, not debug.
7. At least three friends use the app for a week before any Play Store decision.
