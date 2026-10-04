# Nuestro Día Compose - Review and Verification Guide

This checklist separates authored source from verification evidence. Current reliability changes have only had static readback; builds, tests, device checks, and Firebase/backend verification remain pending, alongside the open product decisions requiring owner sign-off.

---

## 1. Current Evidence (Static Readback Only)

Feature source and focused reliability tests are present, but tests/builds/device behavior have not been run for the current changes. This is not a completed functional or security audit, and no current native review approval is claimed. A prior ND-8 native assessment was medium and under budget for its immutable candidate only; it does not cover later source or documentation changes.

The current capture source writes pending metadata before Storage upload and ready metadata only after remote acknowledgement; retry identity/URI are in-memory only. Gallery deletion removes Storage before Firestore metadata. Likes use a Firestore transaction; local like/comment state follows remote acknowledgement. Client owner checks are not backend authorization. Firebase rules/membership remain unimplemented and undeployed. See [Firebase setup](FIREBASE_SETUP.md) and the [feature ledger](../odd/tasks/nuestro-dia-compose.md).

Source presence does not establish landscape lock, M3E motion behavior, accessibility/contrast, offline resilience, or security acceptance.

---

## 2. Environment Setup (Pending Build Environment)

To compile and run the application, set up the development environment with the following dependencies:

1. **Java Development Kit (JDK)**:
   - Install OpenJDK 17 or JDK 21.
   - Ensure `JAVA_HOME` environment variable is set and `%JAVA_HOME%\bin` is added to `PATH`.
2. **Android SDK & Command Line Tools**:
   - Install Android SDK Platform 36 (or 34+) and Build-Tools 36.0.0 (or 34.0.0+).
   - Ensure `ANDROID_HOME` or `ANDROID_SDK_ROOT` is defined.
3. **Android Device or Emulator**:
   - Android 7.0+ (API level 24+) minimum; target SDK 36.
   - For camera testing, an emulator with virtual camera or a physical device with a camera is recommended.

---

## 3. Automated Build & Verification Commands

Execute the following commands from the project root once the environment is set up:

### Run Unit Tests
```bash
./gradlew -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest
```
*Expected Result*: Authored tests pass. They have not been executed for the current changes.

### Build Debug APK
```bash
./gradlew -Pkotlin.compiler.execution.strategy=in-process :app:assembleDebug
```
*Expected Result*: Build completes successfully. It has not been run for the current changes.

### Run Lint & Static Checks
```bash
./gradlew :app:lintDebug
```
*Expected Result*: Zero fatal lint errors.

---

## 4. Manual QA & Device Verification Matrix

When running the application on an emulator or physical device, perform the following verification steps:

