# Life Kernel — Design System

| | |
|---|---|
| **Version** | 0.1 (Draft, derived from PRD v0.1) |
| **Status** | Documentation phase. Nothing here has been built or tested on a device. |
| **Token values** | Defined in `ui-context.md` (single source of truth). This file explains how to use them. |

---

## 1. Design principles

1. **Daily-use first.** The planner is the heart of the app; every pattern is judged by how fast it makes a daily task.
2. **Black, crisp, deliberate.** AMOLED black foundation, strong outlines, clear hierarchy. Character from typography, borders, layout, and one accent.
3. **Quiet by default.** No decorative motion, gradients, blur, or glass effects. Information density is high but calm.
4. **Immediate.** Every tap gives feedback within one frame; saves never wait on animation.
5. **Honest.** The UI never overstates protection (the app lock is a lock, not encryption) and never hides that external links need internet.
6. **Accessible without extras.** The app must be fully usable with animations off, at large font scale, and without color perception.
7. **One system.** One component set and one token set; palettes change colors only, never layout or behavior.

---

## 2. Visual foundation

- **Background:** `#000000`. Cards on `#101010`, inputs and sheets on `#171717`. The default look is predominantly black.
- **Why not off-white neo-brutalism:** classic neo-brutalist styling relies on light backgrounds with black outlines and black shadows. On true black those shadows vanish, so the offset effect is drawn in the accent color and outlines are light-gray. Depth comes from borders and the accent offset, not from shading.
- **Corners:** 4/6/8dp only. No large rounded surfaces.
- **Accent:** one color at a time, used sparingly for actions, selection, progress, and completion.

---

## 3. Warm neo-brutalist component language

| Trait | Implementation |
|---|---|
| Strong boundaries | 1.5dp outlines on cards and controls; 2dp on selected/primary |
| Tactile depth | 3dp hard offset block in accent on primary button, quick-add button, selected card only |
| Press feedback | Component translates by the offset and the offset block disappears (about 100 ms) |
| Compactness | 16dp card padding, 8–12dp between items, no nested cards |
| Typography as structure | Mono caps labels for sections and metadata; bold sans for titles |
| Warmth | Accent hues skew warm by default (lime and amber lean yellow-green and warm); soft off-white text `#F5F5F5` instead of pure white |

Avoid: nested cards, gradients, blur, drop-shadow blur, glass effects, more than one offset component per card, decorative icons that add no information.

---

## 4. Accent palettes and theme architecture

Palette values, derived tokens, and contrast figures are in `ui-context.md`. Seven palettes: Lime/Green (default), Orange, Red, Blue, Purple, Warm Amber, Neutral Mono.

**Contrast check (calculated, WCAG ratios against `#000000` / `#101010` / `#171717`):**

| Palette | Accent vs black | vs surface | vs surfaceVariant | Black text on accent |
|---|---|---|---|---|
| Lime | 13.9 | 12.6 | 11.9 | 13.9 |
| Orange | 9.3 | 8.4 | 7.9 | 9.3 |
| Red | 7.6 | 6.9 | 6.5 | 7.6 |
| Blue | 8.3 | 7.5 | 7.1 | 8.3 |
| Purple | 7.7 | 7.0 | 6.6 | 7.7 |
| Amber | 12.6 | 11.4 | 10.7 | 12.6 |
| Mono | 16.7 | 15.1 | 14.2 | 16.7 |

All exceed the 4.5:1 text and 3:1 non-text guidelines. These are calculations, not device measurements; AMOLED panels, brightness, and user display settings can change perceived contrast, so palettes are reviewed on a real device.

### Architecture

```
AccentPalette(id, displayName, accent, onAccent)      // pure data, one entry per palette
AccentPalettes.all: List<AccentPalette>               // registry; first = default
PosColors(foundation + state + accent + derived)      // built from palette
PosTheme(palette) { ... }                             // provides PosColors, type, spacing, shapes, motion
                                                      // and maps PosColors -> Material ColorScheme
```

