# Testing

## Foundation verification

- `./gradlew testDebugUnitTest` passed
- `./gradlew lintDebug` passed
- `./gradlew assembleDebug` passed

## Current coverage

Unit tests cover folder cover priority, gesture arbitration, and conflict-safe file copying.

Milestone 2 additionally covers rename behavior, the nine view modes, and file-filter contracts.

Milestone 2 verification:

- `./gradlew testDebugUnitTest` passed
- `./gradlew lintDebug --no-daemon --max-workers=2` passed
- `./gradlew assembleDebug --no-daemon --max-workers=2` passed

Thumbnail/media foundation verification:

- `./gradlew testDebugUnitTest` passed
- `./gradlew lintDebug --no-daemon --max-workers=1` passed
- `./gradlew assembleDebug --no-daemon --max-workers=1` passed

## Milestone rule

After each major feature slice, run unit tests, lint, and debug APK assembly. Add behavior-scoped tests before marking a requirement DONE.
