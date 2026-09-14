# Build

Requirements:

- JDK 17
- Android SDK platform 35
- Build tools 35.0.0

Commands:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

For a local machine, set `ANDROID_HOME` or `ANDROID_SDK_ROOT` to the Android SDK directory. CI uses `android-actions/setup-android` and installs platform 35.
