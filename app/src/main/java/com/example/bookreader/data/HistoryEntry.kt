package com.example.bookreader.data

data class HistoryEntry(
    val book: Book,
    val timestamp: Long // store epoch millis
)
