# Stream Player for Android

A Kotlin + XML Android video-player project built around AndroidX Media3. The app is designed to accept direct media URLs, embed URLs, or iframe HTML and attempt playback using the available playback and source-discovery paths.

> **Project status:** This repository is a work in progress. Provider compatibility and playback behavior can vary by website, stream format, request headers, and device. The project has not been independently verified across all providers or devices.

## Features in the current codebase

- **Native playback:** Uses AndroidX Media3 / ExoPlayer components.
- **Common stream formats:** Media3 HLS and DASH modules are included for supported HLS (`.m3u8`) and DASH (`.mpd`) streams.
- **URL handling:** The main screen distinguishes direct media URLs from page or embed URLs.
- **Source discovery:** Extractor components can scan some page responses for media links and inspect embedded frames.
- **WebView fallback:** When source discovery does not find a playable URL, the app can fall back to displaying the provider page in a WebView.
- **Request metadata support:** The codebase includes models/helpers for request headers, referer information, server metadata, and external subtitles.
- **Build workflow:** A GitHub Actions workflow is configured to build a debug APK.

These capabilities do not guarantee that every website or stream will work. Pages that depend on JavaScript execution, authentication, DRM, changing APIs, or provider-specific restrictions may not be supported.

## Tech stack

- Kotlin
- Android XML layouts
- AndroidX Media3 / ExoPlayer
- Gradle
- GitHub Actions

## Build and run

1. Install Android Studio.
2. Install the JDK and Android SDK versions required by the project configuration.
3. Clone or download this repository and open it in Android Studio.
4. Allow Gradle sync to finish.
5. Run the app on an Android device or emulator.

If the build fails, check the Gradle sync output and the project configuration before opening an issue. The exact SDK and JDK requirements should be taken from the current Gradle files.

## Download a CI debug APK

The workflow is located at [`.github/workflows/android-build.yml`](.github/workflows/android-build.yml).

1. Open the repository's **Actions** tab.
2. Select the **Android APK Build** workflow.
3. Open a successful run.
4. Download the **StreamPlayer-debug-apk** artifact, if that artifact is present for the run.

A CI debug APK is intended for testing and is not a signed production release.

## Project structure

The current codebase includes these main areas:

- `com.kanha.player` — native player components and player data models.
- `com.kanha.extractor` — direct-link handling, page scanning, HTTP helpers, and an extractor template.
- Main screen — routes direct media URLs to the native player, attempts source discovery for page URLs, and can use a WebView fallback.

File names and implementation details may change as the project evolves.

## Limitations

- Source discovery is best-effort and may fail on dynamically generated or protected pages.
- Supported formats depend on the stream, Media3 configuration, and device capabilities.
- A provider may require headers or an authorized session, or may prohibit embedding.
- The project does not promise compatibility with any particular third-party website.
- No claim is made here about user counts, downloads, or production readiness.

## Responsible use

Use this app only with media and services you are authorized to access. Respect website terms, copyright, privacy, and applicable laws. Do not use it to bypass DRM, paywalls, authentication, or other access controls. Only add provider-specific integrations when you have permission to do so.

## Contributing

Bug reports, reproducible test cases, documentation improvements, and compatible code contributions are welcome.

When reporting a playback issue, include (where safe and permitted):

- Android version and device/emulator details
- Stream format (for example, HLS or DASH)
- Relevant error message or log output with personal data and tokens removed
- Steps to reproduce using a source you are authorized to test

Please do not post private URLs, access tokens, account details, or copyrighted media.

## License

No license is specified in this repository yet. Until a license is added, do not assume that the code is available for unrestricted reuse. Before adding a license, review any third-party code and dependencies for compatibility.
