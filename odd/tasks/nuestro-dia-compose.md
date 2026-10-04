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
| ND-0 | Read-only mapping of CLI, visual source, old app, and class reference. | Mapping complete (static) |
| ND-1 | Create the Kotlin/Compose sibling app and record architecture/build evidence. | Source created; current build/runtime not reverified |
| ND-2 / PR 1 | FirebaseAuth repository, auth ViewModel/state, validation, unit tests, Gradle dependencies. | Source authored; Firebase/build/test behavior unverified |
| ND-2 / PR 2 | Auth composables and Navigation3 routes for sign-in, registration, reset, terms, and session routing. | Source authored; runtime unverified |
| ND-2 / PR 3 | Auth brand/theme, remove starter screen/tests, align architecture and progress docs with source. | Source authored; visual/runtime acceptance unverified |
| ND-3 | Camera/photo-picker permissions and denial/settings guidance. | Source authored; device behavior unverified |
| ND-4 | Full-resolution capture, Storage upload progress/retry, owner-only deletion. | Source authored; retry is in-memory, runtime/backend authorization unverified |
| ND-5 | Shared live gallery, aspect-ratio justified rows, newest-first ordering, stable inactivity shuffle, QR card. | Source authored; runtime/access-control policy unverified |
| ND-6 | Landscape, read-only TV/projection mode. | Source authored; landscape lock and device behavior unverified |
| ND-7 | Guest interaction: likes, photo comments/wishes, and user profile statistics. | Source authored; Firebase/runtime behavior unverified |

## Current Source Snapshot (Not Runtime Acceptance)

Auth, permission, capture, gallery, display, profile, social, theme, and navigation source surfaces are present. This inventory does not prove that behavior builds, works on devices, meets accessibility/visual acceptance, or is secured by Firebase rules.

- Capture uses a ViewModel UUID and selected URI for in-memory retries. Repository reads current readiness from Firestore `Source.SERVER`, writes a `pending` document before Storage upload, then writes `ready` metadata only after remote Storage and metadata acknowledgement. Gallery listeners omit pending documents. Retry identity/URI do not survive process death.
- Gallery deletion is Storage-first, tolerates only Storage object-not-found on retry, deletes Firestore metadata second, and updates local cache after remote success. UI owner checks are not backend authorization.
- Likes use a Firestore transaction over distinct liked-user IDs and derive the count from that normalized list. Like/comment UI state follows remote acknowledgement.
- Firebase-unconfigured mutation operations fail explicitly; in-memory demo state is not a fake Firebase write. Stored Firebase download URLs are shareable; their privacy/revocation policy remains unresolved.
- Membership, rules, privacy, retention, consent, and other D-01 through D-07 decisions remain open. No Firestore/Storage rules are implemented or deployed. Landscape locking, M3E motion, accessibility/contrast, offline resilience, and security are unverified at runtime.

## Verification & Review Checklist (Pending)

- [x] Focused regression tests for current upload and social behavior are authored; they have not been executed.
- [ ] Build, unit tests, install, device/runtime, accessibility/design, offline, and Firebase/backend acceptance remain pending.
- [ ] Security rules and membership decisions remain owner-controlled; none are implemented or deployed.
- [ ] Run `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` once Java and Android SDK are installed.
- [ ] Physical device / emulator verification: test Camera capture, Exif rotation correction, and Photo Picker.
- [ ] Add `app/google-services.json` and enable Email/Password provider in Firebase Console.
- [ ] Deploy Firestore & Storage rules matching `docs/FIREBASE_SETUP.md`.

## Audit corrections (2026-10-03)

The current source at `a5b9bc2` implements the feature surfaces, but source presence is not functional verification. The audit found incompatible HTTPS image decoding, swallowed backend failures, unstable retry IDs, unsafe concurrent likes, and stale status documentation.

### Authorized scope and checks

- Correct local source and documentation only. No Firebase account access, configuration download, rules deployment, push, PR, or merge.
- Keep app copy Spanish and technical artifacts English; use Ponytail minimalism.
- The user explicitly requests no compilation or test execution for now. Author focused regression checks where useful, but record them as unexecuted. Use static readback and `git diff --check`; do not claim RED/GREEN, runtime success, or security certification.
- Product decisions D-01 through D-07 remain user-owned. Membership policy, remote authorization, retention, consent persistence, and visual/device acceptance cannot be invented or marked complete.
- Reuse this feature ledger and mirror it at `odd/nuestro-dia-compose/tasks`. Prior native review authority belongs to its immutable historical candidate; do not rewrite, invalidate, or reuse it for new corrections.

### Work units

| ID | Source-writing task | Route and evidence | State |
|---|---|---|---|
| ND-8 | Replace unsupported remote photo decoding with maintained URI/HTTPS loading and meaningful loading/failure states. | Delegated: shared image flow across multiple non-trivial screens. | Source written; runtime-unverified |
| ND-9 | Propagate upload/delete failures, preserve cancellation, reuse upload identity across retry, and handle partial deletion safely. | One shared repository/ViewModel/test work unit with ND-10: upload anchors and social mutations share backend-success/local-cache ordering. | Source written; runtime-unverified |
| ND-10 | Use current backend state for concurrent likes and propagate social persistence failures. | Combined with ND-9 as one coherent backend-mutation reliability work unit; regression tests authored but not run. | Source written; runtime-unverified |
| ND-11 | Reconcile README, architecture, product/design/Firebase docs and checklist with authored source versus verified behavior. | Delegated: multiple documentation surfaces and current-source reconciliation. | Complete: static-only, no runtime/build/test/device/backend proof |

