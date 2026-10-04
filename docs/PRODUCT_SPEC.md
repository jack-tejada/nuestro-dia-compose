# Nuestro Día product specification

This document records the intended wedding-photo sharing experience. Requirements describe the target product; decisions marked **Open** are not implemented or silently settled.

## Product and users

Nuestro Día is an Android app for guests at a wedding to contribute photos to a shared, live event gallery. A viewer can browse recent memories on a phone or a read-only display. Email/password authentication is in scope; email verification is explicitly out of scope for the classroom demo.

Primary users are event guests who can sign in, capture or choose a photo, and view the shared gallery. Whether a guest may join using only a QR code, who administers the event, and what the word “public” means for gallery access remain open decisions below.

## User journeys

### Account access

1. A returning guest signs in with email and password.
2. A new guest enters full name, email, password, confirmation, and required consent before an account is created.
3. Registration validation explains errors inline. The established password policy is at least eight characters, one digit, and one non-letter/digit character.
4. Password reset accepts an email and displays a non-enumerating confirmation; the message must not reveal whether an account exists.
5. A Firebase setup or network failure preserves entered form values, stops loading, and presents an actionable, human-readable error.
6. There is no email-confirmation screen or verification gate in this demo.

### Consent and photo access

Before registration completes, the guest must be able to read and accept conditions that explain:

- The camera and device gallery are used only after the guest chooses to capture or select an image.
- Uploaded images appear in the event’s shared, real-time gallery. The public-access boundary still needs an explicit decision; do not imply confidentiality or expose data anonymously by default.
- A guest may request or initiate removal of photos they own, but cannot delete another guest’s photos.
- Account and photo removal are distinct operations, and photo retention after an event is currently unspecified.

Do not request camera or media permission at launch. Request only when the guest chooses the matching action. On denial, explain the immediate consequence and offer a photo-picker path when available. On a permanent/repeated denial, offer a user-initiated link to app settings; never redirect automatically or imply permission was granted. Preserve the current form or gallery state when returning from settings.

### Capture, upload, and deletion

1. The guest may take a full-resolution camera photo or choose an existing image with the Android photo picker.
2. Show a preview and a clear cancel/confirm action before upload. Do not silently upload on selection.
3. Upload the original image to Firebase Storage, with visible progress, retry after recoverable network errors, and a stable operation/object identity so a retry does not create duplicate gallery items.
4. Show a recoverable failure state and keep the guest’s selected image available for retry while the app can safely retain it. Do not claim durable offline queuing until it is implemented and tested.
5. Delete only when ownership is verified. Remove both the photo metadata and stored object safely; make repeated deletion requests idempotent and handle partial failure without hiding an orphaned object or record.

### QR entry and shared gallery

A QR code should resolve to the intended event and provide a path into the app. If the app is absent, a useful web/install fallback is desirable. The link must not itself become an accidental bearer credential or bypass the final access policy. After sign-in, return the user to the event they selected when that event is authorized.

The gallery requirements are:

- Receive metadata updates in real time and show the newest photos first as the baseline ordering.
- Use a justified-row layout that preserves source aspect ratios; avoid cropping as the default browse behavior. Opening a photo may provide a larger view.
- Keep layout changes stable while someone is interacting. A default five-minute inactivity interval is requested for the optional shuffle; make the interval configurable, and do not reshuffle continuously.
- Show a useful loading state, an empty event state, and a retryable live-connection error state.
- Preserve photo ownership and timestamp metadata for the rules that authorize read and delete operations.

The interaction between newest-first ordering and inactivity shuffle, and whether the timer is per device or event-wide, are open decisions. See the decision log before implementing the shuffle.

### TV and projection mode

Provide a landscape, read-only display mode for a television or projected screen. It must not expose upload, delete, or account-management controls as part of the display surface. The session/access mechanism, whether display mode may run without a phone nearby, and what happens on network loss need a product decision. The safe default is an authenticated event session, no anonymous gallery reads, no automatic screen navigation, and a stable display during network interruption.

## Cross-cutting behavior

