package com.example.bookreader.data

import android.net.Uri
import android.provider.DocumentsContract


data class FolderItem(
    val title: String,
    val path: String,
    val uriString: String
) {
    val uri: Uri
        get() = Uri.parse(uriString)

    companion object {
        fun fromUri(uri: Uri): FolderItem {
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val cleanPath = docId.substringAfter(":")

            return FolderItem(
                title = cleanPath.substringAfterLast("/"),
                path = cleanPath,
                uriString = uri.toString()
            )
        }
    }
}


