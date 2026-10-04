# Nuestro Día design system

This document defines the intended visual direction and source tokens. Authored theme, typography, shapes, and motion are present in source, but visual, contrast, accessibility, reduced-motion, and device acceptance have not been verified. Source presence alone does not mark these criteria complete.

## Visual direction

Use a warm, editorial wedding identity: cocoa text, rose-paper surfaces, terracotta actions, muted olive/gold accents, generous rounded panels, and a restrained serif display face paired with a legible sans-serif body. Make the photography the visual content; keep ornaments secondary to photos, controls, and hierarchy.

`material-theme.json` is the verified Material Theme Builder export and supplies colors only. `ejemplo.png` is a plant-care style mood-board used only for broad visual cues such as dark cocoa, rounded raised panels, serif display, and clay/coral actions. Do not copy its plant-care subject matter, assets, or literal UI into the wedding app.

## Color source tokens

The values below are copied from the `light` and `dark` schemes in the JSON export and are used by the authored theme. They are not proof that rendered text/icon pairs meet contrast requirements. Validate actual pairs during visual QA.

| Material role | Light | Dark |
|---|---|---|
| `primary` / `onPrimary` | `#8F4C38` / `#FFFFFF` | `#FFB5A0` / `#561F0F` |
| `primaryContainer` / `onPrimaryContainer` | `#FFDBD1` / `#723523` | `#723523` / `#FFDBD1` |
| `secondary` / `onSecondary` | `#77574E` / `#FFFFFF` | `#E7BDB2` / `#442A22` |
| `secondaryContainer` / `onSecondaryContainer` | `#FFDBD1` / `#5D4037` | `#5D4037` / `#FFDBD1` |
| `tertiary` / `onTertiary` | `#6C5D2F` / `#FFFFFF` | `#D8C58D` / `#3B2F05` |
| `tertiaryContainer` / `onTertiaryContainer` | `#F5E1A7` / `#534619` | `#534619` / `#F5E1A7` |
| `error` / `onError` | `#BA1A1A` / `#FFFFFF` | `#FFB4AB` / `#690005` |
| `errorContainer` / `onErrorContainer` | `#FFDAD6` / `#93000A` | `#93000A` / `#FFDAD6` |
| `background` / `onBackground` | `#FFF8F6` / `#231917` | `#1A110F` / `#F1DFDA` |
| `surface` / `onSurface` | `#FFF8F6` / `#231917` | `#1A110F` / `#F1DFDA` |
| `surfaceVariant` / `onSurfaceVariant` | `#F5DED8` / `#53433F` | `#53433F` / `#D8C2BC` |
| `outline` / `outlineVariant` | `#85736E` / `#D8C2BC` | `#A08C87` / `#53433F` |
| `surfaceContainerLowest` | `#FFFFFF` | `#140C0A` |
| `surfaceContainerLow` | `#FFF1ED` | `#231917` |
| `surfaceContainer` | `#FCEAE5` | `#271D1B` |
| `surfaceContainerHigh` | `#F7E4E0` | `#322825` |
| `surfaceContainerHighest` | `#F1DFDA` | `#3D322F` |
| `inverseSurface` / `inverseOnSurface` | `#392E2B` / `#FFEDE8` | `#F1DFDA` / `#392E2B` |
| `inversePrimary` | `#FFB5A0` | `#8F4C38` |

### Proposed semantic aliases

| Alias | Source role | Status |
|---|---|---|
| `BrandPrimary` | `primary` | Source mapping; rendered use unverified |
| `BrandPrimaryContainer` | `primaryContainer` | Source mapping; rendered use unverified |
| `BrandAccent` | `tertiary` | Source mapping; rendered use unverified |
| `Canvas` | `background` | Source mapping; rendered use unverified |
| `RaisedSurface` | `surfaceContainerLow` or `surfaceContainer` | Source mapping; rendered use unverified |
| `Danger` | `error` | Source mapping; rendered use unverified |

Keep color roles semantic; avoid hard-coded colors inside screens. `Theme.kt` disables dynamic color in source; device rendering has not been checked.

## Proposed shape, type, icon, and component tokens

| System | Proposed direction | Status |
|---|---|---|
| Spacing | 4 dp micro-grid and 8 dp primary rhythm; use 16/24/32 dp for increasing separation. | Proposed |
| Shapes | 12 dp compact controls, 16 dp fields, 20 dp buttons, 28–32 dp hero/cards, pill only for small status/filter chips. | Proposed |
| Display type | Platform serif fallback for short emotional headings; moderate size and weight, never use script fonts for essential copy. | Proposed |
| Body type | Platform sans-serif for forms, terms, timestamps, and longer gallery captions; support system font scaling. | Proposed |
| Iconography | Material Symbols/Material icons with consistent rounded geometry and semantic labels; no emoji as icons or copy. | Proposed |
| Buttons | Primary filled action, secondary outlined/text action, 48 dp minimum touch target, unambiguous loading/disabled feedback. | Proposed |
| Fields | Persistent labels, appropriate email/password keyboard, inline error/help text, visible password reveal control with accessible state. | Proposed |
| Photo surfaces | Preserve image ratio in gallery; keep metadata/action affordances visually subordinate to the image. | Proposed |
| Elevation | Prefer tonal surface layers and subtle shadows; reserve stronger elevation for focused sheets/dialogs. | Proposed |

Use stable Material 3 components already declared by the app; this direction does not require adopting an alpha-only expressive theme API. The design is “Expressive-inspired” through authored hierarchy, shape, color, and interaction tokens rather than a dependency claim.

## Motion and interaction

| Interaction | Authored source direction | Acceptance status |
|---|---|---|
| Screen / Form entry | Compose visibility and spring animation helpers. | Runtime/reduced-motion behavior unverified. |
| Press / Touch feedback | Authored press interaction helper. | Device response, focus visibility, and TV behavior unverified. |
| Loading / Progress | Authored progress UI and motion helper. | Announcement and assistive-technology behavior unverified. |
| Layout adjustments | Authored content-size animation helper. | Large text and reduced-motion behavior unverified. |
| Shuffle banner | Authored gallery inactivity interaction. | Timing, stability, accessibility, and TV behavior unverified. |
| TV ambient slideshow | Authored slideshow motion and crossfade. | Landscape lock, timing, reduced-motion, and device behavior unverified. |

All motion utilizes native Jetpack Compose animation APIs (`animateFloatAsState`, `spring`, `graphicsLayer`, `Crossfade`, `AnimatedVisibility`) adhering to Ponytail guidelines.

## Visual acceptance checklist

- [ ] Verify light/dark rendered text and controls meet contrast requirements.
- [ ] Verify large font scale, narrow phones, landscape, and TV viewing distance.
- [ ] Verify focus, pressed, disabled, loading, error, and selected states, including TalkBack labels and announcements.
- [ ] Verify motion and reduced-motion behavior on devices; authored spring/ambient motion is not acceptance evidence.
- [ ] Verify final UI contains no emoji or copied mood-board assets.
