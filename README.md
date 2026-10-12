# Stream Player

Kotlin + XML Android video player. Paste an iframe HTML snippet, an embed URL, or a direct video URL.

## Integrated code
- `com.kanha.player`: native Media3 player and player data models.
- `com.kanha.extractor`: direct-link extractor, HLS playlist helper, HTTP client, generic page scanner, and provider-specific extractor template.
- The main screen sends direct media to the native player, tries source discovery for page URLs, and falls back to the provider's own WebView player when no source is found.
- Media3 HLS and DASH modules are included for `.m3u8` and `.mpd` streams.
- External subtitle models, server metadata, referer and request-header support are included.

## Build locally
Open in Android Studio with JDK 17 and Android SDK 35, sync Gradle, and run the app.

## GitHub Actions APK
The workflow at `.github/workflows/android-build.yml` builds a debug APK on pushes to `main`/`master`, pull requests, or manual runs. After success, open **Actions → Android APK Build → latest run → Artifacts → StreamPlayer-debug-apk**.

## Notes
Some providers restrict embedded playback or require account authorization. Only use sources you are authorized to access. The app does not bypass DRM, paywalls, sign-in, or access controls. Source discovery may not work on dynamically generated pages.
