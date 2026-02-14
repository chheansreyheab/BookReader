package com.example.bookreader.data

import android.net.Uri
import com.example.bookreader.R

data class FileItem(
    val title: String,
    val author: String,
    val uriString: String
) {
    val uri: Uri get() = Uri.parse(uriString)
}
fun FileItem.toBook(): Book {
    return Book(
        title = this.title,
        author = this.author,
        coverRes = R.drawable.book_cover_placeholder, // placeholder image
        currentRead = 0,
        totalRead = 1,
        uriString = this.uriString
    )
}

