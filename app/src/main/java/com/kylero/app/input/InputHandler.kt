package com.kylero.app.input

import android.view.MotionEvent

data class LaneEvent(
    val lane: Int,
    val pressed: Boolean
)

class InputHandler {

    private val events = mutableListOf<LaneEvent>()
    private val lanePressed = BooleanArray(4)

    fun onMotionEvent(event: MotionEvent, screenWidth: Int) {
        val laneWidth = screenWidth / 4
        val lane = ((event.x / laneWidth).toInt()).coerceIn(0, 3)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lanePressed[lane] = true
                synchronized(events) { events.add(LaneEvent(lane, true)) }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                for (i in 0..3) {
                    if (lanePressed[i]) {
                        lanePressed[i] = false
                        synchronized(events) { events.add(LaneEvent(i, false)) }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val newLane = ((event.x / laneWidth).toInt()).coerceIn(0, 3)
                if (newLane != lane) {
                    for (i in 0..3) {
                        if (i != newLane && lanePressed[i]) {
                            lanePressed[i] = false
                            synchronized(events) { events.add(LaneEvent(i, false)) }
                        }
                    }
                    if (!lanePressed[newLane]) {
                        lanePressed[newLane] = true
                        synchronized(events) { events.add(LaneEvent(newLane, true)) }
                    }
                }
            }
        }
    }

    fun poll(): List<LaneEvent> {
        synchronized(events) {
            val snapshot = events.toList()
            events.clear()
            return snapshot
        }
    }

    fun isLanePressed(lane: Int): Boolean = lanePressed[lane]

    fun reset() {
        lanePressed.fill(false)
        synchronized(events) { events.clear() }
    }
}
