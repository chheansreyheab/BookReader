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