- **Loading:** disable duplicate submissions, expose progress to assistive technology, and retain entered data.
- **Empty:** explain the next useful action without showing a broken layout.
- **Errors:** identify whether the guest can retry, choose another image, re-authenticate, or contact the event organizer. Never report an upload/delete as successful before the backend confirms it.
- **Accessibility:** support TalkBack labels and logical focus order, large text without clipped actions, adequate contrast, and touch targets of at least 48 dp. Communicate errors and upload progress without color alone.
- **Offline:** keep read-only cached content only if a later implementation defines freshness/privacy behavior. Do not imply that an offline upload has been persisted unless a durable queue exists.
- **Privacy:** collect only data needed for authentication and gallery behavior. No analytics, face recognition, location extraction, public download links, or retention promise is implied by this specification.
- **Language:** app copy is Spanish; source identifiers and technical documentation remain English. No emoji in UI copy.

## Acceptance criteria

### Implemented in code
- [x] Guests can sign in, register with required consent, reset a password, and sign out; email verification is absent.
- [x] Permission prompts occur only after user intent, denial guidance is truthful, and users can return from app settings without losing context.
- [x] A chosen full-resolution photo is previewed, uploaded with progress/retry, represented once in the live gallery, and removable only by its owner.
- [x] Gallery metadata changes appear live, baseline order is newest-first, and the justified rows preserve aspect ratios without forced cropping.
- [x] The inactivity shuffle is stable, configurable, accessible, and resets on user interaction.
- [x] QR access card provides event code and details without treating an identifier as an automatic bearer token.
- [x] TV/projection mode is landscape and read-only, with ambient living motion and network-loss resilience.
- [x] Social features (ND-7): photo likes with counters, guestbook comments/dedications, and user profile statistics are implemented.
- [x] Unit test suites cover Auth, Capture, and Gallery lifecycle states, error handling, and business logic.

### Pending review / Next steps when build environment is ready
See the full review protocol in [Review checklist](REVIEW_CHECKLIST.md).
- [ ] **Build & compile check**: Run `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` once Java and Android SDK are available in the host environment.
- [ ] **Hardware validation**: Test on a physical Android device or emulator to verify camera capture, Exif rotation correction, and Photo Picker integration.
- [ ] **Firebase registration**: Download `app/google-services.json` from the owner's Firebase Console and enable Email/Password provider.
- [ ] **Security rules deployment**: Deploy Firestore and Storage rules enforcing event membership and author-only delete before loading real guest data.

## Open product decisions

| ID | Decision to resolve | Recommended safe default | Status |
|---|---|---|---|
| D-01 | Does “public shared gallery” mean anyone with a link, or only signed-in event guests? | Restrict reads to authenticated event members; do not expose anonymous reads until explicitly approved. | Open; blocks production rules and launch. |
| D-02 | Does scanning a QR code grant event membership, or only identify an event? | QR identifies an event; it never grants membership. Define a separate organizer-approved join/invitation process. | Open; no bearer-token behavior implemented. |
| D-03 | How should newest-first ordering coexist with a five-minute inactivity shuffle, and is the timer per-device or global? | Keep new arrivals newest-first; after five minutes without local interaction, perform one deterministic-per-session visual shuffle and hold it stable until activity or a new cycle. Use a configurable per-device timer; disable the shuffle on TV. | Open; confirm before gallery implementation. |
| D-04 | How long are event photos retained, and who may delete an event or another person’s content? | Guests delete only their own items; defer admin/bulk deletion and retention promises until an owner and lifecycle are defined. | Open; do not invent retention policy. |
| D-05 | How is a TV display session authenticated and revoked? | Require an authenticated event session and provide explicit exit/re-authentication; no anonymous display link by default. | Open; resolve before TV mode. |
| D-06 | Which App Link domain and web fallback will host QR destinations? | Use an owner-controlled HTTPS Android App Link with a safe web fallback; select and verify the domain before creating links. | Open; no domain or remote resource selected. |
| D-07 | Must acceptance of the terms be retained, and which policy version/timestamp is required? | Decide with the product/privacy owner; if retention is needed, store only policy version and acceptance time with a clear purpose and deletion lifecycle. | Open; the PR 1 ViewModel only validates a boolean and does not persist consent. |
