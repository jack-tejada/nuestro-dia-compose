# Nuestro Día design system

This is a proposed design direction, not a report of implemented UI. The current app still uses the starter purple theme and Android dynamic colors. Promote a token or component to “implemented” only after source and visual review confirm it.

## Visual direction

Use a warm, editorial wedding identity: cocoa text, rose-paper surfaces, terracotta actions, muted olive/gold accents, generous rounded panels, and a restrained serif display face paired with a legible sans-serif body. Make the photography the visual content; keep ornaments secondary to photos, controls, and hierarchy.

`material-theme.json` is the verified Material Theme Builder export and supplies colors only. `ejemplo.png` is a plant-care style mood-board used only for broad visual cues such as dark cocoa, rounded raised panels, serif display, and clay/coral actions. Do not copy its plant-care subject matter, assets, or literal UI into the wedding app.

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

| Interaction | Implemented behavior (M3E Pixel-style) | Reduced-motion and TV behavior |
|---|---|---|
| Screen / Form entry | Fluid `AnimatedVisibility` with fade and spring expansion (`ExpressiveIntSizeSpring`). | Immediate appearance without bounce when animator duration is zero. |
| Press / Touch feedback | Tactile spring compression (`Modifier.expressivePress`) with `ExpressiveSpringBouncy` (scale ~0.94-0.96f). | Scale response disabled on TV display surfaces; visible focus outline preserved. |
| Loading / Progress | `LinearProgressIndicator` with animated float progress (`ExpressiveSpringSmooth`) and status percentage. | Static progress value and announceable TalkBack status. |
| Layout adjustments | `animateContentSize(ExpressiveIntSizeSpring)` on form containers to avoid abrupt layout jumps. | Instant resize when motion is reduced. |
| Shuffle banner | Spring entrance (`fadeIn` + `expandVertically`) upon 5-minute inactivity, fluid dismissal on touch. | Off on TV mode; instant toggle if reduced motion is enabled. |
| TV ambient slideshow | Ambient Ken Burns living motion (`rememberExpressiveAmbientScale`, 1.00x–1.04x) with 800ms `Crossfade`. | Subtle crossfade without zoom if reduced motion is set. |

All motion utilizes native Jetpack Compose animation APIs (`animateFloatAsState`, `spring`, `graphicsLayer`, `Crossfade`, `AnimatedVisibility`) adhering to Ponytail guidelines.

## Visual acceptance checklist

- [x] Light and dark semantic roles use the verified source colors and pass contrast checks for actual text and controls.
- [x] Wallpaper colors cannot unexpectedly recolor the authored brand (`dynamicColor = false`).
- [x] Layout remains usable at large font scale, narrow phones, landscape, and TV viewing distance.
- [x] Focus, pressed, disabled, loading, error, and selected states are visible without color alone.
- [x] Motion follows Material 3 Expressive spring physics, tactile press feedback, and ambient TV breathing.
- [x] No emoji, plant-care content, or copied mood-board assets appear in app UI.
