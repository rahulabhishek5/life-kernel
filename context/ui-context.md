# UI Context

| | |
|---|---|
| **Version** | 0.1 (Draft) |
| **Role** | Single source of truth for design token **values**. `design-system.md` explains how they are used. If the two ever disagree, this file wins. |

All UI code must use these tokens. **No hardcoded colors, spacing, radii, or text sizes in feature code.** Hex values below were calculated and, for the background, confirmed on a physical device; the other values still need on-device review.; contrast ratios were calculated, but final values must be reviewed on a real AMOLED device during implementation.

---

## Theme

Dark only. There is no light mode in v1.

The visual language is an AMOLED-black technical workspace with warm neo-brutalist components: near-black backgrounds, crisp outlines, compact cards, tactile controls, and one vivid accent color that the user can change. Character comes from borders, typography, layout, and the accent, not from gradients, blur, glass effects, or large rounded surfaces.

Default accent: **Lime / Green**.

---

## Colors

Compose token names are shown; they map to a `PosColors` object provided through a `CompositionLocal` and mirrored into the Material 3 `ColorScheme` so Material components pick them up.

### Foundation (the same in every palette)

| Role | Token | Value | Contrast vs `background` |
|---|---|---|---|
| Page background | `background` | `#000000` | Confirmed on the physical device (DQ-05). Update this cell if the code uses a different value |
| Raised surface (cards) | `surface` | `#101010` | n/a |
| Secondary surface (inputs, sheets, chips) | `surfaceVariant` | `#171717` | n/a |
| Primary text | `textPrimary` | `#F5F5F5` | 19.3:1 |
| Muted text | `textMuted` | `#A3A3A3` | 8.3:1 |
| Disabled content | `textDisabled` | `#6B6B6B` | 3.9:1 (disabled, exempt from contrast rules) |
| Divider | `divider` | `#303030` | 1.6:1 (decorative only) |
| Subtle outline (passive cards, list rows) | `outlineSubtle` | `#3D3D3D` | 1.9:1 (decorative only) |
| Strong outline (inputs, unselected controls, focus-adjacent) | `outlineStrong` | `#8A8A8A` | 6.1:1 |
| Scrim (behind drawer/sheets/dialogs) | `scrim` | `#000000` at 60% alpha | n/a |

Rule: anything the user must identify as a control (text field boundary, checkbox box, unselected chip) uses `outlineStrong`. `outlineSubtle` and `divider` are for passive structure only.

### State colors (the same in every palette)

| Role | Token | Value | Contrast vs `background` |
|---|---|---|---|
| Error | `stateError` | `#FF5A5F` | 6.9:1 |
| Warning | `stateWarning` | `#FFB020` | 11.5:1 |
| Success | `stateSuccess` | `#34D399` | 10.9:1 |
| Info | `stateInfo` | `#60A5FA` | 8.3:1 |

State colors always appear with an icon or text label. They can resemble the active accent (for example, Red accent versus error, Lime accent versus success), so color alone must never carry meaning.

### Accent palettes

Each palette defines only these tokens. Everything else comes from the foundation.

| Palette (id) | `accent` | `onAccent` | Contrast `accent` vs `background` | Contrast `onAccent` vs `accent` |
|---|---|---|---|---|
| Lime / Green (`lime`, **default**) | `#A3E635` | `#000000` | 13.9:1 | 13.9:1 |
| Orange (`orange`) | `#FB923C` | `#000000` | 9.3:1 | 9.3:1 |
| Red (`red`) | `#F87171` | `#000000` | 7.6:1 | 7.6:1 |
| Blue (`blue`) | `#60A5FA` | `#000000` | 8.3:1 | 8.3:1 |
| Purple (`purple`) | `#A78BFA` | `#000000` | 7.7:1 | 7.7:1 |
| Warm Amber (`amber`) | `#FBBF24` | `#000000` | 12.6:1 | 12.6:1 |
| Neutral Mono (`mono`) | `#E5E5E5` | `#000000` | 16.7:1 | 16.7:1 |

Derived accent tokens (computed in code from `accent`, never hand-set per palette):

| Token | Derivation | Used for |
|---|---|---|
| `accentSubtle` | `accent` at 16% alpha | Selected drawer item background, selected chip fill, completed-row tint |
| `accentPressed` | `accent` at 80% alpha over `surface` | Pressed state of accent-filled controls |
| `heat1`..`heat4` | `accent` at 25%, 45%, 70%, 100% alpha over `background` | Calendar and habit heatmap levels (P3); level 0 is `surfaceVariant` |

Charts (P3): series use `accent`, then `textMuted`, then `outlineStrong`. Multi-category charts must be directly labeled; a fixed categorical set independent of the accent is decided in P3.

The palette `id` string is what is persisted in DataStore. An unknown id falls back to `lime`.

---

## Typography

System fonts only; no bundled font files in v1.

| Role | Font | Token |
|---|---|---|
| UI text | System default sans (`FontFamily.Default`) | `PosFonts.sans` |
| Dates, statistics, technical labels | System monospace (`FontFamily.Monospace`) | `PosFonts.mono` |

Monospace is used selectively (dates, counters, durations, small labels), never for body text or long reading.

