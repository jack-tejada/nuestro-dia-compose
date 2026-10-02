# Feature: Nuestro Día Compose wedding-photo app

## Goal

Build a fresh Kotlin/Jetpack Compose wedding-photo sharing demo with clear feature boundaries, manual Firebase setup, a refined Material 3 Expressive-inspired identity, and a durable progress record. The existing Nuestro Día Android app and `gestion-502-android` remain read-only references; neither is the implementation base.

## Why and scope

The prior app is an unfinished Java/XML implementation. A sibling Compose app keeps the classroom demo focused and independently installable. The target product is described in [the product specification](../../docs/PRODUCT_SPEC.md); proposed visual decisions are in [the design system](../../docs/DESIGN_SYSTEM.md); manual Firebase setup and security boundaries are in [Firebase setup](../../docs/FIREBASE_SETUP.md).

The source package identity is `com.madrigalsolu.nuestrodia.compose`. Do not copy old Base64 media, UI-to-Firebase coupling, unsafe read-modify-write likes, or either reference app’s `google-services.json`. No Firebase Console, CLI, account, or remote resource has been used.

## User decisions and constraints

- On 2026-10-02, the user chose Standard testing mode (not strict RED-first) and asked to continue implementation with high-quality Material 3 Expressive visual design as a core requirement.
- The delivery strategy is `ask-on-risk`; the user chose `stacked-to-main` for over-budget work units. This authorizes local work-unit commits only; no PR, push, or merge is authorized.
- Email/password authentication is in scope; email verification is excluded from the classroom demo.
- App content is Spanish. Source identifiers and technical documents are English. No emoji in app UI or project documentation.
- Theme colors come from `/home/jack/proyectos/material-theme.json`; that file supplies colors only. Shapes, typography, components, and motion must be authored deliberately.
- `/home/jack/proyectos/ejemplo.png` is a plant-care style mood-board only. Do not copy its subject matter or assets into the wedding app.
- Firebase registration/configuration is a separate manual, user-owned action. Never copy either reference app’s config or access remote Firebase state without explicit authorization.
- Engram mirror is pending because the runtime did not expose a valid registered Engram session ID. This task file is the current local source of truth; do not invent an ID or retry an unsupported write.

## Roadmap and current state

| ID | Work unit | State |
|---|---|---|
| ND-0 | Read-only mapping of CLI, visual source, old app, and class reference. | Complete |
| ND-1 | Create the Kotlin/Compose sibling app and record architecture/build evidence. | Complete; commit `4a96052` |
| ND-2 / PR 1 | FirebaseAuth repository, auth ViewModel/state, validation, unit tests, Gradle dependencies. | Committed as `56999ce`; isolated unit/build checks passed; native review pending. |
| ND-2 / PR 2 | Auth composables and Navigation3 routes for sign-in, registration, reset, terms, and authenticated placeholder. | Not implemented in current HEAD. |
| ND-2 / PR 3 | Auth brand/theme, remove starter screen/tests, align architecture and progress docs with source. | Not implemented in current HEAD. |
| ND-3 | Camera/photo-picker permissions and denial/settings guidance. | Pending |
| ND-4 | Full-resolution capture, durable Storage upload progress/retry, owner-only deletion. | Pending |
| ND-5 | Shared live gallery, justified rows, recent-first ordering, stable inactivity shuffle. | Pending; resolve product decisions first. |
| ND-6 | Landscape, read-only TV/projection mode. | Pending; resolve session policy first. |
| ND-7 | Reassess optional likes/comments/profile/notifications only after the core flow. | Deferred |

## PR 1 evidence and review blocker

- Commit: `56999ce feat(auth): add Firebase auth foundation`, parent `4a96052`, branch `feat/compose-bootstrap`.
- Files: `AuthRepository.kt`, `AuthViewModel.kt`, `AuthViewModelTest.kt`, app/root Gradle files, and `gradle/libs.versions.toml`.
- Implemented: Firebase Auth repository boundary and implementation, missing-default-app guard, current-user mapping, auth loading/user/error state, Spanish error mapping, validation for name/email/password/confirmation/consent, and non-enumerating reset notice.
- Isolated verification: `./gradlew -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :app:assembleDebug` — `BUILD SUCCESSFUL in 13s`; 4 unit tests passed and debug APK assembled. `git diff --cached --check` passed before commit.
- The same Gradle targets without `-Pkotlin.compiler.execution.strategy=in-process` stalled while Gradle tried to connect to a Kotlin compiler daemon. The in-process property was necessary for the successful isolated run.
- Runtime/instrumented harness: N/A; no device was configured or established.
- Current blocker: native review has not been completed. The local `gentle-ai.review-intended-untracked-selection/v1` schema is unavailable. Do not guess selector arguments, run an alternate lifecycle, change review state, or create/push a PR. The parent/runtime must resolve review through its supported native path before source implementation resumes.
- Firebase end-to-end auth is also pending owner registration of this package, local app-specific config, and Email/Password enablement. No Firebase remote setup was performed.

## Work-unit order and delivery

The original ND-2 source snapshot was sliced once into focused stacked-to-main candidates:

1. **PR 1 — Auth foundation (252 authored changed lines):** repository, ViewModel, focused unit tests, Firebase Gradle setup. No predecessor. Committed locally as `56999ce`.
2. **PR 2 — Auth UI and routing (304 lines in the original snapshot):** sign-in/register/reset/terms composables and Navigation3 keys/routes. Depends on PR 1.
3. **PR 3 — Auth visual identity and starter retirement (352 lines in the original snapshot):** theme, architecture/task docs, and removal of unused starter screens/repository/tests. Depends on PR 2.

