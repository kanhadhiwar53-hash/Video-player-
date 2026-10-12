package com.example.streamplayer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.streamplayer.databinding.ActivityMainBinding
import com.kanha.extractor.ExtractorManager
import com.kanha.extractor.toPlayerItem
import com.kanha.player.PlayerActivity
import com.kanha.player.PlayerItem
import com.kanha.player.VideoSource
import kotlinx.coroutines.launch

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
        EmbedParser.parse(binding.urlInput.text?.toString().orEmpty()).onSuccess { parsed ->
            detectedUrl = parsed.url
            isDirectMedia = parsed.isDirectMedia
            binding.resultUrl.text = parsed.url
            binding.resultCard.visibility = View.VISIBLE
            binding.statusText.text = if (parsed.isDirectMedia)
                "Detected: ${parsed.mediaType}. Tap Play to start the native player."
            else "Embed/page URL found. Tap Play to try a supported source or open the provider player."
        }.onFailure { error ->
            detectedUrl = null
            binding.resultCard.visibility = View.GONE
            binding.statusText.text = error.message ?: "Could not parse the input."
        }
    }
    private fun playDetectedUrl() {
        val url = detectedUrl ?: run { toast("Extract a URL first."); return }
        if (isDirectMedia) {
            PlayerActivity.start(this, arrayListOf(PlayerItem(url.hashCode().toString(), "Stream Player",
                listOf(VideoSource("Direct source", url)))))
            return
        }
        binding.statusText.text = "Checking the page for supported media sources…"
        binding.playButton.isEnabled = false
        lifecycleScope.launch {
            try {
                val result = ExtractorManager.load(url)
                if (result.links.isEmpty()) {
                    binding.statusText.text = "No direct source found. Opening provider player."
                    startActivity(Intent(this@MainActivity, EmbedWebViewActivity::class.java)
                        .putExtra(EmbedWebViewActivity.EXTRA_URL, url))
                } else {
                    binding.statusText.text = "Found ${result.links.size} source(s)."
                    PlayerActivity.start(this@MainActivity,
                        arrayListOf(result.toPlayerItem(url.hashCode().toString(), "Stream Player")))
                }
            } catch (error: Exception) {
                binding.statusText.text = "Source extraction unavailable: ${error.message ?: "unsupported page"}"
                startActivity(Intent(this@MainActivity, EmbedWebViewActivity::class.java)
                    .putExtra(EmbedWebViewActivity.EXTRA_URL, url))
            } finally { binding.playButton.isEnabled = true }
        }
    }
    private fun copyDetectedUrl() {
        val url = detectedUrl ?: run { toast("Extract a URL first."); return }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Video URL", url))
        toast("Link copied")
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