Acceptance is source-level behavior plus honest verification status. Keep test/device/Firebase checks pending until an authorized capable environment exists. Backend membership/rules and privacy decisions remain blocked on owner input; they are not silently included in the completed corrections.

Forecast: approximately 600-900 authored additions/deletions across coherent units (advisory only; do not omit tests or compress code to fit a line budget). Delivery uses the previously selected local stacked-to-main work-unit strategy; no PR creation is authorized. First correction boundary is `a5b9bc2`; record per-unit commit identities, line counts and native assessment outcomes. Source-mutating normalization must precede each native candidate freeze; after freeze use check-only operations.

### Evidence and next action

- Branch: `fix/audit-reliability`.
- Build/test/device/Firebase checks: intentionally not run by user request.
- ND-8 source: Coil 3.4.0 Compose + OkHttp loading replaces the synchronous `ContentResolver.openInputStream` decoder across gallery, detail, TV, and capture preview; one `PhotoImage` handles Spanish loading/failure states, disk caching is disabled, Fit/Crop plus EXIF-aware decoding are retained, and capture upload dimensions still update from the asynchronously loaded image.
- ND-8 static checks: `git diff --check` passed; repository-wide caller grep found no `decodeSampledBitmapFromUri` references after removing `ImageUtils.kt`; dependency aliases and app usage were read back. No tests/build/device check ran, as instructed; runtime image loading remains unverified.
- ND-8 dependency rationale: Coil 3.4.0's release notes list Kotlin 2.3.10 and Compose 1.9.3, matching the installed Kotlin 2.3.20 generation without raising the project toolchain to use 3.6.3; the latter release is built with Kotlin 2.4.10. HTTPS support comes from Coil's documented `coil-network-okhttp` artifact.
- ND-9/ND-10 source: capture upload now uses a ViewModel-owned UUID reused by retry and a pending Firestore photo anchor at the stable photo ID/storage path before Storage upload; only acknowledged ready metadata enters the local photo store, and gallery reads hide pending records while retaining legacy records without a status. Firebase-unconfigured mutations fail explicitly. Deletion removes Storage before metadata, tolerates only Storage object-not-found on retry, then changes local state after remote confirmation. Likes use a Firestore transaction over current backend liked-user IDs/count; comments validate text/user ID, and social local updates happen only after persistence acknowledgement. Existing photo cache upsert-by-ID is retained. `CaptureRepository.deletePhoto` was removed after source/test caller grep found no callers.
- ND-9/ND-10 regressions authored: capture retry reuses its operation ID while a new upload gets another; duplicate upload/retry submissions are ignored while active; reset cancels and ignores late progress; cancellation is not converted into a retryable UI error; gallery ViewModel surfaces like/comment failures without optimistic selection/cache updates. Tests are unexecuted. Process-death URI/retry durability remains out of scope; upload retry state is in-memory only. No ambiguous metadata failure triggers blind Storage cleanup.
- ND-9 follow-up static correction: the ready-state read uses Firestore `Source.SERVER`, avoiding a cached ready record being mistaken for current remote acknowledgement. Capture ViewModel tracks one active job plus an attempt generation, filters stale callbacks/results, and retains the canceled job guard through completion. Storage upload cancellation explicitly cancels its Firebase `UploadTask` and removes its progress listener in `finally`. Likes now sanitize/distinct backend UID values and derive count from the resulting list rather than preserving a drifted stored count. Newly authored UI messages use neutral Spanish.
- ND-9/ND-10 static checks: `git diff --check` and static caller/readback checks only; no runnable RED/GREEN, tests, build, install, formatter, Firebase access, or device proof. Runtime/backend behavior remains unverified; no security certification is claimed.
- Combined ND-9/ND-10 work-unit rationale: both correct the same remote-acknowledgement/local-cache boundary and failure semantics; artificial separation would make partial-success behavior harder to review.
- ND-11 documentation reconciliation: README, architecture, product, design, Firebase setup, review checklist, and this ledger now distinguish source presence from verified behavior; pending/ready upload schema and deletion order are documented, download-URL privacy is unresolved, and D-01 through D-07 remain open. Static `git diff --check` and changed-doc readback are the only permitted checks for this unit; runtime/build/tests/device/backend launch acceptance remain pending.
- Next action: owner decisions and authorized runtime/build/device/backend verification; do not claim the app is ready for launch or that backend authorization exists.
- ND-8 local commit: `4744560` (`fix(media): load local and remote photos asynchronously`), 371 authored changed lines. Native assessment against `a5b9bc2`: medium, `review_due: false`, `under_budget`; the slice remains pending and its reviewed boundary has not advanced. Parent repeated `git diff --check` successfully.

## Relevant files

- `README.md` — project overview, module map, and build guidelines.
- `ARCHITECTURE.md` — current code boundaries, component status, safety rules, and architecture flow.
- `docs/REVIEW_CHECKLIST.md` — comprehensive review matrix, build commands, and device QA steps.
- `docs/PRODUCT_SPEC.md` — product requirements, acceptance criteria, and explicit decision log.
- `docs/DESIGN_SYSTEM.md` — color roles, typography, component shapes, motion, and accessibility.
- `docs/FIREBASE_SETUP.md` — manual Firebase checklist, schema, security/privacy boundaries.
- `odd/tasks/nuestro-dia-compose.md` — canonical ODD feature progress, roadmap, and architecture summary.
