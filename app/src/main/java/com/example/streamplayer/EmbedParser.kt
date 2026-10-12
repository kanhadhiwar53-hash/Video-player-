package com.example.streamplayer

import android.net.Uri
import java.util.Locale

data class ParsedVideoInput(
    val url: String,
    val isDirectMedia: Boolean,
    val mediaType: String
)

object EmbedParser {
    private val iframeSrcRegex = Regex(
        """<iframe\b[^>]*?\bsrc\s*=\s*(['"])(.*?)\1""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    private val urlRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

    fun parse(input: String): Result<ParsedVideoInput> = runCatching {
        val trimmed = input.trim()
        require(trimmed.isNotEmpty()) { "Paste an embed snippet or video URL first." }

        val candidate = iframeSrcRegex.find(trimmed)?.groupValues?.get(2)
            ?: if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) {
                trimmed
            } else {
                urlRegex.find(trimmed)?.value
                    ?: throw IllegalArgumentException("No iframe src or HTTP(S) URL found.")
            }

        val normalized = candidate.trim()
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .let { if (it.startsWith("//")) "https:$it" else it }
            .trimEnd('.', ',', ')', ']', ';')

        val uri = Uri.parse(normalized)
        require((uri.scheme.equals("http", true) || uri.scheme.equals("https", true)) &&
            !uri.host.isNullOrBlank()) {
            "The detected URL is invalid. Only HTTP or HTTPS URLs are supported."
        }

        val path = uri.lastPathSegment.orEmpty().lowercase(Locale.ROOT)
        val type = when {
            path.endsWith(".m3u8") -> "HLS playlist (.m3u8)"
            path.endsWith(".mpd") -> "MPEG-DASH manifest (.mpd)"
            path.endsWith(".mp4") -> "MP4 video"
            path.endsWith(".m4v") -> "M4V video"
            path.endsWith(".webm") -> "WebM video"
            path.endsWith(".mov") -> "MOV video"
            else -> "Embed/page URL (not confirmed as direct media)"
        }
        val direct = listOf(".m3u8", ".mpd", ".mp4", ".m4v", ".webm", ".mov")
            .any { path.endsWith(it) }

        ParsedVideoInput(normalized, direct, type)
    }
}
