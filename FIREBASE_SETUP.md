# Firebase Setup

## Steps to enable Firebase Analytics:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing)
3. Add an Android app with package name: `com.ujjawal.docscanner`
4. Download `google-services.json`
5. Place it at: `app/google-services.json`
6. Build and run — analytics will start automatically

## Note
Without `google-services.json`, the app still builds and runs fine — analytics events are simply not sent.
