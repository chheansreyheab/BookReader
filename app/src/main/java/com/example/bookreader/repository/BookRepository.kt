package com.example.bookreader.repository

import Preferences
import Utils
import android.R.attr.delay
import android.content.Context
import com.example.bookreader.data.Book
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class BookRepository(private val context: Context) {

    // --- Return all local books instantly ---
    fun getAllLocalBooks(): List<Book> {
        // Example: scan your app-specific directory or saved books DB
        val savedBooksDir = File(context.filesDir, "books")
        if (!savedBooksDir.exists()) savedBooksDir.mkdirs()
        return savedBooksDir.listFiles()?.map { file ->
            Book(
                title = file.nameWithoutExtension,
                author = "Unknown",
                uriString = file.absolutePath,
                coverRes = null,
                coverBytes = null,
                currentRead = 0,
                totalRead = 100
            )
        } ?: emptyList()
    }

    // --- Scan for new books incrementally ---
    fun scanBooksFlow(): Flow<Book> = flow {
        val externalBooksDir = File(context.filesDir, "new_books") // simulate external source
        if (!externalBooksDir.exists()) externalBooksDir.mkdirs()

        val files = externalBooksDir.listFiles()?.toList() ?: emptyList()
        for (file in files) {
            // simulate delay for scanning
            delay(500)
            emit(
                Book(
                    title = file.nameWithoutExtension,
                    author = "Unknown",
                    uriString = file.absolutePath,
                    coverRes = null,
                    coverBytes = null,
                    currentRead = 0,
                    totalRead = 100
                )
            )
        }
    }

    // --- History storage ---
    fun addBookToHistory(book: Book) {
        // Save to database or SharedPreferences
        // Example: context.getSharedPreferences("history", Context.MODE_PRIVATE)
    }
}