Each future PR should target `main` after its predecessor is merged, then be rebased/retargeted so the diff contains only its slice. These counts describe the original source snapshot, not the later pause/resume documents. Recount final diffs before any future PR; keep cohesive documentation together rather than splitting it cosmetically to fit 400 lines. The user has not authorized any PR or push.

## Documentation-only pause/resume handoff

On 2026-10-02, the user authorized a documentation-only handoff so work can pause and resume with product, design, motion, architecture, Firebase, and next-step intent intact. This unit changes documentation only; it does not implement auth UI, the theme, photo permissions, storage, gallery, TV mode, Firebase configuration, or security rules.

- [x] `docs/PRODUCT_SPEC.md` records journeys, permission denial/settings behavior, capture/upload/retry/delete expectations, QR entry, gallery layout/order/shuffle, TV mode, accessibility, states, acceptance criteria, and open product decisions.
- [x] `docs/DESIGN_SYSTEM.md` records verified light/dark source colors and proposed (not implemented) semantic tokens, shapes, type, icons/components, motion, reduced-motion, and TV behavior.
- [x] `docs/FIREBASE_SETUP.md` records the manual setup boundary, current implementation status, proposed schema, security/privacy/retention expectations, and verification limits.
- [x] `ARCHITECTURE.md` and this ledger distinguish PR 1 from not-yet-implemented UI/theme/features, link handoff docs, and record the supported resume order and review blocker.
- [x] All changed documentation was read back; Markdown link/heading checks and `git diff --check` passed. No Gradle build applies to this docs-only unit.
- [x] Product, design, and Firebase specs were committed as `37e6988` (`docs: add product design and Firebase handoff specs`): 239 additions across three cohesive files. The architecture/task-ledger handoff is a separate local work unit in progress; no PR or push is authorized.
- [x] No app source, `.atl/`, reference repository, Firebase remote state, or review state was touched. No PR or push was created.

### Documentation work-unit boundaries

1. **Specs (`37e6988`; 239 authored additions):** product requirements/open decisions, design tokens/motion, and Firebase setup/security boundaries. Independently readable; no code behavior changed.
2. **Resume handoff (completed in the next documentation commit; 148 additions, 98 deletions):** architecture and ODD ledger corrections, source-truth status, pause state, and next-session sequence. Depends on the spec files above. Its identity is the second documentation commit in Git history; the ledger cannot embed its own hash without changing that identity.

These are local stacked work units only. The existing native-review blocker remains unresolved; do not infer review approval or open/push a PR.

### Documentation candidate review status

- Assessment command: `gentle-ai review assess --cwd /home/jack/proyectos/nuestro-dia-compose --agent codex --base-ref 37e6988 --committed-only --json` returned `risk: high`, `review_due: true`, reason `unassessable` because `.atl/.skill-registry.cache.json` and `.atl/skill-registry.md` are untracked.
- Selectorless canonical STATUS returned a `collect` transition requiring `gentle-ai.review-intended-untracked-selection/v1` JSON for target `sha256:398324fe8f26ec62ad23ecd66f4a30fc15912519096d3b99f50a020a339d93b0`; expected inventory `sha256:4425697ab74255760bb7c3b87dc13642263a59d4b65cc516abaa19765c94da71`; eligible paths are exactly those two `.atl/` files.
- The installed documentation and binary do not expose the selection payload shape. No JSON was guessed; no START, reviewer capture, acknowledgement, or review-state mutation occurred. Native review remains pending and unapproved. Preserve `.atl/`; resume only through a supported schema/collection contract.

## Required implementation checks

- Standard mode, selected by the user on 2026-10-02; no strict RED-first requirement.
- Auth/source slices: `./gradlew -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :app:assembleDebug` (use the in-process compiler property because the ordinary invocation stalled in this environment).
- Runtime UI/device check: N/A until a connected device/emulator is explicitly available; never claim one was run when it was not.
- For Firebase behavior/rules: unit tests are not end-to-end proof. Require separately authorized manual project setup and defined test account/project before remote integration checks.
- Documentation-only handoff: Markdown link/heading consistency and `git diff --check`; no Gradle build is applicable.

## Next action after the pause

1. Resolve the exact PR 1 native-review blocker with the parent/runtime; do not guess the unavailable selection schema.
2. After review is handled, implement PR 2 auth screens/routes and connect the existing ViewModel/repository. Keep missing Firebase configuration actionable and consent required; do not add email verification.
3. Implement PR 3 brand/theme after the screens, verify actual light/dark contrast and reduced-motion/TV behavior, remove the starter demo, and reconcile docs against the final source.
4. Keep Firebase registration/config download/provider enablement manual and user-owned; run end-to-end auth only after setup.
5. Continue in order: ND-3 permissions/photo picker; ND-4 capture/upload/retry/ownership deletion; ND-5 gallery/QR and resolved shuffle/audience decisions; ND-6 TV; ND-7 optional features only after core stability.

## Relevant files

- `ARCHITECTURE.md` — current code boundaries, PR 1 status, safety rules, and resume order.
- `docs/PRODUCT_SPEC.md` — product requirements, acceptance criteria, and explicit decision log.
- `docs/DESIGN_SYSTEM.md` — verified color source and proposed unimplemented visual/motion tokens.
- `docs/FIREBASE_SETUP.md` — manual Firebase checklist, proposed schema, security/privacy boundaries.
- `odd/tasks/nuestro-dia-compose.md` — canonical ODD feature progress, evidence, blockers, and next steps.
