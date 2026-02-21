package com.example.bookreader.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    // For suspend functions
    @Query("SELECT * FROM books")
    suspend fun getAllBooksOnce(): List<BookEntity>

    // For reactive Flow
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT uriString FROM books")
    suspend fun getAllUris(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Query("DELETE FROM books WHERE uriString NOT IN (:existingUris)")
    suspend fun deleteRemovedBooks(existingUris: List<String>)

    @Query("UPDATE books SET currentRead = :current WHERE uriString = :uri")
    suspend fun updateProgress(uri: String, current: Int)
}