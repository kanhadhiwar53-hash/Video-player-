package com.kanha.extractor

import com.kanha.player.SubtitleSource
import java.net.URI

private fun guessQuality(url: String): Int =
    Regex("""(\d{3,4})[pP]""").find(url)?.groupValues?.get(1)?.toIntOrNull() ?: 0
private fun absolute(base: String, child: String): String =
    runCatching { URI(base).resolve(child).toString() }.getOrDefault(child)

/** Direct media URLs and a conservative HTML source scanner for pages the user is authorized to access. */
class DirectLinkExtractor : ExtractorApi() {
    override val name = "Direct"
    override fun matches(url: String): Boolean {
        val path = url.substringBefore('?').lowercase()
        return listOf(".m3u8", ".mp4", ".mpd", ".mkv", ".webm").any(path::endsWith)
    }
    override suspend fun getUrl(url: String, referer: String?): ExtractResult =
        if (url.contains(".m3u8", true)) ExtractResult(M3u8Helper.expand(url, referer, name))
        else ExtractResult(listOf(ExtractedLink(name, url, referer, guessQuality(url))))
}

class GenericPageExtractor : ExtractorApi() {
    override val name = "Generic"
    override fun matches(url: String) = true
    private val mediaRegex = Regex("""https?://[^\s"'<>\\]+?\.(?:m3u8|mp4|mpd|mkv|webm)(?:\?[^\s"'<>\\]*)?""", RegexOption.IGNORE_CASE)
    private val subtitleRegex = Regex("""https?://[^\s"'<>\\]+?\.(?:vtt|srt)(?:\?[^\s"'<>\\]*)?""", RegexOption.IGNORE_CASE)
    private val iframeRegex = Regex("""<iframe[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)

    override suspend fun getUrl(url: String, referer: String?): ExtractResult = scan(url, referer, 0)
    private suspend fun scan(url: String, referer: String?, depth: Int): ExtractResult {
        val html = Http.get(url, referer)
        val expanded = (listOf(html) + PackedJs.unpackAll(html)).joinToString("\n")
            .replace("\\/", "/").replace("&amp;", "&").replace("\\u0026", "&")
        val media = mediaRegex.findAll(expanded).map { it.value }.toCollection(LinkedHashSet())
        val subtitleUrls = subtitleRegex.findAll(expanded).map { it.value }.toCollection(LinkedHashSet())
        val subtitles = subtitleUrls.mapIndexed { i, value -> SubtitleSource("Subtitle ${i + 1}", value, "en") }
        val links = mutableListOf<ExtractedLink>()
        for (candidate in media) {
            if (candidate.contains(".m3u8", true)) {
                links += runCatching { M3u8Helper.expand(candidate, url, name) }
                    .getOrElse { listOf(ExtractedLink(name, candidate, url, 10_000)) }
            } else {
                val q = guessQuality(candidate)
                links += ExtractedLink(if (q > 0) "$name ${q}p" else name, candidate, url, q)
            }
        }
        if (links.isEmpty() && depth < 2) {
            val frames = iframeRegex.findAll(html).map { absolute(url, it.groupValues[1]) }.distinct().take(5)
            for (frame in frames) {
                val nested = runCatching { scan(frame, url, depth + 1) }.getOrNull() ?: continue
                if (nested.links.isNotEmpty()) return nested.copy(subtitles = nested.subtitles + subtitles)
            }
        }
        return ExtractResult(links.distinctBy { it.url }.sortedByDescending { it.quality }, subtitles)
    }
}

/** Example template only. Replace the host matcher and parsing rule for a provider you are allowed to use. */
class SampleHostExtractor : ExtractorApi() {
    override val name = "SampleHost"
    override fun matches(url: String) = url.contains("example-host.com", ignoreCase = true)
    override suspend fun getUrl(url: String, referer: String?): ExtractResult {
        val html = Http.get(url, referer)
        val body = (listOf(html) + PackedJs.unpackAll(html)).joinToString("\n").replace("\\/", "/")
        val media = Regex("""file\s*:\s*["']([^"']+)["']""").find(body)?.groupValues?.get(1)
            ?: return ExtractResult.EMPTY
        return if (media.contains(".m3u8", true)) ExtractResult(M3u8Helper.expand(media, url, name))
        else ExtractResult(listOf(ExtractedLink(name, media, url, guessQuality(media))))
    }
}