| Step | Feature | Test Action | Expected Result | Status |
|---|---|---|---|---|
| 4.1 | Theme & UI | Launch application | Verify rendered palette, typography, contrast, and dynamic color behavior. | Pending device verification |
| 4.2 | Auth Validation | Attempt sign-in with blank fields | Immediate Spanish inline error message appears without crashing. | Pending device verification |
| 4.3 | Registration | Register with password under 6 characters or unaccepted terms | Inline validation warns user; submit button is blocked until terms consent checkbox is toggled. | Pending device verification |
| 4.4 | Password Reset | Enter email and request reset | Non-enumerating confirmation banner displays indicating instructions were sent if the email exists. | Pending device verification |
| 4.5 | Camera Permission | Click "Tomar foto" from Gallery | Rationale dialog appears before system prompt; if denied, settings redirection button opens application details. | Pending device verification |
| 4.6 | Photo Picker | Click "Elegir de galería" | System Photo Picker opens directly without requesting runtime camera permissions. | Pending device verification |
| 4.7 | Exif Correction | Capture vertical (portrait) photo | Preview renders in correct upright orientation without 90-degree sideways distortion. | Pending device verification |
| 4.8 | Upload Confirmation | Capture or select photo | `CapturePreviewScreen` requires explicit "Publicar foto" confirmation; progress bar advances during upload. | Pending device verification |
| 4.9 | Live Gallery | Return to gallery after upload | New photo appears at the top of the gallery in newest-first order with preserved aspect ratio. | Pending device verification |
| 4.10 | Inactivity Shuffle | Leave gallery idle for 5 minutes | Gallery transitions into randomized shuffle mode; touching or scrolling immediately restores newest-first order. | Pending device verification |
| 4.11 | Photo Detail & Likes | Tap a photo card in the gallery | Detail modal zooms in with spring animation; tapping heart increments like counter. | Pending device verification |
| 4.12 | Comments / Wishes | Type a wish on photo detail modal | New comment appears under photo with author name and timestamp. | Pending device verification |
| 4.13 | Owner Deletion | Open photo detail on a photo authored by current user vs another user | "Eliminar foto" button is visible only on photos belonging to current user; deleting removes it immediately from gallery. | Pending device verification |
| 4.14 | TV Display Mode | Tap projector icon in top app bar | Screen enters landscape presentation mode; photos crossfade every 8 seconds with subtle Ken Burns ambient zoom; no upload or delete buttons appear. | Pending device verification |
| 4.15 | Network Resilience | Disconnect Wi-Fi while in TV mode | Slideshow continues displaying loaded photos without error dialogues or blank screen. | Pending device verification |
| 4.16 | User Profile | Navigate to Profile screen | Profile shows initials, total uploaded photos count, total likes received, and working "Cerrar sesión" action. | Pending device verification |

---

## 5. Firebase Production Wiring Checklist

Some gallery demo/read paths use `InMemoryEventPhotoStore`, but capture, deletion, likes, and comments require remote Firebase acknowledgement. This store does not provide remote mutation persistence. Firebase configuration is a separate owner-controlled step:

- [ ] Create a Firebase project in the [Firebase Console](https://console.firebase.google.com).
- [ ] Register an Android app with package name `com.madrigalsolu.nuestrodia.compose`.
- [ ] Download `google-services.json` and place it in the `app/` directory (ignored by git).
- [ ] In Firebase Authentication, enable the **Email/Password** sign-in method.
- [ ] After product decisions and membership policy are approved, implement and test Firestore rules that:
  - Enforce authentication on all read and write operations.
  - Enforce owner-only deletion using a policy-approved ownership field (the current client field is `ownerUid`).
  - Restrict query scope to valid event IDs (`resource.data.eventId == eventId`).
- [ ] In Firebase Storage, deploy storage security rules matching the specifications in `docs/FIREBASE_SETUP.md`.

---

## 6. Open Product Decisions for Organizer Sign-Off

The following architectural and product decisions are documented in `docs/PRODUCT_SPEC.md` and should be confirmed with the event organizer before public launch:

1. **D-01 (Gallery Read Access)**: Confirm whether gallery read access is restricted strictly to authenticated guests or if a read-only guest link is allowed. (Default: restricted to authenticated guests).
2. **D-02 (QR Code Role)**: Confirm that scanning the QR code populates the event code without bypassing authentication or acting as an automatic bearer token. (Default: identifier only).
3. **D-03 (Inactivity Shuffle Scope)**: Confirm that the 5-minute inactivity shuffle is device-local and that TV mode uses its own continuous ambient cycle. (Default: device-local with instant touch reset).
4. **D-04 (Photo Retention Policy)**: Define how long wedding event photos remain hosted in Storage and whether organizers receive a full-resolution archive export after the event. (Default: user deletion only, no automated purging).
5. **D-05 (TV Display Authentication)**: Confirm whether TV presentation mode runs authenticated with organizer credentials or a designated presentation account. (Default: authenticated session).
6. **D-06 (App Link Domain)**: Select the custom HTTPS domain to host Android App Links for QR code scanning and web fallback.
7. **D-07 (Terms Consent Retention)**: Confirm whether guest acceptance of terms must be recorded with timestamp in Firestore for legal compliance or if client-side validation suffices. (Default: client-side consent validation).
