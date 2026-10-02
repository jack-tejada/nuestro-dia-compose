# Nuestro Día Compose Architecture

This is a fresh, single-module Kotlin and Jetpack Compose app. Keep responsibilities feature-oriented so each behavior can be taught and tested without coupling screens directly to Firebase.

## Package boundaries

| Area | Responsibility |
|---|---|
| `app` | Application entry point, navigation, and dependency wiring. |
| `core/designsystem` | Reusable Compose components, typography, colors, shapes, and motion. |
| `core/model` | Feature-independent domain models and value types. |
| `core/firebase` | Firebase-backed repository implementations and data mapping. |
| `core/permissions` | Camera/media permission checks and user-facing denial guidance. |
| `feature/auth` | Registration, sign-in, password reset, validation, and consent. |
| `feature/capture` | Camera/photo-picker flow, upload progress, retry, and ownership-aware deletion. |
| `feature/gallery` | Shared photo feed, metadata presentation, and stable inactivity shuffle. |
| `feature/display` | Read-only landscape display/projection mode. |
| `feature/profile` (optional) | Add only if a concrete profile need emerges after core flows. |

These are intended package boundaries, not empty placeholder source packages. Add packages as each feature is implemented.

## UI and data flow

Compose screens render state and send user actions to feature ViewModels. ViewModels coordinate use cases and expose screen state; repositories define data access and are implemented behind the Firebase boundary where needed. Keep Firebase SDK calls out of composables and keep domain models independent of Firebase document/storage types.

## References and visual system

The existing Nuestro Día and `gestion-502-android` repositories are read-only references for behavior and integration patterns, not code to copy or an implementation base. In particular, avoid UI-to-Firebase coupling, Base64 photo fallbacks, and read-modify-write likes.

`material-theme.json` supplies color direction only. Define shapes, component treatment, and motion deliberately in `core/designsystem` rather than treating generated colors as a complete visual system.

## Staged feature plan

1. Bootstrap the Compose app and document its boundaries.
2. Add email/password registration, sign-in, password reset, validation, and required consent. **Do not add email verification**; it is excluded from the classroom demo.
3. Add camera/photo-picker access with clear permission-denial guidance.
4. Add full-resolution capture, durable Firebase Storage uploads with progress/retry, and ownership-aware deletion.
5. Add shared real-time Firestore photo metadata, a justified-row gallery, and stable inactivity shuffle.
6. Add landscape read-only display mode.
7. Reassess optional likes, comments, profile, or notifications only after the core flow is sound.

Register a distinct Firebase Android app for this package before Firebase integration. Do not reuse or copy either reference app's Firebase configuration.
