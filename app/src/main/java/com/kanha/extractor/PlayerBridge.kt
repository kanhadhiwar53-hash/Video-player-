package com.kanha.extractor

import com.kanha.player.PlayerItem
import com.kanha.player.VideoSource

fun ExtractedLink.toVideoSource() = VideoSource(name = name, url = url, headers = headers, referer = referer)
fun ExtractResult.toPlayerItem(id: String, title: String) = PlayerItem(
    id = id, title = title, sources = links.map { it.toVideoSource() }, subtitles = subtitles
)
