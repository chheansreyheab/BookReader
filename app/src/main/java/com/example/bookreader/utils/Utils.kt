package com.example.bookreader.utils

import Preferences
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.data.FileItem
import com.example.bookreader.data.FolderItem

class Utils {
    fun scanFolderForBooks(context: Context, folderUri: Uri): List<FileItem> {
        val files = mutableListOf<FileItem>()

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            folderUri,
            DocumentsContract.getTreeDocumentId(folderUri)
        )

        context.contentResolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_DOCUMENT_ID
            ),
            null, null, null
        )?.use { cursor ->

            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                val documentId = cursor.getString(1)
                val fileUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, documentId)

                if (name.endsWith(".pdf", true) || name.endsWith(".epub", true)) {

                    val title = name.substringBeforeLast(".")

                    files.add(
                        FileItem(
                            title = title,
                            author = "Unknown",
                            uriString = fileUri.toString()
                        )
                    )
                }
            }
        }

        return files
    }


    fun openBook(context: Context, uriString: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            val uri = Uri.parse(uriString)

            intent.setDataAndType(uri, when {
                uriString.endsWith(".pdf", true) -> "application/pdf"
                uriString.endsWith(".epub", true) -> "application/epub+zip"
                else -> "*/*"
            })
            intent.flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Optionally show a Toast: "Cannot open file"
        }
    }



}