- The selected palette id is stored in DataStore and read at launch; an unknown id falls back to the default.
- Theme change animates only a handful of tokens (`accent`, `accentSubtle`, `accentPressed`) once, inside `PosTheme`, using the `motionStandard` duration. Individual components do not animate their own color changes.
- Components read colors only through `PosColors` and never reference a palette.

### Adding a new palette (no feature changes)

1. Add one `AccentPalette` entry to the registry with an id, display name, accent, and onAccent.
2. Run the palette contrast test (accent vs background and surfaces, onAccent vs accent; fails below the thresholds above).
3. The settings selector, charts, heatmaps, and every component pick it up automatically.

---

## 5. Typography, spacing, borders, radii

See `ui-context.md` for exact values. Usage rules:

- **Sans** for everything a person reads: titles, tasks, notes, descriptions.
- **Mono** for dates, times, durations, counters, section labels, and streak numbers. Never for paragraphs or note bodies.
- **Hierarchy:** one `headline` per screen at most; `title` for cards and sections; `body` for content; `bodySmall` and `label` for secondary information.
- **Spacing:** 4dp grid; 16dp screen padding; 8dp between related items; 24dp between sections.
- **Line length:** note bodies and descriptions are capped to a comfortable measure on larger phones.

---

## 6. Components

### Cards (`PosCard`)
`surface` fill, 1.5dp `outlineSubtle` border, 6dp radius, 16dp padding. Selected: 2dp `accent` border plus accent offset block. Pressed: translates by offset. Cards never nest inside cards; use dividers and spacing within a card.

### Buttons (`PosButton`)
- **Primary:** accent fill, `onAccent` label, 2dp accent border, offset block. One per screen region.
- **Secondary:** transparent with 1.5dp `outlineStrong` border and `textPrimary` label.
- **Text:** label only, accent color, 48dp target.
- **Destructive:** secondary style with `stateError` outline and label plus a trash icon; always confirmed.
- States: enabled, pressed (translate, no offset), focused (2dp accent ring), disabled (`textDisabled`, no offset).

### Inputs (`PosTextField`)
`surfaceVariant` fill, 1.5dp `outlineStrong` border, 6dp radius, label above (not placeholder-only). Focused: accent border. Error: `stateError` border plus an icon and message below. Inputs show a visible clear action when useful.

### Checkboxes (`PosCheckbox`)
24dp square visible box inside a 48dp target, 4dp radius, `outlineStrong` border unchecked. Checked: accent fill with `onAccent` check mark. Animation: fill and check draw over `motionFast`.

### Chips (`PosChip`)
4dp radius, `outlineStrong` border; selected chips use `accentSubtle` fill plus accent border and text. Used for filters, tags, priority.

### Segmented toggle (`PosSegmentedToggle`)
Two or three segments; selected segment accent-filled with `onAccent`. Used for Tasks / Timetable.

### Drawer (`PosDrawer`)
Modal, slides from the left over `motionEmphasized`, scrim `#000000` at 60%. Width about 80% of the screen up to a sensible cap. Grouped entries with dividers. Selected entry: `accentSubtle` background and a 3dp accent edge bar, label in `textPrimary`.

### Dialogs (`PosDialog`)
Confirmations and very short inputs only. Title, short body, two actions (cancel left, confirm right). Destructive dialogs name the thing being deleted and its consequences.

### Sheets (`PosSheet`)
Modal bottom sheet for simple record creation (quick-add task, quick note, habit entry). Drag handle, 8dp top radius, content respects keyboard insets. Complex forms use full screens instead.

### Banners (`PosBanner`)
Inline strip with icon, one or two lines of text, optional action, dismiss. Used for the backup nudge. Not a toast and not a system notification. Appears and disappears over `motionStandard`.

### Progress (`PosProgressBar`)
Thin linear bar, `surfaceVariant` track, `accent` fill, animates over `motionStandard` on change. Always accompanied by a number or label.

---

