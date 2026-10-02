# Nuestro Día design system

This is a proposed design direction, not a report of implemented UI. The current app still uses the starter purple theme and Android dynamic colors. Promote a token or component to “implemented” only after source and visual review confirm it.

## Visual direction

Use a warm, editorial wedding identity: cocoa text, rose-paper surfaces, terracotta actions, muted olive/gold accents, generous rounded panels, and a restrained serif display face paired with a legible sans-serif body. Make the photography the visual content; keep ornaments secondary to photos, controls, and hierarchy.

`/home/jack/proyectos/material-theme.json` is the verified Material Theme Builder export and supplies colors only. `/home/jack/proyectos/ejemplo.png` is a plant-care style mood-board used only for broad visual cues such as dark cocoa, rounded raised panels, serif display, and clay/coral actions. Do not copy its plant-care subject matter, assets, or literal UI into the wedding app.

## Color source tokens

The values below are copied from the `light` and `dark` schemes in the JSON export. They are source values, not proof that current UI components meet contrast requirements. Validate actual text/icon pairs during implementation and visual QA.

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
| `BrandPrimary` | `primary` | Proposed |
| `BrandPrimaryContainer` | `primaryContainer` | Proposed |
| `BrandAccent` | `tertiary` | Proposed |
| `Canvas` | `background` | Proposed |
| `RaisedSurface` | `surfaceContainerLow` or `surfaceContainer` | Proposed; choose by contrast and elevation role |
| `Danger` | `error` | Proposed |

Keep color roles semantic; avoid hard-coded colors inside screens. Dynamic wallpaper color should not override the authored brand palette. The current `Theme.kt` still defaults `dynamicColor` to true, so that recommendation is not implemented yet.

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

| Interaction | Proposed behavior | Reduced-motion and TV behavior |
|---|---|---|
| Screen change | Short 120–180 ms fade/size transition; do not bounce screens into place. | Disable or replace with immediate state change when system animator duration scale is zero/reduced. |
| Press/focus | Small, non-overshooting surface/tint response; maintain a visible focus ring. | No scale animation on TV; focus state remains clear without motion. |
| Loading | Stable inline progress indicator with status text; do not pulse the whole card. | Static progress/announceable status when animation is reduced. |
| Gallery update | Insert new item without reordering the row under active touch/focus. | Do not auto-scroll or auto-shuffle a TV/projection view. |
| Inactivity shuffle | Only after the product decision in `PRODUCT_SPEC.md`; no repeated tick or continuous motion. | Recommended off in TV mode and paused while reduced motion is enabled. |

All motion values are proposed until implemented. Respect Android animation settings; avoid motion that is required to understand state, and never make animation the only indication of upload, error, selection, or focus.

## Visual acceptance checklist

- [ ] Light and dark semantic roles use the verified source colors and pass contrast checks for actual text and controls.
- [ ] Wallpaper colors cannot unexpectedly recolor the authored brand.
- [ ] Layout remains usable at large font scale, narrow phones, landscape, and TV viewing distance.
- [ ] Focus, pressed, disabled, loading, error, and selected states are visible without color alone.
- [ ] Motion is restrained, optional, and does not fight live gallery updates or read-only TV use.
- [ ] No emoji, plant-care content, or copied mood-board assets appear in app UI.
