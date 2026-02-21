package com.example.bookreader

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookreader.data.BookEntity
import com.example.bookreader.data.BookDao

@Database(entities = [BookEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}