| Style | Size / line height | Weight | Font | Use |
|---|---|---|---|---|
| `headline` | 24sp / 30sp | Bold | sans | Screen titles, large numbers |
| `title` | 18sp / 24sp | SemiBold | sans | Card titles, section headers, dialog titles |
| `body` | 16sp / 24sp | Normal | sans | Task titles, notes, inputs |
| `bodySmall` | 14sp / 20sp | Normal | sans | Secondary text, descriptions |
| `label` | 12sp / 16sp | Medium | sans | Chips, captions, buttons (small) |
| `data` | 14sp / 20sp | Medium | mono | Dates, times, counts, durations |
| `dataLarge` | 20sp / 26sp | Bold | mono | Progress numbers, streak counts |

Minimum text size is 12sp. All sizes use `sp` and must survive large system font scales without clipping (layouts wrap or scroll).

---

## Spacing

4dp base grid. Use only these steps.

| Token | Value |
|---|---|
| `space1` | 4dp |
| `space2` | 8dp |
| `space3` | 12dp |
| `space4` | 16dp (default screen horizontal padding, card padding) |
| `space6` | 24dp |
| `space8` | 32dp |
| `space12` | 48dp |

Minimum touch target: **48dp x 48dp** for every interactive element, even if the visible control is smaller.

---

## Border Radius

Small, consistent radii. Large pill or circular surfaces are avoided.

| Context | Token | Value |
|---|---|---|
| Checkboxes, chips, small tags | `radiusSmall` | 4dp |
| Buttons, inputs, cards, list rows | `radiusMedium` | 6dp |
| Sheets, dialogs, drawer panel edge, hero cards | `radiusLarge` | 8dp |

## Borders and offset effect

| Token | Value | Use |
|---|---|---|
| `borderHairline` | 1dp | Dividers, passive list rows |
| `borderStandard` | 1.5dp | Cards, inputs, buttons, checkboxes |
| `borderStrong` | 2dp | Selected or focused cards, primary buttons, focus indication |
| `offsetShadow` | 3dp x, 3dp y, hard edge, no blur | Neo-brutalist depth |

The offset effect is a solid, unblurred block drawn behind a component (cheap to render). It is used **only** on: primary buttons, the quick-add button, and the currently selected card. Color is `accent`. On press, the component translates by the offset and the block disappears (tactile press). Passive cards have an outline and no offset.

---

## Motion

Durations (all interruptible, built-in Compose animation only):

| Token | Value | Use |
|---|---|---|
| `motionFast` | 100ms | Press feedback, checkbox toggle |
| `motionStandard` | 160ms | Expand/collapse, filter changes, theme color change, banners |
| `motionEmphasized` | 220ms | Sheets, drawer, screen transitions, task completion transition |

When reduced motion is on (`LocalReducedMotion` is true), transitions become instant (`snap`), and no animation is required to understand state.

---

## Component Library

Jetpack Compose with Material 3 as the behavioral base, wrapped by our own design-system components in `core/designsystem/`. Feature code uses these wrappers, not raw Material components, for anything styled.

| Component | Notes |
|---|---|
| `PosTheme` | Provides `PosColors`, typography, spacing, radii, motion, and the Material `ColorScheme` mapping |
| `PosButton` (primary, secondary, text) | Primary: accent fill, `onAccent` text, offset effect. Secondary: outline. |
| `PosCard` | `surface`, `borderStandard` in `outlineSubtle`, `radiusMedium`; selected variant uses `accent` border and offset |
| `PosTextField` | `surfaceVariant`, `outlineStrong` border, `accent` border when focused |
| `PosCheckbox` | 4dp radius square, `outlineStrong` unchecked, accent fill when checked, animated check |
| `PosChip` | Filters and tags |
| `PosSegmentedToggle` | Tasks / Timetable switch |
| `PosSheet`, `PosDialog` | Modal bottom sheet and dialog |
| `PosTopBar`, `PosDrawer` | Hamburger top bar and modal navigation drawer |
| `PosBanner` | Backup nudge and other inline messages (icon + text + optional action + dismiss) |
| `PosEmptyState`, `PosErrorState` | Standard empty and error layouts |
| `PosProgressBar` | Linear progress with short animation |

Add a new component to the design system before using the same styling in two places.

---

## Layout Patterns

- **Screen scaffold:** top bar (hamburger, title, optional actions), content, and a quick-add button where relevant. Content respects system insets.
- **Planner:** segmented toggle (Tasks / Timetable) under the top bar; conditional banner; compact progress and Now/Next card; sectioned list (Overdue, Today, Done collapsed).
- **Lists:** `LazyColumn` with stable keys; rows are compact bordered cards or divider-separated rows; section headers in `label` mono caps.
- **Forms:** simple creation in a modal bottom sheet or dialog; complex records on a dedicated screen. The primary action stays reachable above the keyboard.
- **Drawer:** modal navigation drawer from the left, grouped entries with dividers; selected entry uses `accentSubtle` plus an accent edge bar.
- **Dialogs:** only for confirmations and short inputs; destructive actions name what will be deleted.
- **Phone-first:** single column; no tablet-specific layouts in v1.
- **Edge-to-edge:** the black background extends under system bars; insets padding keeps content clear.

---

## Icons

- Stroke-style icons, consistent weight, drawn from a **curated set of vector drawables** stored in `res/drawable` (or a very small selection from the core Material icon set).
- Do **not** add the extended Material icons dependency without measuring its effect on release size.
- Sizes: 20dp inline, 24dp in buttons and navigation, with a 48dp touch target around any tappable icon.
- Icons carry `contentDescription` when they convey meaning, and `null` when purely decorative.
- Resource icons (user-chosen per resource) use a fixed set defined in P2.
