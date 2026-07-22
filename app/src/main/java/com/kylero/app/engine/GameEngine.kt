package com.kylero.app.engine

import com.kylero.app.Chart
import com.kylero.app.Note
import com.kylero.app.input.InputHandler
import com.kylero.app.input.LaneEvent

data class RenderState(
    val positionMs: Long,
    val activeNotes: List<RenderNote>,
    val score: Int,
    val combo: Int,
    val accuracy: Float,
    val finished: Boolean
)

data class RenderNote(
    val note: Note,
    val yProgress: Float,
    val state: NoteState,
    val lane: Int,
    val height: Float
)

class GameEngine(
    private val chart: Chart,
    private val timing: TimingEngine,
    val input: InputHandler
) {

    val state = GameState()
    private var songDone = false

    var scrollSpeed: Float = 0.8f

    val hitZoneY: Float = 0.85f

    val totalNotes: Int = chart.notes.size

    fun start() {
        state.reset(chart.notes)
        timing.start()
        songDone = false
    }

    fun tick(): RenderState {
        val pos = timing.positionMs

        val events = input.poll()
        for (event in events) {
            handleEvent(event, pos)
        }

        checkMisses(pos)

        if (pos > chart.notes.lastOrNull()?.time_ms?.toLong()?.plus(3000) ?: 0L) {
            songDone = true
        }

        return buildRenderState(pos)
    }

    private fun handleEvent(event: LaneEvent, positionMs: Long) {
        if (!event.pressed) return

        var bestNote: ActiveNote? = null
        var bestDiff = Long.MAX_VALUE

        for (active in state.activeNotes) {
            if (active.state != NoteState.UPCOMING) continue
            if (active.note.lane != event.lane) continue

            val diff = kotlin.math.abs(active.note.time_ms.toLong() - positionMs)
            if (diff < bestDiff && diff <= 100) {
                bestDiff = diff
                bestNote = active
            }
        }

        bestNote?.let {
            val isPerfect = bestDiff <= 50
            state.hitNote(it, isPerfect)
        }
    }

    private fun checkMisses(positionMs: Long) {
        for (active in state.activeNotes) {
            if (active.state != NoteState.UPCOMING) continue
            if (positionMs - active.note.time_ms.toLong() > 100) {
                state.missNote(active)
            }
        }
    }

    private fun buildRenderState(positionMs: Long): RenderState {
        val renderNotes = mutableListOf<RenderNote>()

        for (active in state.activeNotes) {
            val note = active.note
            val noteTimeMs = note.time_ms.toLong()

            val progress = (noteTimeMs - positionMs) * scrollSpeed / 1000f
            val normalized = 1f - (progress / hitZoneY)

            if (normalized < -0.2f || normalized > 1.5f) continue

            val height = if (note.type == "hold" && note.end_ms != null) {
                val holdDuration = (note.end_ms - note.time_ms).toLong()
                (holdDuration * scrollSpeed / 1000f).coerceAtLeast(40f)
            } else {
                0f
            }

            renderNotes.add(
                RenderNote(
                    note = note,
                    yProgress = normalized.coerceIn(0f, 1f),
                    state = active.state,
                    lane = note.lane,
                    height = height
                )
            )
        }

        return RenderState(
            positionMs = positionMs,
            activeNotes = renderNotes,
            score = state.score,
            combo = state.combo,
            accuracy = state.accuracy,
            finished = songDone
        )
    }

    fun pause() {
        timing.pause()
    }

    fun resume() {
        timing.resume()
    }

    fun reset() {
        timing.reset()
        input.reset()
        state.reset(chart.notes)
        songDone = false
    }
}
