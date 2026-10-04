# Nuestro Día Compose architecture

Nuestro Día is a single-module Kotlin/Compose Android app for a wedding-photo sharing experience. Firebase SDK calls stay behind feature repositories. Source implementation is not runtime, backend-security, or release verification.

## Quick state

- Current branch: `fix/audit-reliability` (documentation reconciled against authored source; runtime/build verification pending).
- Architecture adheres to single-module Jetpack Compose with Navigation3 and Material 3.
- **Implemented modules:**
  - **Auth UI & Navigation3:** Complete screens (`SignInScreen`, `RegisterScreen`, `ResetPasswordScreen`, `TermsScreen`) wired via type-safe `NavKey` entries.
  - **Authored Theme:** Full Light and Dark palettes derived from the design system tokens, disabled dynamic wallpaper colors, custom typography (Serif headlines, Sans-serif body) and rounded component shapes.
  - **Permissions:** Progressive camera permission coordinator with rationale dialog and app settings fallback (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`); zero permissions requested at launch; native photo picker integration.
  - **Capture & Upload:** Image preview and progress, in-memory retry identity, pending/ready metadata, and a stable Storage object path. Firebase mutations fail when Firebase is unconfigured.
  - **Live Gallery:** Aspect-ratio-preserving justified photo layout, newest-first baseline ordering, configurable 5-minute inactivity session shuffle with instant user touch restoration, photo detail view with deletion for owners, and event QR code access card.
  - **TV / Projection Mode:** A read-only presentation surface is authored; landscape locking, accessibility, motion preferences, and network-loss behavior still need device verification.
  - **Starter Retirement:** Starter demo screens, mock repository, and starter tests removed.

## Current package boundaries

| Area | Status | Responsibility and implementation |
|---|---|---|
| `MainActivity` / `MainNavigation` | Implemented | Activity lifecycle, edge-to-edge Compose entry, Navigation3 backstack and route provider. |
| `NavigationKeys` | Implemented | Type-safe serializable `NavKey` declarations for Auth, Gallery, Capture Preview, and TV Display. |
| `theme` | Implemented | Material 3 authored Light/Dark palettes, custom shapes (8–28 dp), Serif headlines, Sans-serif body. `dynamicColor` disabled. |
| `feature/auth/data` | Implemented | `AuthRepository` interface, Firebase Auth implementation with missing-default-app guard and user mapping. |
| `feature/auth/ui` | Implemented | `AuthViewModel`, validation rules, `SignInScreen`, `RegisterScreen`, `ResetPasswordScreen`, `TermsScreen`, `AuthComponents`. |
| `feature/permissions` | Implemented | `rememberCameraPermissionController`, rationale dialog, settings guidance, photo picker integration. |
| `feature/capture` | Implemented | `CaptureRepository`, `CaptureViewModel`, `CapturePreviewScreen`, photo domain models, upload progress, retry logic. |
| `feature/gallery` | Implemented | `GalleryRepository`, `GalleryViewModel`, `GalleryScreen` with justified aspect-ratio rows, inactivity shuffle, `PhotoDetailDialog` (likes, comments, owner deletion), `QrEventDialog`. |
| `feature/display` | Implemented | `TvDisplayScreen` landscape read-only projection slideshow, network-loss resilient display. |
| `feature/profile` | Implemented | `ProfileScreen` user profile, upload/likes statistics, terms link, and account sign-out. |

“Implemented” here means source is present, not that the behavior is tested or accepted at runtime.

## State and data flow

**Auth and Session Flow:** `Compose screen → AuthViewModel → AuthRepository → FirebaseAuthRepository`. The repository maps Firebase users to app-owned models and reports configuration or authentication errors in natural Spanish.

**Capture and Upload Flow:** User confirms a preview → `CaptureViewModel` retains a UUID and URI for retry in memory → `CaptureRepository` reads the existing Firestore state from the server, writes a `pending` photo document, uploads to Storage at `events/{eventId}/photos/{ownerUid}/{photoId}/original`, then writes `ready` metadata after Storage and download-URL acknowledgement. Gallery listeners omit `pending` records. Retry identity/URI are not durable across process death. The stored Firebase download URL is shareable; its privacy/rotation policy remains an owner decision.

Deletion is implemented in `GalleryRepository`: remove the Storage object first, ignore only object-not-found on retry, then delete Firestore metadata and update local cache after remote success. A metadata deletion failure can leave metadata after object removal and requires a retry. UI/repository owner checks are client-side behavior, not backend authorization.

Likes use a Firestore transaction to normalize distinct liker UIDs and derive the count. Comment and like UI state changes follow remote acknowledgement. These paths have authored tests that have not been run.

**Gallery Flow:** `GalleryScreen` observes `GalleryViewModel.uiState` (backed by `GalleryRepository` Flow) → photos rendered in justified aspect-ratio rows → inactivity timer triggers a deterministic visual shuffle after 5 minutes of idle time, instantly reverting to newest-first order upon user touch/scroll.

**TV Mode Flow:** `TvDisplayScreen` observes event photos in read-only mode, smoothly crossfading every 8 seconds, with zero mutation affordances and stable display across network disconnection.

## Firebase and configuration safety

The registered package identity is `com.madrigalsolu.nuestrodia.compose`. No `app/google-services.json` or deployed Firestore/Storage rules are included. Gradle conditionally applies the Google Services plugin only when the local file exists. Firebase-backed capture, deletion, like, and comment mutations require Firebase and propagate failure; the in-memory gallery demo is not a substitute for those remote operations. No backend authorization guarantee is established by app code.

## References and design direction

Design tokens and product specifications are documented in:
- [Product specification](docs/PRODUCT_SPEC.md) — user journeys, photo lifecycle, gallery behavior, TV display, open decisions.
- [Design system](docs/DESIGN_SYSTEM.md) — source color roles, typography, component shapes, motion, and accessibility.
- [Firebase setup](docs/FIREBASE_SETUP.md) — manual setup checklist and security boundary guidelines.
- [Review checklist](docs/REVIEW_CHECKLIST.md) — comprehensive review matrix, build commands, and device QA steps.

Implementation followed the Ponytail philosophy: minimal boilerplate, standard platform features, and clean feature isolation.