## 7. Navigation behavior

- Hamburger button opens a modal drawer; there is no persistent bottom bar.
- Drawer groups (dividers between groups): **Daily** (Home/Today, Calendar, Tasks, Timetable), **Track** (Habits, Goals and Projects), **Capture** (Notes and Journal, Resources), **Insight** (Analytics), then **Settings**. Entries for unbuilt phases are not shown.
- App opens on Home/Today with the task view (user-configurable default).
- Selecting a destination closes the drawer, then navigates with state saved and restored.
- Back: drawer closes first; then back stack pops; from a top-level destination back returns to Home, then exits.
- The quick-add button appears on the planner and on list screens where it makes sense.

---

## 8. Wireframes

Textual wireframes. Dimensions are indicative; the design tokens decide actual values.

### 8.1 Planner — Tasks view (home)

```
┌────────────────────────────────────────┐
│ ☰   Today                        ⋮     │
│     SAT · 24 OCT 2026          (mono)  │
│ ┌────────────────────────────────────┐ │
│ │ [ Tasks ]        Timetable         │ │  segmented toggle
│ └────────────────────────────────────┘ │
│ ┌ ⚠ Last backup 7 days ago ─────────┐ │  conditional banner
│ │ Data changed since.  [BACK UP]  ✕ │ │
│ └────────────────────────────────────┘ │
│ ┌────────────────────────────────────┐ │
│ │ 3 / 7 DONE   ▓▓▓▓░░░░░░░           │ │  progress card
│ │ NOW   Study block       09:00–10:30│ │
│ │ NEXT  Gym               18:00      │ │
│ └────────────────────────────────────┘ │
│ OVERDUE (2)                            │
│ ☐ Submit lab record     !!   OCT 22    │
│ ☐ Pay club fee          !    OCT 23    │
│ TODAY (5)                              │
│ ☐ Revise notes          !!   today     │
│ ☐ Email professor            today     │
│ ▸ DONE (2)                             │  collapsed
│                                  ┌───┐ │
│                                  │ + │ │  quick-add (offset block)
│                                  └───┘ │
└────────────────────────────────────────┘
```

### 8.2 Planner — Timetable view

```
┌────────────────────────────────────────┐
│ ☰   Today                        ⋮     │
│ ┌────────────────────────────────────┐ │
│ │   Tasks        [ Timetable ]       │ │
│ └────────────────────────────────────┘ │
│ ‹  SAT 24 OCT  ›              [TODAY]  │
│ 08 ┤                                   │
│ 09 ┤ ┌────────────────────────────────┐│
│    │ │ Study block        09:00–10:30 ││  ← NOW highlight (accent border)
│ 10 ┤ │ planned 90m · actual 75m       ││
│    │ └────────────────────────────────┘│
│ 11 ┤                                   │
│ 12 ┤ ┌──────────────────┐ ┌───────────┐│
│    │ │ Lunch  12:00     │ │ Call  ⚠   ││  overlap marked with ⚠ + label
│    │ └──────────────────┘ └───────────┘│
│ 18 ┤ ┌────────────────────────────────┐│
│    │ │ Gym                18:00–19:00 ││
│    │ └────────────────────────────────┘│
│                                  [ + ] │
└────────────────────────────────────────┘
```

### 8.3 Navigation drawer

```
┌──────────────────────┐▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
│ Life Kernel          │▒ (scrim 60%) ▒▒
│ v0.1                 │▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
│──────────────────────│
│ ▌ Home / Today       │  selected: accentSubtle + edge bar
│   Calendar           │
│   Tasks              │
│   Timetable          │
│──────────────────────│
│   Habits             │
│   Goals & Projects   │
│──────────────────────│
│   Notes & Journal    │
│   Resources          │
│──────────────────────│
│   Analytics          │
│──────────────────────│
│   Settings           │
└──────────────────────┘
```

### 8.4 Quick-add sheet

