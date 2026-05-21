# Document Scanner

A Kotlin Android document scanner app with:

- 📷 Camera capture with CameraX
- 🔲 Auto edge detection & crop (OpenCV)
- 📐 Perspective correction
- 🎨 Image filters (Grayscale, B&W, Enhanced contrast)
- 📝 OCR text extraction (Google ML Kit)
- 📄 Multi-page document support
- 📑 PDF export & sharing

## Build

Requires Android SDK command-line tools + JDK 17.

```bash
# Install dependencies (one-time)
sdkmanager "platforms;android-34" "build-tools;34.0.0"

# Build debug APK
./gradlew assembleDebug

# APK location
app/build/outputs/apk/debug/app-debug.apk
```

## Setup (GitHub Actions CI)

Push to this repo and the CI workflow will build the APK automatically. Download from the Actions artifacts.

## Tech Stack

- Kotlin
- CameraX
- OpenCV 4.9
- Google ML Kit (Text Recognition)
- iText 7 (PDF generation)
- Material Design 3
