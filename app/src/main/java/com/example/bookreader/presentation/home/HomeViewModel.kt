package com.example.bookreader.presentation.home


import Preferences
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.Book
import com.example.bookreader.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application

    // All scanned books
    private val _localBooks = MutableStateFlow<List<Book>>(emptyList())
    val localBooks: StateFlow<List<Book>> get() = _localBooks

    // Continue Reading
    private val _continueReading = MutableStateFlow<List<Book>>(emptyList())
    val continueReading: StateFlow<List<Book>> get() = _continueReading

    // Scanning state
    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> get() = _scanning

    val hasPermission: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()

    private val prefs = Preferences(context)

    init {
        if (hasPermission) scanDeviceBooks()
    }

    fun scanDeviceBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            _scanning.value = true

            val books = Utils().getAllDeviceBooks(context)
            _localBooks.value = books

            // Load Continue Reading from prefs
            val continueUris = prefs.getContinueReading()
            _continueReading.value = books.filter { continueUris.contains(it.uriString) }

            _scanning.value = false
        }
    }

    fun addToContinueReading(book: Book) {
        if (!_continueReading.value.any { it.uriString == book.uriString }) {
            val updated = _continueReading.value + book
            _continueReading.value = updated
            prefs.addToContinueReading(book.uriString)
        }
    }

}
