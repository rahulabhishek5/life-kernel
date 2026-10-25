# Analytics Specification

| | |
|---|---|
| **Version** | 0.1 (Draft) |
| **Status** | Specification for P3 (M15 to M17). Nothing is implemented. |
| **Depends on** | `DATABASE_SCHEMA.md` (tables and conventions), `PRD.md` (FR-ANL) |
| **Last updated** | 2026-10-25 |

All calculations are **deterministic**, run locally, use no AI, and live in `domain/rules/analytics` as pure Kotlin that takes plain data and a `Clock`. They never import Android, Room, or Compose. Finance metrics are reserved for P4 and marked as such.

---

## 1. Principles

1. **Missing is not zero.** Days with no data are "no data", not "inactive", unless a record explicitly says inactivity (for example a `MISSED` habit entry).
2. **Say when there is not enough data.** Every metric and insight has a minimum sample. Below it, the app shows "not enough data yet" and how much more is needed, never a figure.
3. **Explain everything.** Every insight shows the rule that fired, the calculation period, and the numbers behind it.
4. **Observations, not causes.** Wording never claims one thing caused another.
5. **No false precision.** Percentages are shown as whole numbers; ratios with tiny samples are shown as counts ("3 of 4").
6. **Cheap by default.** Compute on demand, off the main thread, from indexed queries; no persisted analytics tables.

---

## 2. Shared definitions

