# Nuestro Día Compose

Wedding photo-sharing Android application developed in modern Jetpack Compose, featuring Material 3 Expressive tactile motion and built according to the Ponytail minimalist architectural philosophy.

---

## Overview

Nuestro Día Compose (`com.madrigalsolu.nuestrodia.compose`) is an Android photo-sharing experience tailored for wedding events. It provides:
- Real-time guest photo sharing with aspect-ratio preserving justified gallery layout.
- Material 3 Expressive spring physics, tactile touch responses, and ambient motion.
- Full landscape TV / projection slideshow mode for venues and screens.
- Social interactions: likes, comments/wishes, and user profile metrics.
- Progressive permissions, offline resilience, and robust memory handling for high-resolution images.
- Zero boilerplate architecture with clean separation of concerns and graceful local in-memory fallbacks when Firebase is unconfigured.

---

## Tech Stack & Architecture

- **Language & Platform**: Kotlin 2.0+ targeting Android SDK 36 (min SDK 24).
- **UI Framework**: Jetpack Compose with Material 3 (BOM 2024.09.00 / Compose 1.7+).
- **Navigation**: Modern Navigation3 (`androidx.navigation3`) with type-safe `@Serializable NavKey` routes.
- **Backend / Storage**: Firebase Authentication, Cloud Firestore, and Firebase Storage via official Firebase BoM (with in-memory fallback store).
- **Architecture Philosophy**: Ponytail - minimal abstractions, standard library first, zero unnecessary dependencies, and clear feature boundaries.
- **Motion & Physics**: Material 3 Expressive spring physics (`ExpressiveMotion.kt`) and Ken Burns ambient camera panning.
- **Memory Safety**: Direct sub-sampling (`inSampleSize`) avoiding OOM on 48MP photos, and Exif rotation handling via `android.media.ExifInterface`.

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
    └── ImageUtils.kt             # OOM-safe downsampling and Exif rotation handling
```

---

## Documentation

Comprehensive project documentation is available in the `docs/` and `odd/` directories:

- [ARCHITECTURE.md](ARCHITECTURE.md): Architecture boundaries, state flow, and safety constraints.
- [docs/REVIEW_CHECKLIST.md](docs/REVIEW_CHECKLIST.md): Step-by-step verification guide, build commands, and device QA matrix.
- [docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md): Product requirements, user journeys, acceptance criteria, and decision log.
- [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md): Color tokens, typography, component shapes, and motion specs.
- [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md): Manual Firebase setup guide, collection schemas, and security rules.
- [odd/tasks/nuestro-dia-compose.md](odd/tasks/nuestro-dia-compose.md): ODD task log, work units ND-0 through ND-7, and milestones.

---

## Verification & Build Guide

The codebase is authored and verified statically. To compile and run once build tools are installed:

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
