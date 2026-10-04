# Firebase setup and data boundaries

Firebase project registration and configuration are manual, user-owned setup. This repository has not accessed the Firebase Console, Firebase CLI, an account, or remote resources. There is currently no `app/google-services.json`.

## Manual setup checklist

Complete these steps only when the app owner is ready to configure Firebase:

1. Register a **separate Android app** in the intended Firebase project with application ID `com.madrigalsolu.nuestrodia.compose`.
2. Download that app’s own `google-services.json` and place it at `app/google-services.json` locally. Never reuse either reference app’s configuration.
3. The Google Services plugin is conditionally applied only when that local file exists. Source now calls Firebase Auth, Firestore, and Storage; no configured project or end-to-end Firebase verification is included.
4. Before adding the file, configure a personal/global Git exclude or an explicitly approved repository ignore rule for `app/google-services.json`; the current `.gitignore` files do not exclude it. Check `git status` before any commit. Do not put service-account credentials, private keys, or server secrets in the Android client.
5. In Firebase Authentication settings, enable Email/Password. The demo has no email-verification step.
6. Rebuild and test with a dedicated authorized test account after an owner has configured the project. Never claim end-to-end authentication from unit tests or a local APK built without this configuration.

No Firebase setup step was executed for the current handoff. Adding local configuration is not part of the documentation work.

## Current implementation boundary

- Auth, photo metadata, Storage uploads/deletes, likes, and comments have repository-backed source paths. Authored fake-repository tests do not verify Firebase wiring or rules; tests/builds have not been run for the current reliability changes.
- Uploads use `events/{eventId}/photos/{photoId}` metadata and `events/{eventId}/photos/{ownerUid}/{photoId}/original` Storage objects. The Firestore photo document contains `id`, `eventId`, `ownerUid`, `ownerName`, `storagePath`, `timestamp`, `width`, `height`, `aspectRatio`, and `uploadStatus`. It is written as `pending` before upload and as `ready` only after Storage upload and download-URL acknowledgement. Gallery reads omit `pending` documents.
- A retry reuses the ViewModel's UUID and selected URI in memory; process-death durability is not implemented. The app stores Firebase `downloadUrl` as `uriString`; these URLs are shareable. Access/rotation and associated privacy policy remain unresolved.
- Deletion removes the Storage object before Firestore metadata. Only Storage object-not-found is tolerated for retry; metadata/cache changes occur after remote confirmation. Partial failure can leave metadata without an object until retried.
- Likes use a Firestore transaction over the current `likedByUids` list, normalizing distinct UIDs and deriving `likesCount`. Like/comment local state follows remote acknowledgement.
- Client-side owner checks are UX behavior only. They do not enforce security. No Firestore/Storage rules, membership provisioning, or backend authorization have been implemented or deployed.
- Firebase-unconfigured mutations fail explicitly rather than returning fake success. The in-memory event/photo store is not an upload or social persistence fallback.

## Planned Firebase responsibilities

| Capability | Intended boundary | Current status |
|---|---|---|
| Authentication | Auth UI sends actions to a ViewModel; repository wraps Firebase Auth and maps Firebase users/errors into app-owned types. | UI, repository, and ViewModel source are connected; Firebase runtime verification is pending. |
| Firestore gallery metadata | Feature repository exposes event/photo models and live metadata updates; composables do not call Firestore. | Source implemented; backend/runtime verification pending. |
| Storage media | Capture feature uploads media and returns progress/retry state through its repository boundary. | Source implemented; process-death retry durability and runtime behavior unverified. |
| Authorization | Firebase Security Rules enforce event membership, authenticated reads, uploader ownership, and owner-only delete. | Rules and membership model are not implemented or deployed. |

### Current client data shape (not a security policy)

Use an event boundary rather than a global unscoped gallery. A candidate model for review is:

```text
events/{eventId}
events/{eventId}/members/{uid}
events/{eventId}/photos/{photoId}
Storage: events/{eventId}/photos/{ownerUid}/{photoId}/original
```

The current client writes photo ID, event ID, owner UID/name, Storage path, client timestamp, dimensions/aspect ratio, upload status, download URL, and like UID/count fields. It uses a client timestamp, not a server upload timestamp. A download URL is shareable and is not proof of membership or ownership. Do not treat `eventId`, a QR URL, or client-supplied `ownerUid` as proof of membership or ownership.

Membership provisioning, admin role, timestamp semantics, and the accepted photo fields remain subject to owner decisions. No remote collections/resources have been created by this work.

## Security and privacy expectations

- Start from deny-by-default rules. Require authenticated users and an explicit event-membership check for gallery reads and uploads.
- On upload, bind ownership to `request.auth.uid`; on deletion, verify ownership in trusted metadata/rules rather than trusting the client UI.
- Restrict media type and size at the backend. The exact size limit and supported image formats are open product/operations decisions.
- Keep authorization decisions in rules or a trusted backend; a hidden button is not access control.
- Make metadata/object deletion idempotent and handle partial failure. Do not silently report success while an owned object or metadata record remains.
- Do not promise retention duration, permanent deletion semantics, backups behavior, or administrator access until an owner and policy are decided.
- Define whether the gallery is public to anyone with a link or restricted to event members before any real guest photos are used. The safe default is member-only access; no anonymous read is implied.
- The terms screen must describe visibility and removal in plain Spanish, but displayed terms do not enforce backend rules.
- Decide whether shareable Firebase download URLs are acceptable for event photos and how they should be revoked or rotated; do not imply that a URL is member-gated.

## Verification boundary

Unit tests with a fake repository can validate ViewModel state, validation, and error mapping. They do not verify Firebase project wiring or deployed rules. Firebase Emulator/rules tests and a manual test account are future verification; obtain explicit authorization and define the test project/account before any remote or account operation.
