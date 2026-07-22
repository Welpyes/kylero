package com.kylero.app.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.kylero.app.engine.GameEngine
import com.kylero.app.engine.NoteState

class NoteRenderer(private val engine: GameEngine) {

    private val laneColors = intArrayOf(
        Color.rgb(100, 180, 255),
        Color.rgb(255, 100, 100),
        Color.rgb(100, 255, 100),
        Color.rgb(255, 255, 100)
    )

    private val lanePaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val dividerPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val hitZonePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val notePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val holdPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    private val comboPaint = Paint().apply {
        color = Color.WHITE
        textSize = 72f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val hitRect = RectF()

    fun draw(canvas: Canvas, state: com.kylero.app.engine.RenderState, screenWidth: Int, screenHeight: Int) {
        canvas.drawColor(Color.BLACK)

        val laneWidth = screenWidth / 4
        val hitZoneY = (screenHeight * engine.hitZoneY).toInt()

        drawLaneBackgrounds(canvas, laneWidth, screenHeight)
        drawLaneDividers(canvas, laneWidth, screenHeight)
        drawHitZone(canvas, laneWidth, hitZoneY)
        drawNotes(canvas, state, laneWidth, screenHeight, hitZoneY)
        drawHUD(canvas, state, screenWidth)
    }

    private fun drawLaneBackgrounds(canvas: Canvas, laneWidth: Int, screenHeight: Int) {
        for (i in 0..3) {
            lanePaint.color = laneColors[i]
            lanePaint.alpha = 30
            canvas.drawRect(
                (i * laneWidth).toFloat(),
                0f,
                ((i + 1) * laneWidth).toFloat(),
                screenHeight.toFloat(),
                lanePaint
            )
        }
    }

    private fun drawLaneDividers(canvas: Canvas, laneWidth: Int, screenHeight: Int) {
        for (i in 1..3) {
            canvas.drawLine(
                (i * laneWidth).toFloat(),
                0f,
                (i * laneWidth).toFloat(),
                screenHeight.toFloat(),
                dividerPaint
            )
        }
    }

    private fun drawHitZone(canvas: Canvas, laneWidth: Int, hitZoneY: Int) {
        canvas.drawLine(0f, hitZoneY.toFloat(), (laneWidth * 4).toFloat(), hitZoneY.toFloat(), hitZonePaint)
    }

    private fun drawNotes(
        canvas: Canvas,
        state: com.kylero.app.engine.RenderState,
        laneWidth: Int,
        screenHeight: Int,
        hitZoneY: Int
    ) {
        val noteWidth = laneWidth * 0.7f
        val noteHeight = 30f

        for (renderNote in state.activeNotes) {
            val x = renderNote.lane * laneWidth + (laneWidth - noteWidth) / 2f
            val y = renderNote.yProgress * hitZoneY

            if (renderNote.state == NoteState.HIT) continue

            when {
                renderNote.state == NoteState.MISS -> {
                    notePaint.alpha = 60
                    notePaint.color = Color.GRAY
                }
                renderNote.note.type == "hold" -> {
                    notePaint.alpha = 255
                    notePaint.color = laneColors[renderNote.lane]
                    holdPaint.color = laneColors[renderNote.lane]
                    holdPaint.alpha = 120

                    hitRect.set(x, y, x + noteWidth, y + renderNote.height)
                    canvas.drawRoundRect(hitRect, 8f, 8f, holdPaint)
                }
                else -> {
                    notePaint.alpha = 255
                    notePaint.color = laneColors[renderNote.lane]
                }
            }

            hitRect.set(x, y - noteHeight / 2f, x + noteWidth, y + noteHeight / 2f)
            canvas.drawRoundRect(hitRect, 12f, 12f, notePaint)
        }
    }

    private fun drawHUD(canvas: Canvas, state: com.kylero.app.engine.RenderState, screenWidth: Int) {
        textPaint.textSize = 36f
        canvas.drawText("Score: ${state.score}", 24f, 60f, textPaint)
        canvas.drawText("Acc: ${"%.1f".format(state.accuracy)}%", 24f, 110f, textPaint)

        if (state.combo > 1) {
            comboPaint.textSize = 72f
            canvas.drawText("${state.combo}x", screenWidth / 2f, 100f, comboPaint)
        }
    }
}
