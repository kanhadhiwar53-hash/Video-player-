package com.kanha.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.streamplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_ITEMS = "items"
        fun start(context: Context, items: ArrayList<PlayerItem>) {
            context.startActivity(Intent(context, PlayerActivity::class.java).putExtra(EXTRA_ITEMS, items))
        }
    }
    private lateinit var b: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var trackSelector: DefaultTrackSelector
    private var items: List<PlayerItem> = emptyList()
    private var itemIndex = 0
    private var sourceIndex = 0

    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(b.root)
        items = (intent.getSerializableExtra(EXTRA_ITEMS) as? ArrayList<PlayerItem>).orEmpty()
        if (items.isEmpty() || items.first().sources.isEmpty()) { finish(); return }
        trackSelector = DefaultTrackSelector(this)
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build().also {
            b.playerView.player = it
            it.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    AlertDialog.Builder(this@PlayerActivity).setTitle("Playback error")
                        .setMessage("${error.errorCodeName}\nThis source may be unavailable or require provider authorization.")
                        .setPositiveButton("Retry") { _, _ -> it.prepare(); it.playWhenReady = true }
                        .setNegativeButton("Close", null).show()
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED && itemIndex < items.lastIndex) {
                        itemIndex++; sourceIndex = 0; playCurrent()
                    }
                }
            })
        }
        b.btnBack.setOnClickListener { finish() }
        b.btnPrev.setOnClickListener { if (itemIndex > 0) { itemIndex--; sourceIndex = 0; playCurrent() } }
        b.btnNext.setOnClickListener { if (itemIndex < items.lastIndex) { itemIndex++; sourceIndex = 0; playCurrent() } }
        b.btnRotate.setOnClickListener { b.playerView.resizeMode =
            if (b.playerView.resizeMode == 0) 4 else 0 }
        b.btnMenu.setOnClickListener { showMenu() }
        b.btnLock.setOnClickListener { b.playerView.useController = !b.playerView.useController }
        b.btnPip.setOnClickListener { toast("Picture-in-picture can be enabled by the device's app settings.") }
        playCurrent()
    }

    private fun playCurrent() {
        val item = items[itemIndex]
        val source = item.sources.getOrNull(sourceIndex) ?: return
        b.tvTitle.text = item.title
        val headers = HashMap(source.headers)
        source.referer?.let { headers["Referer"] = it }
        val factory = DefaultHttpDataSource.Factory()
            .setUserAgent(source.userAgent ?: "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true).setDefaultRequestProperties(headers)
        val media = MediaItem.Builder().setUri(source.url)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(item.title).build())
        if (source.isHls) media.setMimeType(MimeTypes.APPLICATION_M3U8)
        if (source.isDash) media.setMimeType(MimeTypes.APPLICATION_MPD)
        val subs = item.subtitles.map { s ->
            MediaItem.SubtitleConfiguration.Builder(Uri.parse(s.url))
                .setLanguage(s.language).setLabel(s.name)
                .setMimeType(if (s.url.substringBefore('?').endsWith(".vtt", true)) MimeTypes.TEXT_VTT else MimeTypes.APPLICATION_SUBRIP).build()
        }
        media.setSubtitleConfigurations(subs)
        player?.setMediaSource(DefaultMediaSourceFactory(factory).createMediaSource(media.build()))
        player?.prepare()
        player?.playWhenReady = true
    }

    private fun showMenu() {
        val options = arrayOf("Choose server", "Playback speed", "Skip intro", "Video quality")
        AlertDialog.Builder(this).setTitle("Player settings").setItems(options) { _, index ->
            when (index) {
                0 -> {
                    val names = items[itemIndex].sources.map { it.name }.toTypedArray()
                    AlertDialog.Builder(this).setTitle("Choose server").setSingleChoiceItems(names, sourceIndex) { d, i ->
                        sourceIndex = i; playCurrent(); d.dismiss()
                    }.setNegativeButton("Cancel", null).show()
                }
                1 -> {
                    val speeds = floatArrayOf(.5f, .75f, 1f, 1.25f, 1.5f, 2f)
                    AlertDialog.Builder(this).setTitle("Playback speed").setItems(speeds.map { "${it}x" }.toTypedArray()) { _, i ->
                        player?.setPlaybackSpeed(speeds[i])
                    }.show()
                }
                2 -> player?.seekTo((player?.currentPosition ?: 0L) + items[itemIndex].introSkipMs)
                3 -> {
                    val p = player ?: return@setItems
                    val groups = p.currentTracks.groups.filter { it.type == C.TRACK_TYPE_VIDEO }
                    val labels = groups.map { g -> (0 until g.length).map { g.getTrackFormat(it).height }.maxOrNull()?.let { "${it}p" } ?: "Video" }.toTypedArray()
                    if (labels.isNotEmpty()) toast("Available qualities are selectable through device/player track controls.")
                }
            }
        }.show()
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    override fun onStop() { super.onStop(); player?.pause() }
    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
}
