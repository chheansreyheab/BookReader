package com.example.bookreader.data

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap

data class Book(
    val title: String,
    val author: String,
    val coverRes: Int? = null,
    val coverBytes: ByteArray? = null,
    var currentRead: Int = 0,
    val totalRead: Int = 0,
    val uriString: String,
    val description: String = "No description",
    val scrollProgress: Float = 0f
)


fun ByteArray.toImageBitmap() =
    BitmapFactory.decodeByteArray(this, 0, this.size)?.asImageBitmap()

