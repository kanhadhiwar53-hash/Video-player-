package com.example.streamplayer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.streamplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var detectedUrl: String? = null
    private var isDirectMedia = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.extractButton.setOnClickListener { extract() }
        binding.playButton.setOnClickListener { playDetectedUrl() }
        binding.copyButton.setOnClickListener { copyDetectedUrl() }
    }

    private fun extract() {
        val result = EmbedParser.parse(binding.urlInput.text?.toString().orEmpty())
        result.onSuccess { parsed ->
            detectedUrl = parsed.url
            isDirectMedia = parsed.isDirectMedia
            binding.resultUrl.text = parsed.url
            binding.resultCard.visibility = View.VISIBLE
            binding.statusText.text = if (parsed.isDirectMedia) {
                "Detected: ${parsed.mediaType}. Tap Play to try playback."
            } else {
                "Embed/page URL found. Play will open the provider's own player in WebView."
            }
        }.onFailure { error ->
            detectedUrl = null
            binding.resultCard.visibility = View.GONE
            binding.statusText.text = error.message ?: "Could not parse the input."
        }
    }

    private fun playDetectedUrl() {
        val url = detectedUrl ?: run {
            toast("Extract a URL first.")
            return
        }
        if (isDirectMedia) {
            startActivity(Intent(this, VideoPlayerActivity::class.java)
                .putExtra(VideoPlayerActivity.EXTRA_URL, url))
        } else {
            startActivity(Intent(this, EmbedWebViewActivity::class.java)
                .putExtra(EmbedWebViewActivity.EXTRA_URL, url))
        }
    }

    private fun copyDetectedUrl() {
        val url = detectedUrl ?: run {
            toast("Extract a URL first.")
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Video URL", url))
        toast("Link copied")
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
