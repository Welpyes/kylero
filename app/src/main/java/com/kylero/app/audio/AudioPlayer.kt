package com.kylero.app.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class AudioPlayer(private val context: Context) {

    private var player: MediaPlayer? = null
    private var prepared = false

    val durationMs: Long
        get() = player?.duration?.toLong() ?: 0L

    val currentPositionMs: Long
        get() = player?.currentPosition?.toLong() ?: 0L

    val isPlaying: Boolean
        get() = player?.isPlaying == true

    fun prepare(uri: Uri, onReady: () -> Unit = {}, onError: (String) -> Unit = {}) {
        release()
        player = MediaPlayer().apply {
            setDataSource(context, uri)
            setOnPreparedListener {
                prepared = true
                onReady()
            }
            setOnErrorListener { _, what, extra ->
                onError("MediaPlayer error: $what, $extra")
                true
            }
            prepareAsync()
        }
    }

    fun start() {
        if (prepared) player?.start()
    }

    fun pause() {
        if (player?.isPlaying == true) player?.pause()
    }

    fun resume() {
        if (prepared && !isPlaying) player?.start()
    }

    fun seekTo(ms: Int) {
        player?.seekTo(ms)
    }

    fun release() {
        player?.release()
        player = null
        prepared = false
    }
}
