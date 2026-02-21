package com.example.bookreader.repository

import Utils
import android.content.Context
import com.example.bookreader.data.Book
import com.example.bookreader.data.BookDao
import com.example.bookreader.data.toDomain
import com.example.bookreader.data.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepository(
    private val context: Context,
    private val bookDao: BookDao
) {

    // Flow for reactive UI updates
    fun observeBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks()
            .map { list -> list.map { it.toDomain() } }
    }

    // Scan device and save only new books
    suspend fun smartScan(): Int {
        val deviceBooks = Utils().getAllDeviceBooks(context)
        val deviceUris = deviceBooks.map { it.uriString }

        val existingUris = bookDao.getAllUris()

        // New books
        val newBooks = deviceBooks.filter { it.uriString !in existingUris }

        if (newBooks.isNotEmpty()) {
            bookDao.insertBooks(newBooks.map { it.toEntity() })
        }

        // Remove deleted books
        bookDao.deleteRemovedBooks(deviceUris)

        return newBooks.size
    }

    suspend fun updateProgress(uri: String, current: Int) {
        bookDao.updateProgress(uri, current)
    }

    suspend fun getSavedBooks(): List<Book> {
        val entities = bookDao.getAllBooksOnce() // <-- suspend function
        return entities.map { it.toDomain() }
    }
}