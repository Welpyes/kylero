package com.kylero.app.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.kylero.app.engine.GameEngine

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private var renderThread: Thread? = null
    private var running = false
    private var engine: GameEngine? = null
    private var renderer: NoteRenderer? = null
    private var onFinished: (() -> Unit)? = null

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    fun setup(engine: GameEngine, onFinished: () -> Unit) {
        this.engine = engine
        this.renderer = NoteRenderer(engine)
        this.onFinished = onFinished
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startRenderThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopRenderThread()
    }

    private fun startRenderThread() {
        running = true
        renderThread = Thread(this).apply {
            name = "GameRenderThread"
            start()
        }
    }

    fun stopRenderThread() {
        running = false
        renderThread?.let {
            try { it.join(200) } catch (_: InterruptedException) {}
        }
        renderThread = null
    }

    override fun run() {
        while (running) {
            val canvas = holder.lockCanvas() ?: continue
            try {
                val eng = engine ?: continue
                val rend = renderer ?: continue

                val state = eng.tick()
                rend.draw(canvas, state, width, height)

                if (state.finished) {
                    post { onFinished?.invoke() }
                    running = false
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        engine?.let {
            it.input.onMotionEvent(event, width)
        }
        return true
    }

    fun pause() {
        engine?.pause()
        stopRenderThread()
    }

    fun resumeGame() {
        engine?.resume()
        startRenderThread()
    }
}
