# Stream Player

Kotlin + XML Android video player starter. Paste an iframe HTML snippet, an embed URL, or a direct video URL.

## Features
- Extracts the `src` attribute from pasted iframe HTML.
- Recognizes common direct media URLs (MP4, HLS `.m3u8`, DASH `.mpd`, WebM, M4V, MOV).
- Uses AndroidX Media3 for supported direct media playback.
- Opens non-direct HTTP(S) embed/page URLs in a WebView so the provider's own player can work.
- Copy detected URLs and see useful playback/load errors.
- Dark UI.

## Integration guides
- [Player setup](SETUP.md) — dependencies, manifest registration, and player launch example.
- [Extractor setup](EXTRACTOR_SETUP.md) — extractor dependencies and authorized host integration.

## Build locally
Open this project in Android Studio with JDK 17 and Android SDK 35, sync Gradle, and run the app.

## GitHub Actions APK
The workflow at [`.github/workflows/android-build.yml`](.github/workflows/android-build.yml) builds a debug APK on pushes to `main`/ `master`, pull requests, or manual runs. After it finishes, open **Actions → Android APK Build → the latest run → Artifacts → StreamPlayer-debug-apk** to download the APK.

## Limitations and safety
An iframe URL usually points to a web page, not a raw media file. This app does not scrape arbitrary websites, inspect hidden player APIs, bypass DRM, evade paywalls, or bypass sign-in. Some sites block WebView playback or require provider-specific authorization and may not work. WebView has file/content access disabled, mixed content blocked, non-HTTP(S) navigation blocked, and Android Safe Browsing enabled.
