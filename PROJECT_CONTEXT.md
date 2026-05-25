# Free Scanner

## Tech Stack
- Language: Kotlin
- Framework: Android SDK (native), Material Design 3
- Camera: CameraX
- Edge Detection: OpenCV 4.9
- OCR: Google ML Kit Text Recognition
- PDF: iText 7 (Android)
- Min SDK: 24 (Android 7.0), Target SDK: 34 (Android 14)
- Build: Gradle (Kotlin DSL), JDK 17

## Architecture Overview
Single-activity-per-screen architecture. Home → Camera → Editor → Export flow.
No fragments, no Jetpack Navigation. Activities communicate via intents.

## Directory Structure
```
app/src/main/
├── java/com/ujjawal/docscanner/
│   ├── model/          Document.kt
│   ├── ui/
│   │   ├── home/       HomeActivity.kt (launcher)
│   │   ├── camera/     CameraActivity.kt, EdgeOverlayView.kt
│   │   ├── editor/     EditorActivity.kt, CropOverlayView.kt
│   │   ├── gallery/    GalleryActivity.kt, PdfListAdapter.kt (legacy)
│   │   ├── pdf/        PdfPreviewActivity.kt
│   │   └── settings/   SettingsActivity.kt
│   └── utils/
│       ├── EdgeDetector.kt
│       ├── PerspectiveTransform.kt
│       ├── ImageFilters.kt
│       ├── PdfGenerator.kt
│       ├── OcrEngine.kt
│       └── AppPrefs.kt
├── res/
│   └── layout/         10 XML layouts
└── AndroidManifest.xml
```

## Key Files
- Entry point: HomeActivity.kt (launcher)
- Config: app/build.gradle.kts, settings.gradle.kts
- CI: .github/workflows/build.yml

## Common Commands
- Build: `./gradlew assembleDebug`
- Test: `./gradlew testDebugUnitTest`
- APK: `app/build/outputs/apk/debug/app-debug.apk`

## Git
- Remote: https://github.com/ujjawal200/document-scanner.git
- Branch: main (up to date with origin)
- Latest commit: 11165f5 (settings screen, home menu, filter fix)
