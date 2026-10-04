# Nuestro Día Compose - Review and Verification Guide

This document defines the comprehensive review, verification, and testing checklist for the Nuestro Día Compose wedding photo-sharing application. It outlines what has been verified statically, what requires runtime verification once build tools are available, and the open product decisions requiring organizer sign-off.

---

## 1. Current Implementation Status (Static Audit Complete)

The complete codebase has been written and audited following the Ponytail architectural principles and Material 3 Expressive design tokens:

- **Theme & Identity**: Custom wedding palette (Light and Dark) in `Color.kt`, Serif headlines and Sans-serif body in `Type.kt`, custom shapes (8 dp to 28 dp) in `Theme.kt`, and dynamic wallpaper coloring disabled (`dynamicColor = false`).
- **Tactile Motion**: Spring-based physics (`spring(dampingRatio = 0.7f, stiffness = 400f)`), press scale interactions, and ambient subtle zoom in `ExpressiveMotion.kt`.
- **Authentication**: `SignInScreen`, `RegisterScreen`, `ResetPasswordScreen`, `TermsScreen`, and `AuthComponents` integrated with `AuthViewModel` and `FirebaseAuthRepository` (with graceful fallback when Firebase is not configured).
- **Permissions**: Progressive camera permission coordinator (`rememberCameraPermissionController`), rationale dialog, application settings guidance, and photo picker fallback (`PickVisualMedia`).
- **Capture & Upload**: Mandatory confirmation before upload in `CapturePreviewScreen`, real-time progress indicators, retry logic, idempotent UUID generation, and owner-only photo deletion.
- **Live Gallery**: Justified aspect-ratio rows preserving camera proportions, newest-first ordering, 5-minute inactivity session shuffle with instant touch reset, `PhotoDetailDialog`, and event QR code card (`QrEventDialog`).
- **Social Features**: Photo likes with real-time counts, guestbook comments and wishes, and user profile statistics (photos uploaded, likes received) in `ProfileScreen`.
- **TV / Presentation Mode**: Read-only landscape slideshow with 8-second crossfade, ambient living zoom, corner attribution, zero mutating controls, and resilience during network interruption.
- **Memory Safety & Exif**: Efficient downsampling with `inJustDecodeBounds` and power-of-two `inSampleSize` in `ImageUtils.kt`, plus orientation correction via `android.media.ExifInterface`.
- **Navigation**: Modern Navigation3 backstack with type-safe `@Serializable NavKey` definitions and stack clearing on session transitions.
- **ProGuard / R8**: Rules configured in `app/proguard-rules.pro` for Navigation3, Kotlin Serialization, and Firebase.
- **Unit Tests**: Full test suites authored in `AuthViewModelTest.kt`, `CaptureViewModelTest.kt`, and `GalleryViewModelTest.kt`.

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
*Expected Result*: All tests in `AuthViewModelTest`, `CaptureViewModelTest`, and `GalleryViewModelTest` pass without errors.

### Build Debug APK
```bash
./gradlew -Pkotlin.compiler.execution.strategy=in-process :app:assembleDebug
```
*Expected Result*: Build completes successfully and outputs `app/build/outputs/apk/debug/app-debug.apk`.

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
| 4.1 | Theme & UI | Launch application | Warm terracotta and champagne palette renders; headlines display in Serif typography; no dynamic wallpaper recoloring occurs. | Ready for device |
| 4.2 | Auth Validation | Attempt sign-in with blank fields | Immediate Spanish inline error message appears without crashing. | Ready for device |
| 4.3 | Registration | Register with password under 6 characters or unaccepted terms | Inline validation warns user; submit button is blocked until terms consent checkbox is toggled. | Ready for device |
| 4.4 | Password Reset | Enter email and request reset | Non-enumerating confirmation banner displays indicating instructions were sent if the email exists. | Ready for device |
| 4.5 | Camera Permission | Click "Tomar foto" from Gallery | Rationale dialog appears before system prompt; if denied, settings redirection button opens application details. | Ready for device |
| 4.6 | Photo Picker | Click "Elegir de galería" | System Photo Picker opens directly without requesting runtime camera permissions. | Ready for device |
| 4.7 | Exif Correction | Capture vertical (portrait) photo | Preview renders in correct upright orientation without 90-degree sideways distortion. | Ready for device |
| 4.8 | Upload Confirmation | Capture or select photo | `CapturePreviewScreen` requires explicit "Publicar foto" confirmation; progress bar advances during upload. | Ready for device |
| 4.9 | Live Gallery | Return to gallery after upload | New photo appears at the top of the gallery in newest-first order with preserved aspect ratio. | Ready for device |
| 4.10 | Inactivity Shuffle | Leave gallery idle for 5 minutes | Gallery transitions into randomized shuffle mode; touching or scrolling immediately restores newest-first order. | Ready for device |
| 4.11 | Photo Detail & Likes | Tap a photo card in the gallery | Detail modal zooms in with spring animation; tapping heart increments like counter. | Ready for device |
| 4.12 | Comments / Wishes | Type a wish on photo detail modal | New comment appears under photo with author name and timestamp. | Ready for device |
| 4.13 | Owner Deletion | Open photo detail on a photo authored by current user vs another user | "Eliminar foto" button is visible only on photos belonging to current user; deleting removes it immediately from gallery. | Ready for device |
| 4.14 | TV Display Mode | Tap projector icon in top app bar | Screen enters landscape presentation mode; photos crossfade every 8 seconds with subtle Ken Burns ambient zoom; no upload or delete buttons appear. | Ready for device |
| 4.15 | Network Resilience | Disconnect Wi-Fi while in TV mode | Slideshow continues displaying loaded photos without error dialogues or blank screen. | Ready for device |
| 4.16 | User Profile | Navigate to Profile screen | Profile shows initials, total uploaded photos count, total likes received, and working "Cerrar sesión" action. | Ready for device |

---

## 5. Firebase Production Wiring Checklist

The app functions in standalone demo mode via `InMemoryEventPhotoStore` when Firebase is not present. To connect to production Firebase:

- [ ] Create a Firebase project in the [Firebase Console](https://console.firebase.google.com).
- [ ] Register an Android app with package name `com.madrigalsolu.nuestrodia.compose`.
- [ ] Download `google-services.json` and place it in the `app/` directory (ignored by git).
- [ ] In Firebase Authentication, enable the **Email/Password** sign-in method.
- [ ] In Cloud Firestore, deploy security rules that:
  - Enforce authentication on all read and write operations.
  - Enforce author-only deletion (`request.auth.uid == resource.data.authorId`).
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
