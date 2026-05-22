# Free Scanner

A free, open-source document scanner app for Android. Capture documents with your camera, auto-detect edges, crop manually, apply filters, extract text with OCR, and export as PDF or JPEG.

## Screenshots

| Home | Camera | Crop | Editor | Export |
|------|--------|------|--------|--------|
| ![Home](screenshots/home.png) | ![Camera](screenshots/camera.png) | ![Crop](screenshots/crop.png) | ![Editor](screenshots/editor.png) | ![Export](screenshots/export.png) |

## Features

### 📷 Smart Capture
- High-quality camera capture using CameraX
- File-based image saving with EXIF rotation handling
- Import images from gallery
- Flash toggle support

### 🔲 Edge Detection & Crop
- Automatic document edge detection using OpenCV
- Interactive crop overlay with draggable corner handles
- Manual corner adjustment for precise cropping
- Perspective correction to flatten documents

### 🎨 Image Filters
- **Original** — no processing
- **Grayscale** — black & white tones
- **B&W** — high contrast adaptive threshold
- **Enhanced** — CLAHE contrast enhancement

### 📝 OCR (Text Extraction)
- Powered by Google ML Kit Text Recognition
- Extract text from scanned documents
- Copy extracted text directly

### 📄 Export & Share
- Export as **PDF** or **JPEG**
- Rename documents before saving
- Share directly to WhatsApp, email, or any app
- Multi-page PDF support

### 🏠 Document Management
- Home screen with saved documents list
- View, share, or delete saved PDFs
- Sorted by most recent

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| Camera | CameraX |
| Edge Detection | OpenCV 4.9 |
| OCR | Google ML Kit |
| PDF Generation | iText 7 (Android) |
| UI | Material Design 3 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |

## Architecture

```
app/
├── ui/
│   ├── home/          # Home screen - document list
│   ├── camera/        # Camera capture
│   ├── editor/        # Crop overlay + filters + OCR
│   ├── gallery/       # Legacy gallery view
│   └── pdf/           # PDF preview & sharing
├── utils/
│   ├── EdgeDetector   # OpenCV edge detection
│   ├── PerspectiveTransform  # Warp correction
│   ├── ImageFilters   # Grayscale, B&W, Enhanced
│   ├── PdfGenerator   # iText PDF creation
│   └── OcrEngine      # ML Kit text extraction
└── model/
    └── Document       # Data models
```

## Build

Requires Android SDK + JDK 17.

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew testDebugUnitTest

# APK location
app/build/outputs/apk/debug/app-debug.apk
```

## CI/CD

GitHub Actions automatically builds and tests on every push. Download the APK from Actions artifacts.

## App Flow

1. **Home** → View saved documents or tap camera FAB
2. **Camera** → Frame document and capture
3. **Crop** → Auto-detected edges shown, drag corners to adjust, confirm
4. **Edit** → Apply filters, run OCR, add more pages
5. **Export** → Name your file, choose PDF/JPEG, share

## License

MIT
