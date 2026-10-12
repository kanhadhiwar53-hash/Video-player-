# Kanha Player: Setup

This guide describes how to integrate the reusable `com.kanha.player` player module into an Android app. The source files from the supplied bundle are intended to be copied into the matching package/resource paths; this repository's current app uses `com.example.streamplayer`, so keep the two package names distinct unless you deliberately migrate the app.

## 1. Gradle dependencies

This project uses Kotlin DSL (`app/build.gradle.kts`). Add the dependencies below inside the existing `dependencies { }` block, keeping the versions aligned with the project's current Media3 version:

```kotlin
implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
implementation("androidx.media3:media3-exoplayer-dash:1.5.1")
implementation("androidx.media3:media3-ui:1.5.1")
implementation("androidx.appcompat:appcompat:1.7.0")
implementation("androidx.core:core-ktx:1.15.0")
```

The player UI uses View Binding. Ensure this remains enabled:

```kotlin
android {
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

## 2. Manifest

Add internet permission at the manifest level:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Register the player activity inside `<application>`:

```xml
<activity
    android:name="com.kanha.player.PlayerActivity"
    android:exported="false"
    android:screenOrientation="sensorLandscape"
    android:supportsPictureInPicture="true"
    android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden|keyboard|navigation|uiMode"
    android:theme="@style/Theme.AppCompat.NoActionBar" />
```

## 3. Add the source and layout files

Copy the player Kotlin files into `app/src/main/java/com/kanha/player/`:

- `PlayerActivity.kt`
- `PlayerGestures.kt`
- `PlayerModels.kt`

Copy the supplied layout to `app/src/main/res/layout/activity_player.xml` and the style resources to `app/src/main/res/values/player_styles.xml`.

## 4. Start playback

```kotlin
val items = arrayListOf(
    PlayerItem(
        id = "episode_1",
        title = "Episode 1",
        sources = listOf(
            VideoSource(
                name = "Server 1",
                url = "https://example.com/master.m3u8",
                referer = "https://example.com/"
            ),
            VideoSource(
                name = "Server 2",
                url = "https://example.com/video.mp4"
            )
        ),
        subtitles = listOf(
            SubtitleSource(
                name = "English",
                url = "https://example.com/en.vtt",
                language = "en"
            )
        )
    )
)
PlayerActivity.start(this, items, startIndex = 0)
```

Replace example URLs with media sources you own or are authorized to access. Direct MP4, HLS, and DASH playback depends on server availability, supported codecs, and any required headers/authorization.

## 5. CloudStream-style link mapping

If your project already uses an authorized extractor that exposes `name`, `url`, `headers`, and `referer`, map its link into a player source:

```kotlin
VideoSource(
    name = link.name,
    url = link.url,
    headers = link.headers,
    referer = link.referer
)
```

## Package name

The sample code uses `com.kanha.player`. If you change it, update the package declarations, imports, manifest activity name, and any generated View Binding references consistently.

## Troubleshooting

- **Unresolved `ActivityPlayerBinding`:** confirm the layout is named exactly `activity_player.xml` and View Binding is enabled.
- **HLS/DASH does not play:** confirm the matching Media3 HLS/DASH dependency is present and the URL is accessible.
- **HTTP 403/404:** check the source URL, referer, and permitted request headers. Do not attempt to bypass DRM, access controls, or sign-in restrictions.
