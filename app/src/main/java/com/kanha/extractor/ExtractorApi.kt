package com.kanha.extractor

import com.kanha.player.SubtitleSource

data class ExtractedLink(
    val name: String,
    val url: String,
    val referer: String? = null,
    val quality: Int = 0,
    val headers: Map<String, String> = emptyMap(),
    val isM3u8: Boolean = false,
)

data class ExtractResult(
    val links: List<ExtractedLink>,
    val subtitles: List<SubtitleSource> = emptyList(),
) {
    companion object { val EMPTY = ExtractResult(emptyList()) }
}

abstract class ExtractorApi {
    abstract val name: String
    abstract fun matches(url: String): Boolean
    abstract suspend fun getUrl(url: String, referer: String? = null): ExtractResult
}

object ExtractorManager {
    private val extractors = mutableListOf<ExtractorApi>(DirectLinkExtractor(), SampleHostExtractor())
    private val fallback = GenericPageExtractor()

    fun register(extractor: ExtractorApi) { extractors.add(0, extractor) }

    suspend fun load(url: String, referer: String? = null): ExtractResult {
        val uri = android.net.Uri.parse(url)
        require((uri.scheme == "https" || uri.scheme == "http") && !uri.host.isNullOrBlank()) {
            "Only valid HTTP/HTTPS URLs are supported."
        }
        val extractor = extractors.firstOrNull { it.matches(url) } ?: fallback
        val result = runCatching { extractor.getUrl(url, referer) }.getOrDefault(ExtractResult.EMPTY)
        if (result.links.isNotEmpty() || extractor === fallback) return result
        return runCatching { fallback.getUrl(url, referer) }.getOrDefault(ExtractResult.EMPTY)
    }
}
