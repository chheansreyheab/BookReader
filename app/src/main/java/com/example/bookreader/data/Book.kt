package com.example.bookreader.data

data class Book(
    val title: String,
    val author: String,
    val coverRes: Int,
    val currentRead: Int,
    val totalRead: Int,
    val uriString: String
)

