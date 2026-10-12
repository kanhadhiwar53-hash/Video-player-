# Kanha Extractor: Setup

The extractor helpers are designed for URLs you own or are authorized to access. They do not guarantee that every website can be resolved: some providers require official APIs, authentication, JavaScript execution, or protected playback.

## 1. Dependencies

Add these dependencies to the existing `dependencies { }` block in `app/build.gradle.kts`:

```kotlin
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
```

## 2. Add source files

Copy the supplied Kotlin files into `app/src/main/java/com/kanha/extractor/`:

- `ExtractorApi.kt`
- `Extractors.kt`
- `Helpers.kt`
- `Http.kt`
- `PlayerBridge.kt`

The extractor package depends on the player models in `com.kanha.player`, including `PlayerItem`, `VideoSource`, and `SubtitleSource`. Make sure those classes are included in the project before building.

## 3. Use from an Activity or Fragment

Call the suspend API from a coroutine scope such as `lifecycleScope`:

```kotlin
lifecycleScope.launch {
    val result = ExtractorManager.load(
        pageUrl,
        referer = "https://your-authorized-site.example/"
    )

    if (result.links.isEmpty()) {
        Toast.makeText(this@MainActivity, "Link nahi mila", Toast.LENGTH_SHORT).show()
        return@launch
    }

    val item = result.toPlayerItem(id = "ep1", title = "Episode 1")
    PlayerActivity.start(this@MainActivity, arrayListOf(item), 0)
}
```

## 4. Add a host-specific extractor

1. In `Extractors.kt`, copy `SampleHostExtractor` and give the class a meaningful name.
2. Implement `matches(url)` for the host you are authorized to support.
3. Implement `getUrl()` using the provider's documented API or permitted page format.
4. Register it before the first extraction:

```kotlin
ExtractorManager.register(MyHostExtractor())
```

The sample host uses `example-host.com` only as a template; it is not a working provider integration by itself.

## Important limitations

The generic scanner only finds URLs visible in fetched page source or supported packed JavaScript. It may not work with dynamically generated, encrypted, authenticated, or DRM-protected streams. Do not use it to bypass paywalls, DRM, login checks, or other access controls.
