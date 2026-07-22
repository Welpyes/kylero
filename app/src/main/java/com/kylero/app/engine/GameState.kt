package com.kylero.app.engine

import com.kylero.app.Note

data class ActiveNote(
    val note: Note,
    var state: NoteState = NoteState.UPCOMING,
    var hitTimeMs: Long = 0L,
    var holdStartMs: Long = 0L
)

enum class NoteState {
    UPCOMING,
    ACTIVE,
    HIT,
    MISS
}

data class GameState(
    var score: Int = 0,
    var combo: Int = 0,
    var maxCombo: Int = 0,
    var perfectCount: Int = 0,
    var goodCount: Int = 0,
    var missCount: Int = 0,
    val activeNotes: MutableList<ActiveNote> = mutableListOf()
) {
    val accuracy: Float
        get() {
            val total = perfectCount + goodCount + missCount
            if (total == 0) return 100f
            return ((perfectCount * 300f + goodCount * 100f) / (total * 300f)) * 100f
        }

    fun reset(notes: List<Note>) {
        score = 0
        combo = 0
        maxCombo = 0
        perfectCount = 0
        goodCount = 0
        missCount = 0
        activeNotes.clear()
        notes.forEach { activeNotes.add(ActiveNote(note = it)) }
    }

    fun hitNote(activeNote: ActiveNote, isPerfect: Boolean) {
        activeNote.state = NoteState.HIT
        activeNote.hitTimeMs = System.currentTimeMillis()
        combo++
        if (combo > maxCombo) maxCombo = combo
        if (isPerfect) {
            perfectCount++
            score += 300 * combo
        } else {
            goodCount++
            score += 100 * combo
        }
    }

    fun missNote(activeNote: ActiveNote) {
        activeNote.state = NoteState.MISS
        combo = 0
        missCount++
    }
}
