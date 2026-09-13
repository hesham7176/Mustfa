# Mustfa

Mustfa is a native Android file and media explorer built with Kotlin, Jetpack Compose, AndroidX Media3, and coroutine-based data operations.

## Current status

Implemented foundation:

- Compose home/file browser with Arabic and English resources
- Internal storage listing, search filtering, sorting, breadcrumbs, back navigation, and folder creation
- Folder cover resolution with `cover`, `poster`, and `folder` priority
- Asynchronous copy, move, delete, conflict-safe destinations, and progress callbacks
- Search and playback-position primitives
- Unit tests for folder covers, gestures, and file operations
- GitHub Actions build, test, lint, and APK verification

Planned integrations include SAF/USB browsing, Media3 player screens, image viewer, audio session, archive support, recycle bin, network/cloud providers, app manager, settings, and the storage analyzer UI.

## Build

See [BUILD.md](BUILD.md). The local container used during scaffolding did not provide an Android SDK; CI installs the required SDK packages.
