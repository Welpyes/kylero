package com.kylero.app

import kotlinx.serialization.Serializable

@Serializable
data class Chart(
    val meta: Meta,
    val notes: List<Note>
)

@Serializable
data class Meta(
    val title: String,
    val bpm: Int,
    val offset_ms: Int,
    val keys: Int
)

@Serializable
data class Note(
    val time_ms: Int,
    val lane: Int,
    val type: String,
    val end_ms: Int? = null
)
