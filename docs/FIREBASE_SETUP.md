# Firebase setup and data boundaries

Firebase project registration and configuration are manual, user-owned setup. This repository has not accessed the Firebase Console, Firebase CLI, an account, or remote resources. There is currently no `app/google-services.json`.

## Manual setup checklist

Complete these steps only when the app owner is ready to configure Firebase:

1. Register a **separate Android app** in the intended Firebase project with application ID `com.madrigalsolu.nuestrodia.compose`.
2. Download that app’s own `google-services.json` and place it at `app/google-services.json` locally. Never reuse either reference app’s configuration.
3. The Google Services plugin is conditionally applied only when that local file exists. The current root and app Gradle files declare the plugin and Firebase Authentication dependency; Firestore and Storage are not yet wired.
4. Before adding the file, configure a personal/global Git exclude or an explicitly approved repository ignore rule for `app/google-services.json`; the current `.gitignore` files do not exclude it. Check `git status` before any commit. Do not put service-account credentials, private keys, or server secrets in the Android client.
5. In Firebase Authentication settings, enable Email/Password. The demo has no email-verification step.
6. Rebuild and test with a dedicated authorized test account after an owner has configured the project. Never claim end-to-end authentication from unit tests or a local APK built without this configuration.

No Firebase setup step was executed for the current handoff. Adding local configuration is not part of the documentation work.

## Current implementation boundary

- PR 1 (`56999ce`) adds `FirebaseAuthRepository`, an `AuthRepository` interface, an `AuthViewModel` with `StateFlow` loading/error state, validation, and unit tests.
- The repository safely reports missing Firebase initialization rather than assuming a default app exists.
- `Navigation.kt` is still the starter `Main` route and `MainScreen`; there are no auth composables/routes or terms screen in the current source.
- The theme remains the starter purple scheme and still enables Android dynamic color by default.
- There are no Firestore/Storage dependencies, data writes, deployed rules, event membership records, photo uploads, deletion behavior, or Firebase end-to-end tests.

## Planned Firebase responsibilities

| Capability | Intended boundary | Current status |
|---|---|---|
| Authentication | Auth UI sends actions to a ViewModel; repository wraps Firebase Auth and maps Firebase users/errors into app-owned types. | Repository/ViewModel only; UI is not connected. |
| Firestore gallery metadata | Feature repository exposes event/photo models and live metadata updates; composables do not call Firestore. | Not implemented. |
| Storage media | Capture feature uploads original-resolution media and returns progress/retry state through its repository boundary. | Not implemented. |
| Authorization | Firebase Security Rules enforce event membership, authenticated reads, uploader ownership, and owner-only delete. | Rules and membership model are not implemented. |

### Proposed data model (not implemented)

Use an event boundary rather than a global unscoped gallery. A candidate model for review is:

```text
events/{eventId}
events/{eventId}/members/{uid}
events/{eventId}/photos/{photoId}
Storage: events/{eventId}/photos/{ownerUid}/{photoId}/original
```

Proposed photo metadata is limited to the fields needed for display and authorization: owning UID, event ID, Storage object path, server upload timestamp, content type, and image dimensions. Avoid storing image bytes as Base64 or treating a long-lived download URL as authorization. Do not treat `eventId`, a QR URL, or a client-supplied `ownerUid` as proof of membership or ownership.

This schema, membership provisioning, admin role, and timestamp semantics remain proposals. Confirm them before creating collections or remote resources.

## Security and privacy expectations

- Start from deny-by-default rules. Require authenticated users and an explicit event-membership check for gallery reads and uploads.
- On upload, bind ownership to `request.auth.uid`; on deletion, verify ownership in trusted metadata/rules rather than trusting the client UI.
- Restrict media type and size at the backend. The exact size limit and supported image formats are open product/operations decisions.
- Keep authorization decisions in rules or a trusted backend; a hidden button is not access control.
- Make metadata/object deletion idempotent and handle partial failure. Do not silently report success while an owned object or metadata record remains.
- Do not promise retention duration, permanent deletion semantics, backups behavior, or administrator access until an owner and policy are decided.
- Define whether the gallery is public to anyone with a link or restricted to event members before any real guest photos are used. The safe default is member-only access; no anonymous read is implied.
- The terms screen must describe visibility and removal in plain Spanish, but displayed terms do not enforce backend rules.

## Verification boundary

Unit tests with a fake repository can validate ViewModel state, validation, and error mapping. They do not verify Firebase project wiring or deployed rules. Firebase Emulator/rules tests and a manual test account are future verification; obtain explicit authorization and define the test project/account before any remote or account operation.
