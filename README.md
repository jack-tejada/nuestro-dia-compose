# Nuestro Día Compose

Kotlin/Jetpack Compose wedding-photo sharing app. The current branch contains authored feature and reliability changes; runtime, build, device, and Firebase behavior remain unverified.

---

## Overview

Nuestro Día Compose (`com.madrigalsolu.nuestrodia.compose`) is an Android photo-sharing app for wedding events. Source includes authentication, capture, gallery, display, profile, and social feature surfaces. Their behavior has not yet been validated by a build, tests, or device run.

Firebase mutations require configured Firebase and report failures rather than claiming a successful remote write. Local in-memory demo data is not a substitute for Firebase-backed upload, deletion, or social persistence. Durable offline upload/retry is not implemented or claimed.

---

## Tech Stack & Architecture

- **Language & Platform**: Kotlin targeting Android SDK 36 (min SDK 24).
- **UI Framework**: Jetpack Compose with Material 3.
- **Navigation**: Modern Navigation3 (`androidx.navigation3`) with type-safe `@Serializable NavKey` routes.
- **Backend / Storage**: Firebase Authentication, Cloud Firestore, and Firebase Storage; no project configuration or deployed rules are included.
- **Architecture Philosophy**: Ponytail - minimal abstractions, standard library first, zero unnecessary dependencies, and clear feature boundaries.
- **Motion & Images**: Authored Compose motion and asynchronous local/HTTPS image loading. Visual and device behavior remains unverified.

---

## Key Modules

```
app/src/main/java/com/madrigalsolu/nuestrodia/compose/
├── MainActivity.kt               # Edge-to-edge entry point and Navigation3 host
├── NavigationKeys.kt             # Type-safe serializable destination keys
├── theme/
│   ├── Color.kt                  # Authored wedding Light and Dark palettes
│   ├── Type.kt                   # Serif headings and Sans-serif body typography
│   ├── Theme.kt                  # M3 theme setup with dynamic color disabled
│   └── ExpressiveMotion.kt       # Spring physics, tactile press, and ambient zoom
├── feature/
│   ├── auth/                     # Authentication screens, ViewModel, and repository
│   ├── permissions/              # Progressive camera permission and Photo Picker coordinator
│   ├── capture/                  # Preview confirmation, upload progress, and retry
│   ├── gallery/                  # Justified rows, 5-min inactivity shuffle, details, QR card
│   ├── display/                  # Read-only landscape TV/projector slideshow
│   ├── profile/                  # User stats (uploads, likes), terms, and sign-out
│   └── social/                   # Photo likes, guestbook comments, and profile aggregation
└── ui/util/
    └── PhotoImage.kt             # Asynchronous local and HTTPS image loading
```

---

## Documentation

Comprehensive project documentation is available in the `docs/` and `odd/` directories:

- [ARCHITECTURE.md](ARCHITECTURE.md): Architecture boundaries, state flow, and safety constraints.
- [docs/REVIEW_CHECKLIST.md](docs/REVIEW_CHECKLIST.md): Step-by-step verification guide, build commands, and device QA matrix.
- [docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md): Product requirements, user journeys, acceptance criteria, and decision log.
- [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md): Color tokens, typography, component shapes, and motion specs.
- [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md): Manual Firebase setup guide, collection schemas, and security rules.
- [odd/tasks/nuestro-dia-compose.md](odd/tasks/nuestro-dia-compose.md): ODD task ledger and verification status.

---

## Verification & Build Guide

Build, test, and device verification have not been run for the current reliability/documentation changes. When an authorized environment is available:

### 1. Prerequisites
- JDK 17 or JDK 21 configured in `JAVA_HOME`.
- Android SDK Platform 36 and Build-Tools 36.0.0.

### 2. Run Tests
```bash
./gradlew -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest
```

### 3. Build APK
```bash
./gradlew -Pkotlin.compiler.execution.strategy=in-process :app:assembleDebug
```

For the complete testing matrix and manual QA checklist, refer to [docs/REVIEW_CHECKLIST.md](docs/REVIEW_CHECKLIST.md).
