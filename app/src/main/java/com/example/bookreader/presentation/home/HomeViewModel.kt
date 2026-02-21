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
import androidx.room.Room
import com.example.bookreader.AppDatabase
import com.example.bookreader.data.Book
import com.example.bookreader.data.HistoryEntry
import com.example.bookreader.repository.BookRepository
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
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "book_database"
    ).build()

    private val repository = BookRepository(context, database.bookDao())
    // All scanned books
    private val _localBooks = MutableStateFlow<List<Book>>(emptyList())
    val localBooks: StateFlow<List<Book>> get() = _localBooks

    // Continue Reading
    private val _continueReading = MutableStateFlow<List<Book>>(emptyList())
    val continueReading: StateFlow<List<Book>> get() = _continueReading

    // Scanning state
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

    // History
    private val _history = mutableStateListOf<HistoryEntry>()
    val history: List<HistoryEntry> get() = _history

    // --- Combine UI state ---
    val uiState: StateFlow<HomeUiState> = combine(
        _localBooks, _continueReading
    ) { books, continueBooks ->
        HomeUiState.Content(localBooks = books, continueReading = continueBooks)
    }.stateIn(viewModelScope, SharingStarted.Lazily, HomeUiState.Loading)


    init {
        loadBooks()

        if (hasPermission) {
            scanBooks()
        }
    }

    private fun loadBooks() {
        viewModelScope.launch(Dispatchers.IO) {

            val savedBooks = repository.getSavedBooks()
            _localBooks.value = savedBooks
            loadContinueReading(savedBooks)
            loadHistory(savedBooks)   // ← ADD THIS

            if (hasPermission) {
                _scanning.value = true
                repository.smartScan()
                val updatedBooks = repository.getSavedBooks()
                _localBooks.value = updatedBooks
                loadContinueReading(updatedBooks)
                loadHistory(updatedBooks)
                _scanning.value = false
            }
        }
    }
    fun scanBooks() {
        if (!hasPermission) return

        viewModelScope.launch(Dispatchers.IO) {
            _scanning.value = true
            repository.smartScan() // Only adds new books
            _localBooks.value = repository.getSavedBooks()
            loadContinueReading(_localBooks.value)
            _scanning.value = false
        }
    }

    private fun loadContinueReading(books: List<Book>) {
        val continueUris = prefs.getContinueReading()
        _continueReading.value = books.filter { continueUris.contains(it.uriString) }
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

    private fun loadHistory(books: List<Book>) {
        val historyData = prefs.getHistory()

        _history.clear()

        historyData
            .sortedByDescending { it.second }
            .forEach { (uri, timestamp) ->
                books.find { it.uriString == uri }
                    ?.let { book ->
                        _history.add(HistoryEntry(book, timestamp))
                    }
            }
    }
    fun updateGoal(newGoal: Int) {
        goal = newGoal
        prefs.saveGoal(newGoal)
    }

    fun markFirstLaunchDone() {
        isFirstLaunch = false
        prefs.setFirstLaunchDone()
    }

}





