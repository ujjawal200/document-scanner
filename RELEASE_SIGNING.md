# Release Signing Setup

## Generate Keystore (run once, keep the file FOREVER)

```bash
keytool -genkey -v -keystore app/release-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias free-scanner \
  -storepass FreeScanner2026 \
  -keypass FreeScanner2026 \
  -dname "CN=Ujjawal, OU=Dev, O=FreeScanner, L=India, ST=India, C=IN"
```

Or use Android Studio: **Build → Generate Signed Bundle/APK → Create new keystore**

## Build Release AAB

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## ⚠️ IMPORTANT
- **NEVER lose `release-keystore.jks`** — you cannot update your app without it
- Back it up to Google Drive or a safe location
- The keystore is gitignored (not pushed to GitHub)
