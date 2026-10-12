package com.example.streamplayer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.streamplayer.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra(EXTRA_URL)
        if (url.isNullOrBlank()) {
            showError("No video URL was provided.")
            return
        }
        initializePlayer(url)
    }

    private fun initializePlayer(url: String) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    showError("Playback failed. The source may be unavailable, unsupported, require authorization, or use protected playback. Error: ${error.errorCodeName}")
                }
            })
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    private fun showError(message: String) {
        binding.playerError.visibility = View.VISIBLE
        binding.playerError.text = message
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    companion object {
        const val EXTRA_URL = "video_url"
    }
}
