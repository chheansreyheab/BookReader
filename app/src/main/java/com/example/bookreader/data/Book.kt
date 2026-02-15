package com.example.bookreader.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

data class Book(
    val title: String,
    val author: String,
    val coverRes: Int? = null,
    var coverBytes: ByteArray? = null,
    var coverBitmap: androidx.compose.ui.graphics.ImageBitmap? = null,
    val currentRead: Int = 0,
    val totalRead: Int = 0,
    val uriString: String
)


fun ByteArray.toImageBitmap() =
    BitmapFactory.decodeByteArray(this, 0, this.size)?.asImageBitmap()