```
┌────────────────────────────────────────┐
│               ───                      │
│ NEW TASK                               │
│ ┌────────────────────────────────────┐ │
│ │ Title                              │ │
│ └────────────────────────────────────┘ │
│ [Today] [Tomorrow] [Pick date]         │
│ [!] [!!] [!!!]                         │
│ [ More details › ]            [ SAVE ] │
└────────────────────────────────────────┘
```
Switching type (task, note, habit, resource) happens through a small chip row at the top of the sheet in the phases where those modules exist.

### 8.5 Resource library (P2)

```
┌────────────────────────────────────────┐
│ ☰   Resources                    🔍    │
│ [All] [Learning] [Tools] [Videos] [+]  │  collections as chips
│ ┌────────────────────────────────────┐ │
│ │ ▣ Kotlin Compose docs        ★     │ │  icon tinted with resource color
│ │ developer.android.com   [doc]      │ │
│ │ #android #learning                 │ │
│ ├────────────────────────────────────┤ │
│ │ ▣ DSA playlist                     │ │
│ │ youtube.com             [video]    │ │
│ └────────────────────────────────────┘ │
│  Opening a link needs internet.        │  (shown once, dismissible)
│                                  [ + ] │
└────────────────────────────────────────┘
```
Long-press or overflow on a row: open, copy URL, edit, archive, delete.

### 8.6 Calendar (P1) and analytics (P3)

```
Month view (P1)                         Analytics (P3)
┌──────────────────────────────┐        ┌──────────────────────────────┐
│ ☰  Calendar   [M] W D        │        │ ☰  Analytics   [Week][Month] │
│ ‹  OCTOBER 2026  ›  [TODAY]  │        │ ACTIVITY HEATMAP             │
│ M  T  W  T  F  S  S          │        │ ░▒▓█░▒▓ ░▒▓█░▒▓ ...          │
│ 19 20 21 22 23 24• 25        │        │ TASK COMPLETION  72% ▲ 6pts  │
│       ·  ·  ··  [24]         │        │ vs previous week             │
│ dots = items on that day     │        │ INSIGHTS                     │
│ selected day: accent border  │        │ ⓘ Habit "Read" 40% this month│
│ ─────────────────────────────│        │   Why: 8 of 20 scheduled days│
│ 24 OCT: 3 tasks, 2 blocks    │        │   Period: 1–24 Oct           │
└──────────────────────────────┘        └──────────────────────────────┘
```
Every insight card shows what rule fired, the calculation period, and the supporting numbers.

### 8.5b Finance (P4)
Not designed yet. The finance wireframes are written when P4 starts, so nothing in P1–P3 depends on them.

---

## 9. Empty states and error states

Every empty state has: a short mono label, one sentence of guidance, and one primary action. No illustrations are required.

| Screen | Empty state |
|---|---|
| Today, no tasks | "NOTHING PLANNED" — "Add your first task for today." [Add task] |
| Timetable, no blocks | "NO BLOCKS TODAY" — "Plan your day with time blocks, or stay task-only." [Add block] |
| Search with no results | "NO MATCHES" — shows the query and a clear-search action |
| Habits / Goals / Notes / Resources | Module-specific one-liner and [Create] |
| Analytics, insufficient data | "NOT ENOUGH DATA YET" — states how much more data is needed |

| Error | Behavior |
|---|---|
| Invalid input | Inline under the field with an icon, never only a color change |
| Backup destination folder not accessible | Banner on the backup screen with [Choose folder] |
| Backup file corrupt or incompatible | Dialog stating nothing was changed |
| Wrong backup password | Inline error, retry allowed, no data touched |
| Low storage | Message that the operation was not completed and nothing was lost |
| Unexpected error | Generic message with retry; no technical details with user content |

---

## 10. Accessibility

