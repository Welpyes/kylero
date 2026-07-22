# Agents Guide

## Project Layout

- Repo root is `kylero/`. Actual Android project lives in `blackscreen/`.
- Single-module Gradle project: root `build.gradle.kts` + `app/` module only.
- Package: `com.blackscreen`. Two source files: `MainActivity.kt`, `BlackScreenService.kt`.

## Build

```bash
cd blackscreen
./gradlew assembleDebug    # debug APK (auto-signed)
./gradlew assembleRelease  # release APK (unsigned, needs manual signing)
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

CI (`.github/workflows/build.yml`) builds debug APK only, on push/PR to `main`. Uses JDK 17.

## Key Design Decisions

- Uses native `android.app.Activity` (not AppCompat) to keep APK < 1MB.
- No Material Components, no ConstraintLayout. Only `core-ktx` dependency.
- R8 minification + resource shrinking enabled for release builds.
- Foreground service with `specialUse` type on Android 14+.
- Auto-exits on screen off via `ACTION_SCREEN_OFF` BroadcastReceiver.

## Gotchas

- **Version catalog mismatch**: `gradle/libs.versions.toml` defines AGP 8.13.1, Kotlin 2.2.21, and several unused libraries (appcompat, material, constraintlayout). Actual `build.gradle.kts` uses AGP 8.5.0, Kotlin 1.9.24. The catalog is not referenced by any `build.gradle.kts` — treat it as stale.
- **No tests**: No test dependencies, no test sources. `./gradlew test` is a no-op.
- **No lint/detekt/ktlint**: No static analysis configured.
- **Release builds unsigned**: Must be signed manually before install.
- **Gradle wrapper**: Committed (jar + properties). Use `./gradlew`, not system gradle.

## Modifying This App

- Keep dependencies minimal — the whole point is tiny APK size.
- If adding features, ensure they work with `minSdk = 26`.
- ProGuard rules in `app/proguard-rules.pro` keep `MainActivity` and `BlackScreenService`.
- Android 14+ requires `FOREGROUND_SERVICE_SPECIAL_USE` permission and typed `startForeground()`.
