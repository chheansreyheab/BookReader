package com.example.bookreader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val uriString: String,
    val title: String,
    val author: String,
    val coverRes: Int?,
    val coverBytes: ByteArray?,
    val currentRead: Int,
    val totalRead: Int,
    val description: String
)


