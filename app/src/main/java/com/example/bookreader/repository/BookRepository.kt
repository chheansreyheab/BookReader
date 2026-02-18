package com.example.bookreader.repository

import Preferences
import Utils
import android.content.Context
import com.example.bookreader.data.Book

class BookRepository(
    private val context: Context,
    private val prefs: Preferences
) {

    private val utils = Utils()

    suspend fun scanBooks(): List<Book> {
        return utils.getAllDeviceBooks(context)
    }

    fun getContinueReadingUris(): List<String> {
        return prefs.getContinueReading()
    }

    fun addToContinueReading(uri: String) {
        prefs.addToContinueReading(uri)
    }

    fun getHistory(): List<Pair<String, Long>> {
        return prefs.getHistory()
    }

    fun addToHistory(uri: String, timestamp: Long) {
        prefs.addToHistory(uri, timestamp)
    }
}