- **Touch targets:** at least 48dp x 48dp for every interactive element, with spacing so adjacent targets are not mis-tapped.
- **Contrast:** text and meaningful controls meet at least 4.5:1 (text) and 3:1 (control boundaries) in every palette. Passive decoration (dividers, subtle outlines) is exempt and never carries meaning.
- **Not color alone:** state is shown with icon plus text (errors, warnings, selected, completed, overlaps).
- **Screen readers:** meaningful icons have descriptions; decorative ones are hidden from accessibility; custom checkboxes, toggles, and heatmap cells expose role, state, and label (for example "Submit lab record, task, not completed").
- **Font scale:** layouts wrap or scroll at large scales; nothing is fixed-height around text.
- **Focus order:** follows visual order; sheets and dialogs trap focus and return it on close.
- **Motion:** respects reduced motion (see section 11).
- **Verification:** an accessibility pass is part of every screen's definition of done and is listed in `TEST_PLAN.md`.

---

## 11. Animation and interaction specification

All animations use built-in Compose APIs, are interruptible, never block a save, and stay between 100 and 220 ms. No looping or decorative motion anywhere.

| Interaction | What animates | Duration | Easing | Reduced-motion behavior |
|---|---|---|---|---|
| Button / card press | Translate by offset, remove offset block | 100 ms (`motionFast`) | Ease-out | Instant state change |
| Checkbox toggle | Fill and check mark | 100 ms | Ease-out | Instant |
| Task completion | Row dims and strikes through, then moves to Done | 220 ms (`motionEmphasized`) | Ease-in-out | Instant move; state shown by text and icon |
| Section expand/collapse | Height and chevron rotation | 160 ms (`motionStandard`) | Ease-in-out | Instant |
| Drawer open/close | Slide and scrim fade | 220 ms | Ease-in-out | Instant |
| Sheet / dialog | Slide up / fade in | 220 ms | Ease-out | Instant |
| Screen transition | Short fade between top-level destinations | 160 ms | Ease-in-out | No animation |
| Progress change | Bar fill | 160 ms | Ease-out | Instant |
| Banner show/hide | Height and fade | 160 ms | Ease-in-out | Instant |
| Theme change | `accent`, `accentSubtle`, `accentPressed` only | 160 ms | Linear | Instant |
| Calendar view / filter switch | Content cross-fade | 160 ms | Ease-in-out | Instant |
| Save confirmation | Brief accent check in the save button or snackbar | 160 ms | Ease-out | Instant |

**Reduced motion:** a single `LocalReducedMotion` value is provided at the root. It is true when the system animation scale is off. How Android exposes this reliably across API 26 and newer is a verification task on real devices. When true, every transition above becomes an immediate state change. The app must remain pleasant and unambiguous with all non-essential animation off.

**Interaction rules:**
- Data is written immediately on user action; animation never delays persistence.
- Rapid taps interrupt and retarget animations rather than queue them.
- Only the changed component animates; lists never animate every item at once.
- Haptics are not used in v1.

---

## 12. Mobile layout rules

- Phone portrait is the only supported layout in v1; landscape must work without breakage but is not optimized.
- Single column; 16dp horizontal padding.
- Primary action reachable by thumb: quick-add lives bottom-right, and sheet actions sit at the bottom.
- Keyboard: forms scroll so the focused field and primary action remain visible.
- Lists are lazy; section headers can stick on long lists.
- System bar insets are respected; the black background extends edge to edge.
- Support system font scale and display size without clipping.
- Screen content is in a consistent order: title, controls (toggle/filters), banner, summary, list, quick-add.

---

## 13. Open design questions

| ID | Question | Resolve |
|---|---|---|
| DQ-01 | Resolved: priorities None/Low/Medium/High, marks `!`, `!!`, `!!!` plus a text label for screen readers | Done |
| DQ-02 | Resolved: keep last 5 backups; banner snooze 24 hours | Done |
| DQ-03 | Categorical (non-accent) chart colors for multi-category analytics | Start of P3 |
| DQ-04 | Resource icon set (size of curated list) | Start of P2 |
| DQ-05 | Resolved by checking on the physical device (pure AMOLED black); confirm the hex in `ui-context.md` matches the code | Done |