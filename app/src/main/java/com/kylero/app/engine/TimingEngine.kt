package com.kylero.app.engine

class TimingEngine {

    private var startNano: Long = 0L
    private var pauseNano: Long = 0L
    private var pausedAtMs: Long = 0L
    private var running: Boolean = false

    val positionMs: Long
        get() {
            if (!running) return pausedAtMs
            return (System.nanoTime() - startNano) / 1_000_000
        }

    fun start() {
        startNano = System.nanoTime()
        pausedAtMs = 0L
        running = true
    }

    fun pause() {
        if (!running) return
        pausedAtMs = (System.nanoTime() - startNano) / 1_000_000
        running = false
    }

    fun resume() {
        if (running) return
        startNano = System.nanoTime() - (pausedAtMs * 1_000_000)
        running = true
    }

    fun reset() {
        running = false
        pausedAtMs = 0L
        startNano = 0L
    }
}
