package com.kanha.player

import java.io.Serializable

data class VideoSource(
    val name: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val referer: String? = null,
    val userAgent: String? = null,
) : Serializable {
    val isHls: Boolean get() = url.substringBefore('?').contains(".m3u8", ignoreCase = true)
    val isDash: Boolean get() = url.substringBefore('?').endsWith(".mpd", ignoreCase = true)
}

data class SubtitleSource(
    val name: String,
    val url: String,
    val language: String = "en",
) : Serializable

data class PlayerItem(
    val id: String,
    val title: String,
    val sources: List<VideoSource>,
    val subtitles: List<SubtitleSource> = emptyList(),
    val introSkipMs: Long = 85_000L,
) : Serializable
