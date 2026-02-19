package com.example.bookreader.presentation.home


import Preferences
import Utils
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.Book
import com.example.bookreader.data.HistoryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application
    private val prefs = Preferences(context)

    private val _localBooks = MutableStateFlow<List<Book>>(emptyList())
    val localBooks: StateFlow<List<Book>> = _localBooks

    private val _continueReading = MutableStateFlow<List<Book>>(emptyList())
    val continueReading: StateFlow<List<Book>> = _continueReading

    private val _history = mutableStateListOf<HistoryEntry>()
    val history: List<HistoryEntry> get() = _history

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning

    private val _newBooksCount = MutableStateFlow(0)
    val newBooksCount: StateFlow<Int> = _newBooksCount

    var goal by mutableStateOf(prefs.getGoal())
        private set

    var isFirstLaunch by mutableStateOf(prefs.isFirstLaunch())
        private set

    private var hasScannedOnce = false

    val hasPermission: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()

    // --- Combine UI state ---
    val uiState: StateFlow<HomeUiState> = combine(
        _localBooks, _continueReading
    ) { books, continueBooks ->
        HomeUiState.Content(localBooks = books, continueReading = continueBooks)
    }.stateIn(viewModelScope, SharingStarted.Lazily, HomeUiState.Loading)

    init { checkAndScan() }

    private fun checkAndScan() {
        if (!hasPermission) return
        if (!hasScannedOnce) scanDeviceBooks()
    }

    fun scanDeviceBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            _scanning.value = true
            _newBooksCount.value = 0

            val existingUris = _localBooks.value.map { it.uriString }.toMutableSet()
            val continueUris = prefs.getContinueReading()
            val historyData = prefs.getHistory()

            val scannedBooks = Utils().getAllDeviceBooks(context)

            // --- Filter only new books ---
            val newBooks = scannedBooks.filter { it.uriString !in existingUris }

            if (newBooks.isNotEmpty()) {
                // --- append new books to local list without removing old books ---
                _localBooks.value = _localBooks.value + newBooks

                // --- add to continue reading if in prefs ---
                val updatedContinueReading = _continueReading.value.toMutableList()
                newBooks.forEach { book ->
                    if (continueUris.contains(book.uriString)) {
                        updatedContinueReading.add(book)
                    }
                }
                _continueReading.value = updatedContinueReading

                // --- update history for all books (old + new) ---
                _history.clear()
                historyData.sortedByDescending { it.second }.forEach { (uri, timestamp) ->
                    _localBooks.value.find { it.uriString == uri }?.let { book ->
                        _history.add(HistoryEntry(book, timestamp))
                    }
                }

                _newBooksCount.value = newBooks.size
            }

            _scanning.value = false
            hasScannedOnce = true
        }
    }

    fun addToContinueReading(book: Book) {
        if (!_continueReading.value.any { it.uriString == book.uriString }) {
            _continueReading.value = _continueReading.value + book
            prefs.addToContinueReading(book.uriString)
        }
    }

    fun addToHistory(book: Book) {
        val timestamp = System.currentTimeMillis()
        _history.removeAll { it.book.uriString == book.uriString }
        _history.add(0, HistoryEntry(book, timestamp))
        prefs.addToHistory(book.uriString, timestamp)
    }

    fun updateGoal(newGoal: Int) {
        goal = newGoal
        prefs.saveGoal(newGoal)
    }

    fun markFirstLaunchDone() {
        isFirstLaunch = false
        prefs.setFirstLaunchDone()
    }

    fun onPermissionGranted() {
        if (!hasScannedOnce) scanDeviceBooks()
    }
}




