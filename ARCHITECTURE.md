# Nuestro Día Compose architecture

Nuestro Día is a fresh, single-module Kotlin/Compose Android app for a wedding-photo sharing demo. Keep feature behavior testable and keep Firebase SDK details behind repositories. This page distinguishes the current PR 1 foundation from the intended product; the auth UI and later photo features are not yet implemented.

## Quick state

- Current branch: `feat/compose-bootstrap`.
- PR 1 commit: `56999ce` (`feat(auth): add Firebase auth foundation`), parent `4a96052`.
- PR 1 adds Firebase Auth repository/state/validation and four unit tests. Its isolated check passed: `./gradlew -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :app:assembleDebug` — 4 unit tests passed and debug APK assembled.
- **Not present yet:** auth composables/routes, consent/terms screen, authored wedding theme, camera/photo picker, uploads, gallery, QR links, and TV mode.
- Firebase is not configured, and no Firebase Console/CLI/remote setup has been performed.
- Native review of PR 1 is still pending. The local `gentle-ai.review-intended-untracked-selection/v1` schema is unavailable; do not guess its selector, invoke an alternate lifecycle, or create/push a PR. Resolve through the parent/runtime’s supported native path before continuing implementation.

## Current package boundaries

| Area | Exists now | Responsibility and next boundary |
|---|---|---|
| `MainActivity` / `MainNavigation` | Yes | Activity lifecycle, Compose entry, Navigation3 wiring. Navigation still points to the starter `Main` route. |
| `ui/main` | Yes | Starter/demo screen only; it is not the wedding gallery or auth UI. Remove or replace with PR 2/3 when routing the real flow. |
| `theme` | Yes | Starter Material 3 palette and typography. Dynamic wallpaper colors remain enabled; proposed authored tokens are in `docs/DESIGN_SYSTEM.md`. |
| `feature/auth/data` | Yes, PR 1 | `AuthRepository` interface and Firebase Auth implementation, app-owned user mapping, configuration guard. |
| `feature/auth/ui` | Partial, PR 1 | `AuthViewModel`, `AuthUiState`, and validation functions. No Compose auth screens call them yet. |
| `feature/permissions` | Not yet | Permission/photo-picker coordination and denial/settings guidance. Add when ND-3 starts. |
| `feature/capture` | Not yet | Camera selection, preview, upload progress/retry, and owner-aware delete. |
| `feature/gallery` | Not yet | Event-scoped real-time metadata and justified-row presentation. |
| `feature/display` | Not yet | Landscape, read-only TV/projection mode. |
| `core/model`, `core/firebase`, `core/designsystem` | Not yet | Add shared abstractions only when a second feature needs them; avoid empty packages. |

## State and data flow

**Current auth foundation:** an eventual screen action should call `AuthViewModel`; the ViewModel validates input, exposes `StateFlow<AuthUiState>` for loading/user/error/notice, and invokes the `AuthRepository` interface. `FirebaseAuthRepository` owns Firebase SDK calls, awaits tasks, maps Firebase users, and checks that a default `FirebaseApp` exists before use. Unit tests use a fake repository. At present, `MainNavigation` still shows the starter `MainScreen`; no auth action reaches this foundation.

**Target flow:** `Compose screen → feature ViewModel/action → use case when cross-feature policy warrants one → feature repository interface → Firebase implementation`. Return app-owned models and error states toward the UI. Composables should not call Firebase directly, own long-running backend work, or decide authorization. Keep photo bytes in Storage rather than Firestore or Base64 fields.

## Firebase and configuration safety

The registered package identity is `com.madrigalsolu.nuestrodia.compose`. No `app/google-services.json` is present. Gradle conditionally applies Google Services only when the local file exists; Firebase Authentication is the only Firebase product currently added. Firestore, Storage, event membership, deployed rules, retention, and real-user data are not configured or implemented.

Firebase setup is a separate manual, user-owned step. Register this package as its own Firebase Android app, download its matching config, enable Email/Password, and review the rules/data model before real guest photos. Do not copy config from `/home/jack/proyectos/nuestro-dia-android` or `/home/jack/proyectos/gestion-502-android`. The current `.gitignore` files do not exclude `app/google-services.json`; configure a local/global exclude or obtain approval for a repository ignore rule before downloading it. See [Firebase setup](docs/FIREBASE_SETUP.md).

## References and design direction

The existing Nuestro Día app and `gestion-502-android` are read-only behavior/integration references, not an implementation base. Do not copy UI-to-Firebase coupling, Base64 media fallback, unsafe read-modify-write likes, or either Firebase configuration.

The verified color source is `/home/jack/proyectos/material-theme.json`; it supplies colors only. `/home/jack/proyectos/ejemplo.png` provides plant-care style inspiration only, not wedding imagery or reusable app content. The app still has its starter purple/dynamic theme. Proposed wedding product and visual details are recorded separately:

- [Product specification](docs/PRODUCT_SPEC.md) — flows, privacy, photo lifecycle, gallery, TV, acceptance criteria, open decisions.
- [Design system](docs/DESIGN_SYSTEM.md) — exact source colors, proposed shapes/type/components, motion, accessibility, and reduced-motion behavior.
- [Firebase setup](docs/FIREBASE_SETUP.md) — manual setup, proposed data boundary, and security/privacy expectations.

## Resume order

1. Resolve the PR 1 native-review blocker using only the parent/runtime’s supported contract; do not infer missing schema or create a PR/push.
2. Finish PR 2: auth screens, Navigation3 routes, current-session routing, required terms/consent, and actionable missing-Firebase errors.
3. Finish PR 3: implement/review the proposed brand tokens, remove the starter screen/tests, then read back the completed visual/design docs against source.
4. Treat Firebase registration/config download/provider enablement as manual and user-owned; do not claim end-to-end auth before it is configured and tested.
5. Continue ND-3 permissions/photo picker, ND-4 capture/upload/delete, ND-5 live gallery/ordering/shuffle, then ND-6 TV. Revisit optional likes/comments/profile/notifications only after the core flow is sound.

The user selected Standard testing mode (not strict RED-first), with unit tests and the repository’s Gradle debug build. The ODD progress file is the local source of truth while the Engram mirror is pending; do not invent a registered session ID.
