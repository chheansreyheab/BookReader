package com.example.bookreader.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.data.FileItem

class Utils {

    /**
     * Scan a specific folder URI for PDF and EPUB books
     */
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

    /**
     * Open a book using external apps (PDF/EPUB)
     */
    fun openBook(context: Context, uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, when {
                    uriString.endsWith(".pdf", true) -> "application/pdf"
                    uriString.endsWith(".epub", true) -> "application/epub+zip"
                    else -> "*/*"
                })
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Scan all device files for PDF and EPUB books (MediaStore)
     */
    fun getAllDeviceBooks(context: Context): List<Book> {
        val books = mutableListOf<Book>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )

        val selection = """
            ${MediaStore.Files.FileColumns.MIME_TYPE}=? OR
            ${MediaStore.Files.FileColumns.MIME_TYPE}=?
        """.trimIndent()

        val selectionArgs = arrayOf(
            "application/pdf",
            "application/epub+zip"
        )

        val uri = MediaStore.Files.getContentUri("external")

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val contentUri = ContentUris.withAppendedId(uri, id)

                books.add(
                    Book(
                        title = name.substringBeforeLast("."),
                        author = "Unknown",
                        coverRes = R.drawable.ic_folder,
                        currentRead = 0,
                        totalRead = 0,
                        uriString = contentUri.toString()
                    )
                )
            }
        }

        return books
    }


}