| Term | Definition |
|---|---|
| Today | The local date from the injected `Clock` in the device's current zone |
| Date range | Inclusive pair of local dates `[start, end]` |
| Period | A date range of one of these kinds: `DAY`, `WEEK` (starts on the user's week-start setting), `MONTH` (calendar month), `ROLLING(n)` (the n days ending today) |
| Complete period | A period whose end date is before today. The current week or month is **partial** |
| Previous period | For `WEEK` and `MONTH`: the immediately preceding calendar week or month. For `ROLLING(n)`: the n days before the current n days. For custom ranges: the same number of days immediately before |
| First activity date | The earliest local date on which any activity event (section 3) exists, or the earliest habit `start_date`, whichever is earlier |
| Tracked days in a range | Days in the range that are on or after the first activity date and on or before today. Future days and days before the first activity date are **not tracked** |
| Scheduled date (habit) | A date on which the habit's schedule says it applies, within `[start_date, min(end_date, today)]` (rules in `DATABASE_SCHEMA.md` section 13) |

Dates derived from an instant (for example a note's creation) use the local zone at calculation time. If the user changes time zone, those few derived dates can shift; record types that matter for statistics already store a local date (`completed_on`, entry `date`, journal date). A note creation date is the one exception, and the schema adds a `created_on` snapshot for notes and a `completed_on` snapshot for goal milestones (see validation report), so this does not apply to them either.

---

## 3. Activity events

"Activity" means things done, not things planned. One event per item. No weighting.

| Code | Event | Date used |
|---|---|---|
| A1 | Task completion (`task_completions`) | `completed_on` |
| A2 | Habit entry with state DONE | entry `date` |
| A3 | Note or journal entry created | `created_on` (local date snapshot) |
| A4 | Time-block occurrence with `actual_minutes` recorded | occurrence `date` |
| A5 | Goal milestone completed | `completed_on` (local date snapshot stored with the milestone; see validation report) |
| A6 | Goal progress entry recorded | entry `date` |

Not activity: creating or editing tasks, planning blocks, editing notes, SKIPPED or MISSED entries.

**Daily activity count** for date D = number of events A1 to A6 dated D. This is derived; no table stores it (DB-12).

---

## 4. Metric definitions

Each metric lists: formula, inputs, period, missing-data behavior, edge cases.

### M-01 Daily activity count
- **Formula:** count of events A1 to A6 on D.
- **Period:** a single date, or a series over a range.
- **Missing data:** dates before the first activity date or after today are **no data**, not zero.
- **Edge cases:** midnight-crossing time blocks count on their start date; deleted records no longer count.

### M-02 Active days and active-day ratio
- **Active day:** a tracked date with daily activity count at least 1.
- **Active days** = number of active days in the range. **Ratio** = active days divided by tracked days in the range.
- **Minimum sample:** at least 7 tracked days to show a ratio; otherwise show the count only.
- **Edge:** if tracked days is 0, show "no data".

### M-03 Calendar heatmap
- **Input:** daily activity counts for the displayed window (for example the last 26 weeks).
- **Level for a date:** `NO_DATA` for untracked dates. For tracked dates: 0 if the count is 0. Otherwise, with `maxCount` the largest daily count in the window, level = 1 if `count <= 0.25 * maxCount`, 2 if `<= 0.50`, 3 if `<= 0.75`, else 4.
- **Edge:** if `maxCount` is 0, all tracked dates are level 0. Levels are **relative to the window**, and the legend says so. The level scale is not comparable across windows.
- **Accessibility:** each cell exposes its date and count to screen readers; colors are never the only signal.

### M-04 Weekly and monthly activity trend
- **Formula:** per period, the sum of daily counts; compared with the previous period (M-14).
- **Missing data:** a partial current period is drawn but labeled "so far" and is excluded from insight rules.
- **Edge:** a period that starts before the first activity date is excluded from trends.

### M-05 Task completion rate
- **Counted items (all with a due date):**
  - **Done items:** completion rows whose `occurrence_date` is in the range.
  - **Open items:** open tasks whose `due_date` is in the range and on or before today.
- **Formula:** `done / (done + open_due)`.
- **Not counted:** tasks without a due date; open items due after today.
- **Known limitation (documented in the UI):** recurring tasks advance after each completion, so a missed occurrence that is never completed is not separately recorded. Gaps in a recurring series do not lower the rate. Only concrete items present in the data are counted.
- **Edge:** if the user edited a due date after completing a task, the completion keeps its original `occurrence_date`.
- **Minimum sample:** at least 5 counted items; otherwise show "3 of 4".

### M-06 Overdue percentage (snapshot)
- **Formula (as of today):** `open dated tasks with due_date < today` divided by `all open dated tasks`.
- **Period:** a point in time only. Historical overdue percentages **cannot** be reconstructed because the schema keeps no history of due-date changes, so no trend is offered.
- **Edge:** if there are no open dated tasks, show "no dated tasks".
- **Minimum sample:** at least 5 open dated tasks to show a percentage; otherwise show the count.

### M-07 Habit completion rate
For one habit over a range, take the scheduled dates in the range:
- `S` = scheduled dates in the range up to today.
- `K` = scheduled dates with state SKIPPED (excluded from the denominator).
- `D` = scheduled dates with state DONE that meet the target (see below).
- `M` = scheduled dates with state MISSED.
- `U` = scheduled dates with no entry (unrecorded).
- **Strict rate** = `D / (S − K)`. Unrecorded days count against the rate.
- **Coverage** = `(D + M + K + partials) / S`, the share of scheduled days that have any entry.
- **Target rule:** for NUMERIC or DURATION habits with a target, an entry counts as DONE only if `value >= target`; a lower value is a **partial**, which is not counted as DONE but is listed separately. For habits without a target, DONE is DONE.
- **Display:** the strict rate, plus "`U` days not recorded" whenever `U > 0`.
- **Minimum sample:** `S − K >= 7`; otherwise show "`D` of `S − K` days".
- **Edge:** FLEXIBLE habits have no scheduled dates, so no rate; show entry counts only. Dates before `start_date` or after `end_date` are not scheduled. A range with no scheduled dates shows "not scheduled in this period".

### M-08 Streaks
Over the habit's scheduled dates in chronological order up to today:
- **Counts toward a streak:** a DONE (target met) scheduled date.
- **Transparent (neither extends nor breaks):** a SKIPPED scheduled date.
- **Breaks a streak:** a MISSED or unrecorded scheduled date **earlier than today**.
- **Today grace:** if today is scheduled and has no entry yet, it does not break the streak; the streak is computed through the previous scheduled date.
- **Unscheduled days** are ignored entirely.
- **Current streak:** the number of DONE dates in the run that ends at the latest scheduled date (today excluded if unrecorded) and goes back until a break.
- **Longest streak:** the maximum run length over the habit's history under the same rules.
- **Edge:** editing a past entry recomputes both. A habit with no DONE dates has streak 0. FLEXIBLE habits have no streaks.

### M-09 Planned versus completed time
- **Planned minutes** for a range = sum of `duration_minutes` over block occurrences in the range that are not skipped. Recurring blocks are expanded to occurrences.
- **Actual minutes** = sum of `actual_minutes` over occurrences that have an actual value.
- **Comparable set** = occurrences that have an actual value. **Ratio** = `actual / planned` computed **only over the comparable set** (planned minutes of those same occurrences).
- **Coverage** = comparable occurrences divided by all non-skipped occurrences. If coverage is under 50%, label the ratio "based on N of M blocks".
- **Edge:** a midnight-crossing block belongs to its start date. Minimum sample: at least 5 comparable occurrences.

### M-10 Time allocation by category
- **Formula:** per category, the sum of planned minutes in the range; share = category minutes divided by total planned minutes. A block with no category is "Uncategorized".
- **Actual allocation** is shown only for occurrences with actuals, labeled as such.
- **Edge:** shares sum to 100% of planned minutes; if the total is 0, show "no blocks".

### M-11 Goal progress over time
- **MANUAL and NUMERIC goals:** plot the `goal_progress_entries` series as stored, with progress computed by the formula in `DATABASE_SCHEMA.md` section 7.2. A goal needs at least 2 entries to draw a line.
- **MILESTONE goals:** cumulative count of completed milestones by completion date over the **current** total. If milestones were added later, earlier points are understated; the chart notes "based on current milestone list".
- **TASKS goals:** no history exists; show only the current value.

### M-12 Custom tracker trends
- **Formula:** for numeric and duration trackers, the mean of **recorded** values per week; compared with the previous week by M-14.
- **Missing data:** days without an entry are skipped, never counted as 0.
- **Minimum sample:** at least 3 recorded days in each week being compared.

### M-13 Finance summaries (P4, reserved)
Monthly income, expenses, savings rate, category totals, and budget usage are specified when P4 starts. Required rules: integer minor units only, transfers excluded from income and spending, balances derived from transactions, and sample-data tests before UI.

### M-14 Period-over-period change
- **Counts and sums:** `change = current − previous`; percentage shown only when `previous >= 5` (counts) and the previous period is complete, as `(current − previous) / previous`, rounded to a whole percent.
- **Rates (0 to 100%):** shown as the difference in **percentage points**, never a percentage of a percentage.
- **Edge:** if `previous` is 0 or below the minimum, show the absolute change only. If the previous period starts before the first activity date, show "no earlier data".

---

## 5. Insight rules

Rules produce cards for the Analytics screen only. They never produce system notifications and never appear on the planner. Each card contains:

| Field | Content |
|---|---|
| Title | Plain statement of the observation |
| Rule | One sentence describing the rule that fired |
| Period | The exact dates evaluated |
| Evidence | The numbers used (counts, rates, comparisons) |
| Caveat | A short non-causal reminder where relevant |

Order: by rule number, then by magnitude (larger gap first). At most **5** cards shown; the rest are available under "see all". An insight is not shown if its minimum sample is not met.

Thresholds are named constants in an `AnalyticsConfig` object that tests can override. A settings screen for editing them is **deferred**; defaults below ship in v1.

| ID | Insight | Rule (all conditions must hold) | Defaults |
|---|---|---|---|
| I-01 | Activity has declined | There are at least 4 **complete** weeks of tracked data. The average weekly activity of the last 2 complete weeks is at most `(1 − drop)` times the average of the 2 complete weeks before them, and that earlier average is at least `minWeekly` | `drop = 30%`, `minWeekly = 5` events |
| I-02 | A habit is below target | Habit not archived; over the last 30 days, `S − K >= 10`; coverage at least `minCoverage`; strict rate below `lowRate` | `lowRate = 50%`, `minCoverage = 70%` |
| I-03 | Spending above usual (P4, reserved) | Category spend so far this month exceeds the average of the previous 3 complete months by more than `rise`, and the excess is at least `minAmount` | `rise = 50%`; `minAmount` set in P4 |
| I-04 | A goal is behind its pace | Goal ACTIVE, with a target date within the next `window` days and total duration (creation to target) at least 14 days; progress is lower than the linear pace by more than `tolerance` points. **Linear pace** = `elapsed days / total days`, shown only as a reference line | `window = 14` days, `tolerance = 15` points |
| I-05 | No recent activity on something you usually do | Habit with no DONE entry in the last `gapDays`, but at least 3 DONE entries in the 60 days before that gap; or the same for a time-block category | `gapDays = 21` |
| I-06 | Overdue tasks are piling up | At least `minOverdue` open tasks are overdue **and** they make up at least `share` of open dated tasks | `minOverdue = 5`, `share = 30%` |

Wording rules: "Your activity was lower in the last two weeks than in the two before" is acceptable. "You are slacking" and "because" claims are not.

Edge cases: archived habits and completed or dropped goals are skipped; with fewer data than the minimums, the rule is silent; two rules about the same habit show as one card with both pieces of evidence.

---

## 6. Trend comparisons

- Compare like with like: week with week, month with month, rolling window with the previous rolling window.
- Never compare a partial period with a complete one. A partial period may be shown with its own label.
- Use the user's week-start setting for week boundaries; changing it re-bucketing results is expected and not an error.
- Direction arrows come with the numbers; the arrow alone is never the only signal.

---

## 7. Forecasting limitations

- v1 has **no forecasting**. No "projected completion date" and no predicted spending.
- If added later: simple linear extrapolation only, labeled "estimate", shown only with at least 5 data points over at least 14 days, and never presented as a commitment.
- The goal-pace line in I-04 is a reference, not a prediction.

---

## 8. Caching and invalidation

- **No persisted analytics tables** and no background computation.
- Results are memoized in memory by `(metric id, period, parameters, data revision)`. The data revision is a counter that increments when the Room invalidation tracker reports a change to any source table (the same mechanism that sets the backup dirty flag; see `ARCHITECTURE.md`).
- The cache holds a small bounded number of entries and is cleared when the app process ends.
- Calculations run off the main thread. The Analytics screen shows a loading state and keeps its previous results while recomputing.
- The planner's small progress indicators use direct queries and do not depend on the analytics engine.
- Expected scale to verify (T-035 and P3): thousands of tasks and tens of thousands of entries compute without visible freezing. This is a target, not a measurement.

---

## 9. Data access and testability

- The engine reads data through an `AnalyticsDataSource` interface in the domain layer that returns plain lists and counts. Tests use an in-memory fake, so no database is needed for rule tests.
- A fixed `Clock` and fixed zone are required in every test.
- Metrics return typed results (`Value`, `NotEnoughData(needed, have)`, `NoData`) so the UI cannot show a number when the data is insufficient.

---

## 10. Unit test examples

Each metric has at least these cases (written as tables the tests are built from). Dates are examples.

**Streaks (M-08)** — habit scheduled Mon, Wed, Fri; today is Fri 2026-10-23.

| History | Expected current streak |
|---|---|
| Mon DONE, Wed DONE, Fri (today) unrecorded | 2 (today grace) |
| Mon DONE, Wed MISSED, Fri DONE | 1 |
| Mon DONE, Wed SKIPPED, Fri DONE | 2 (skip is transparent) |
| Tue (unscheduled) has an entry | ignored |
| Previous Fri unrecorded, then Mon, Wed DONE | 2; the unrecorded Friday breaks the older run |

**Habit rate (M-07)** — 10 scheduled days: 6 DONE, 1 SKIPPED, 1 MISSED, 2 unrecorded → `6 / (10 − 1) = 67%`, "2 days not recorded", coverage `8/10`. With only 6 scheduled days → show "4 of 5 days", no percentage.

**Completion rate (M-05)** — 4 completions with `occurrence_date` in range, 2 open tasks due in range and before today, 1 open task due tomorrow, 1 undated task → `4 / (4 + 2) = 67%`; the future and undated tasks are excluded.

**Period change (M-14)** — previous 8 events, current 12 → `+4 (+50%)`. Previous 3 events, current 6 → `+3`, no percentage. Previous rate 60%, current 72% → `+12 percentage points`.

**Heatmap (M-03)** — counts `[0, 1, 2, 3, 4]` in a window with max 4: levels `[0, 1, 2, 3, 4]` (thresholds 1, 2, 3). Max 0 → all level 0. A date before the first activity date → `NO_DATA`.

**Time ratio (M-09)** — 4 blocks, planned 60 each, 2 with actuals 45 and 75 → ratio over the comparable set `120 / 120 = 100%`, coverage 50%, labeled "based on 2 of 4 blocks"; below the minimum of 5 comparable occurrences, show minutes instead.

**Insight I-01** — weekly totals oldest to newest `[20, 18, 10, 9]` → earlier average 19, recent average 9.5, which is `<= 0.7 × 19 = 13.3`, so the rule fires. Totals `[3, 2, 1, 1]` → earlier average below `minWeekly`, silent. Only 3 complete weeks → silent.

**Insight I-04** — goal created 2026-09-01, target 2026-11-01 (61 days), today 2026-10-25 (54 days elapsed, pace 89%), progress 60% → 29 points behind, window and tolerance met → fires.

**Boundary tests** required for every metric: empty data, a single record, the first activity date, today's date, leap day, month and year boundaries, week-start Monday versus Sunday, a time-zone change, and very large histories.
