package com.kanha.extractor

import java.net.URI

object M3u8Helper {
    suspend fun expand(masterUrl: String, referer: String?, name: String): List<ExtractedLink> {
        val text = Http.get(masterUrl, referer)
        val auto = ExtractedLink("$name Auto", masterUrl, referer, 10_000, isM3u8 = true)
        if (!text.contains("#EXT-X-STREAM-INF")) return listOf(auto)
        val out = mutableListOf(auto)
        val lines = text.lines()
        for (i in lines.indices) {
            if (!lines[i].startsWith("#EXT-X-STREAM-INF")) continue
            val height = Regex("RESOLUTION=\\d+x(\\d+)").find(lines[i])?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val child = lines.drop(i + 1).firstOrNull { it.isNotBlank() && !it.startsWith("#") } ?: continue
            val resolved = runCatching { URI(masterUrl).resolve(child.trim()).toString() }.getOrDefault(child.trim())
            out += ExtractedLink(if (height > 0) "$name ${height}p" else name, resolved, referer, height, isM3u8 = true)
        }
        return out
    }
}

object PackedJs {
    private val regex = Regex("""\}\('(.*?)',\s*(\d+),\s*(\d+),\s*'(.*?)'\.split\('\|'\)""", RegexOption.DOT_MATCHES_ALL)
    fun unpackAll(text: String): List<String> {
        if (!text.contains("eval(function(p,a,c,k,e,d)")) return emptyList()
        return regex.findAll(text).mapNotNull { match ->
            runCatching {
                val payload = match.groupValues[1].replace("\\'", "'").replace("\\\\", "\\")
                val radix = match.groupValues[2].toInt()
                val count = match.groupValues[3].toInt()
                val dictionary = match.groupValues[4].split("|")
                var result = payload
                for (i in count - 1 downTo 0) {
                    val word = dictionary.getOrNull(i)
                    if (!word.isNullOrEmpty()) result = result.replace(Regex("\\b" + encode(i, radix) + "\\b"), word)
                }
                result
            }.getOrNull()
        }.toList()
    }
    private fun encode(n: Int, radix: Int): String {
        val prefix = if (n < radix) "" else encode(n / radix, radix)
        val rem = n % radix
        return prefix + if (rem > 35) (rem + 29).toChar().toString() else Character.forDigit(rem, 36).toString()
    }
}
