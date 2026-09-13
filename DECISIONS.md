# Architecture Decisions

## 001: Single application module initially

Use one Android application module with package-level separation. Add Gradle modules only when a subsystem has a stable boundary and independent build value.

## 002: Compose and Material 3

Use Jetpack Compose and Material 3 for responsive UI, with Android string resources for Arabic and English.

## 003: File access boundaries

Keep java.io filesystem access in repositories and operations. Add Storage Access Framework support through a separate document-tree repository so Composables never own permissions or I/O.

## 004: State ownership

ViewModels own navigation, selection, sorting, filtering, and operation state. Composables render state and emit user intent.

## 005: Honest feature status

A feature is marked DONE only after implementation and the relevant test, lint, and build gates succeed.
