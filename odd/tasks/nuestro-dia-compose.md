# Feature: Nuestro Día Compose wedding-photo app

## Goal

Build a fresh Kotlin/Jetpack Compose wedding-photo sharing demo with clear feature boundaries, manual Firebase setup, a refined Material 3 Expressive-inspired identity, and a durable progress record. The existing Nuestro Día Android app and `gestion-502-android` remain read-only references; neither is the implementation base.

## Why and scope

The prior app is an unfinished Java/XML implementation. A sibling Compose app keeps the classroom demo focused and independently installable. The target product is described in [the product specification](../../docs/PRODUCT_SPEC.md); proposed visual decisions are in [the design system](../../docs/DESIGN_SYSTEM.md); manual Firebase setup and security boundaries are in [Firebase setup](../../docs/FIREBASE_SETUP.md).

The source package identity is `com.madrigalsolu.nuestrodia.compose`. Do not copy old Base64 media, UI-to-Firebase coupling, unsafe read-modify-write likes, or either reference app’s `google-services.json`. No Firebase Console, CLI, account, or remote resource has been used.

## User decisions and constraints

- Standard testing mode (not strict RED-first) with high-quality Material 3 Expressive visual design as a core requirement.
- Email/password authentication is in scope; email verification is excluded from the classroom demo.
- App content is Spanish. Source identifiers and technical documents are English. No emoji in app UI or project documentation.
- Authored theme colors are based on the wedding design tokens in `docs/DESIGN_SYSTEM.md`. Shapes, typography, components, and motion are authored deliberately.
- Firebase registration/configuration is a separate manual, user-owned action. Never copy reference configs or access remote Firebase state without explicit authorization.
- Ponytail design philosophy applied across all features: minimal boilerplate, standard library/platform components first, clean feature isolation.

## Roadmap and current state

| ID | Work unit | State |
|---|---|---|
| ND-0 | Read-only mapping of CLI, visual source, old app, and class reference. | Complete |
| ND-1 | Create the Kotlin/Compose sibling app and record architecture/build evidence. | Complete |
| ND-2 / PR 1 | FirebaseAuth repository, auth ViewModel/state, validation, unit tests, Gradle dependencies. | Complete |
| ND-2 / PR 2 | Auth composables and Navigation3 routes for sign-in, registration, reset, terms, and session routing. | Complete |
| ND-2 / PR 3 | Auth brand/theme, remove starter screen/tests, align architecture and progress docs with source. | Complete |
| ND-3 | Camera/photo-picker permissions and denial/settings guidance. | Complete |
| ND-4 | Full-resolution capture, durable Storage upload progress/retry, owner-only deletion. | Complete |
| ND-5 | Shared live gallery, aspect-ratio justified rows, newest-first ordering, stable inactivity shuffle, QR card. | Complete |
| ND-6 | Landscape, read-only TV/projection mode. | Complete |
| ND-7 | Guest interaction: likes, photo comments/wishes, and user profile statistics. | Complete |

## Architecture and Feature Implementation Summary

All planned features have been implemented following the architectural boundaries:
- `feature/auth`: Full authentication UI (`SignInScreen`, `RegisterScreen`, `ResetPasswordScreen`, `TermsScreen`, `AuthComponents`) connected with `AuthViewModel` and `FirebaseAuthRepository`.
- `feature/permissions`: Camera permission controller with rationale dialog, direct fallback to Photo Picker (`PickVisualMedia`), and app settings redirection guidance (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`).
- `feature/capture`: `CapturePreviewScreen` requiring explicit user confirmation before upload, `CaptureViewModel` managing upload progress states, retry logic, and owner-only deletion through `CaptureRepository`.
- `feature/gallery`: `GalleryScreen` with aspect-ratio preserving justified photo layout, newest-first baseline ordering, configurable 5-minute inactivity session shuffle with instant user touch restoration, `PhotoDetailDialog` with owner deletion, photo likes, comments/wishes, and `QrEventDialog`.
- `feature/display`: `TvDisplayScreen` providing a read-only landscape ambient slideshow for projector/TV with zero mutation controls and resilient display during offline/network interruptions.
- `feature/profile`: `ProfileScreen` with user statistics (uploaded photos count, total likes received), terms access, and account sign-out.
- `theme`: Authored Light and Dark palettes matching `docs/DESIGN_SYSTEM.md`, disabled dynamic wallpaper coloring, custom typography (Serif headlines, Sans-serif body) and rounded component shapes (8–28 dp).
- `Navigation`: Modern Navigation3 implementation using type-safe serializable `NavKey` declarations (`SignInKey`, `RegisterKey`, `ResetPasswordKey`, `TermsKey`, `GalleryKey`, `CapturePreviewKey`, `TvDisplayKey`, `ProfileKey`).

## Verification & Review Checklist (when build tools are ready)

- [x] Code authored cleanly according to Jetpack Compose best practices, M3E motion, and Ponytail simplicity.
- [x] Comprehensive unit tests created: `AuthViewModelTest.kt`, `CaptureViewModelTest.kt`, and `GalleryViewModelTest.kt`.
- [ ] Run `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` once Java and Android SDK are installed.
- [ ] Physical device / emulator verification: test Camera capture, Exif rotation correction, and Photo Picker.
- [ ] Add `app/google-services.json` and enable Email/Password provider in Firebase Console.
- [ ] Deploy Firestore & Storage rules matching `docs/FIREBASE_SETUP.md`.

## Relevant files

- `README.md` — project overview, module map, and build guidelines.
- `ARCHITECTURE.md` — current code boundaries, component status, safety rules, and architecture flow.
- `docs/REVIEW_CHECKLIST.md` — comprehensive review matrix, build commands, and device QA steps.
- `docs/PRODUCT_SPEC.md` — product requirements, acceptance criteria, and explicit decision log.
- `docs/DESIGN_SYSTEM.md` — color roles, typography, component shapes, motion, and accessibility.
- `docs/FIREBASE_SETUP.md` — manual Firebase checklist, schema, security/privacy boundaries.
- `odd/tasks/nuestro-dia-compose.md` — canonical ODD feature progress, roadmap, and architecture summary.
