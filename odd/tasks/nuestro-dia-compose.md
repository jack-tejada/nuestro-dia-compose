# Feature: nuestro-dia-compose — Fresh Compose application foundation

## Objective
Create a clean Kotlin + Jetpack Compose Android application for the Nuestro Día wedding-photo demo, with an explainable feature-oriented structure and durable ODD progress tracking. The existing Nuestro Día app and `gestion-502-android` remain read-only behavior/integration references; neither is the implementation base.

## Why
The previous app is unfinished Java/XML and an incremental migration would mix old and new architecture. A fresh app makes the classroom demo simpler to explain and leaves both reference projects intact.

## Scope
- Scaffold a sibling app at `/home/jack/proyectos/nuestro-dia-compose` using the installed Android CLI's Compose template.
- Use Kotlin, a single Gradle `app` module, Compose, and feature-oriented packages; preserve the current app as an independent installable reference.
- Retain the existing Nuestro Día application ID with a `.compose` suffix so both variants can coexist; configure/register a distinct Firebase Android app before connecting Firebase.
- Document the intended package boundaries, visual direction, Firebase integration cautions, and staged feature roadmap.
- Create no Firebase configuration, credentials, product features, or remote resources in this bootstrap task.

## Constraints
- No emoji in app or project documentation.
- Keep source and technical docs in English; user conversation remains Spanish.
- Email verification is excluded from the classroom demo flow; email/password authentication remains in scope.
- Do not copy the old app's Base64 photo fallback, read-modify-write likes, or UI-to-Firebase coupling.
- Theme JSON at `/home/jack/proyectos/material-theme.json` supplies colors only; establish shapes and motion deliberately in the design system.
- ODD route: direct bootstrap after delegated read-only CLI/reference mapping (4+ file/context trigger).
- TDD: no project-specific setting or runner has been established for the new app; scaffold gets a Gradle debug-build smoke check, not invented test-first evidence. Resolve TDD and exact runner when implementing the first behavior task.
- Engram mirror: pending (the current Engram write integration reports `unknown_session`; local task file is authoritative until the mirror can be synchronized).

## Roadmap checklist
- [ ] ND-1 Bootstrap the Kotlin/Compose project with Android CLI, architecture guide, and build smoke check. Route: delegated CLI/reference mapping then direct setup. Estimated authored diff: under 400 lines excluding generated files.
- [ ] ND-2 Implement email/password registration, sign-in, password reset, validation, and required consent; omit email verification for demo. Route: determine after mapping the fresh structure; resolve TDD first.
- [ ] ND-3 Implement graceful camera/photo-picker access and denial guidance. Route: determine before implementation.
- [ ] ND-4 Implement full-resolution capture, durable Firebase Storage upload progress/retry, ownership-aware deletion. Route: determine before implementation.
- [ ] ND-5 Implement shared real-time Firestore photo metadata and justified-row gallery with stable inactivity shuffle. Route: determine before implementation; clarify global versus per-device shuffle if needed.
- [ ] ND-6 Implement landscape read-only display/projection mode. Route: determine before implementation.
- [ ] ND-7 Reassess optional likes/comments/profile/notifications only after core flow is sound. Route: determine before implementation.

## Authorized scope
The user authorized on 2026-10-01 a fresh Compose app structure using the Android CLI, retaining `/home/jack/proyectos/nuestro-dia-android` as a behavior reference alongside `/home/jack/proyectos/gestion-502-android`. This authorization covers ND-1; later roadmap behavior stays within the user's stated app scope but is not started in this task.

## Acceptance criteria
- A separate Kotlin + Compose app exists at `/home/jack/proyectos/nuestro-dia-compose` and can build a debug APK.
- Both `namespace` and `applicationId` use `com.madrigalsolu.nuestrodia.compose`.
- A concise architecture guide maps app/core/feature responsibilities and names the staged feature sequence.
- Existing reference repositories remain unchanged.
- This file records observed build result and commit identity; Engram mirror status is honest.

## Applicable checks
- `/home/jack/Android/Sdk/cmdline-tools/latest/bin/android create ...` — create scaffold.
- `./gradlew :app:assembleDebug` — scaffold smoke check.
- `git status --short` and diff readback — scope check.
- TDD mode: no behavior is implemented in ND-1; smoke-build only. Reassess TDD for ND-2 onward.

## Delivery
- Strategy: `ask-on-risk`; forecast below 400 authored lines (generated scaffold files excluded from heuristic).
- Branch: create a feature branch before source changes/commit; bootstrap files must not land directly on a default branch.
- Commit: one work-unit commit for fresh scaffold + architecture/roadmap documentation + build evidence.

## Progress
- [x] ND-0 Read-only mapping of CLI, theme, prior app and class reference completed; confirmed target path is available.
- [x] ND-1 Bootstrapped from the Android CLI Compose template, added this architecture guide, and verified the debug build.

### ND-1 evidence
- CLI scaffold: `/home/jack/Android/Sdk/cmdline-tools/latest/bin/android create empty-activity --name='Nuestro Día' --output=/home/jack/proyectos/nuestro-dia-compose --application-id=com.madrigalsolu.nuestrodia.compose --namespace=com.madrigalsolu.nuestrodia.compose` — succeeded after temporarily moving the pre-existing `odd/` directory out of the target; restored it immediately afterward. The CLI refuses a non-empty output directory rather than preserving existing files.
- Identity readback: `app/build.gradle.kts` contains `namespace = "com.madrigalsolu.nuestrodia.compose"` and `applicationId = "com.madrigalsolu.nuestrodia.compose"`.
- Smoke build: from `/home/jack/proyectos/nuestro-dia-compose`, `./gradlew :app:assembleDebug` — `BUILD SUCCESSFUL in 8m 11s`; `36 actionable tasks: 36 executed`; configuration cache stored. Gradle emitted an SDK XML v4 compatibility warning and packaged `libandroidx.graphics.path.so` without stripping; neither prevented the build.
- TDD: not applicable to this scaffold-only task; no app behavior was implemented.
- Commit identity: the work-unit commit is reported in the handoff; its final object ID cannot be embedded in the same commit without changing that ID.

## Next step
Resolve TDD mode and its exact test runner before implementing ND-2; keep Firebase registration/configuration separate from this scaffold.
