package com.example.bookreader.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.bookreader.data.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*class BookRepositoryImpl(
    private val context: Context
) : BookRepository {

    override suspend fun scanBooks(): List<Book> {
        return withContext(Dispatchers.IO) {
            val books = mutableListOf<Book>()

            val roots = context.contentResolver.persistedUriPermissions

            for (permission in roots) {
                val uri = permission.uri
                books.addAll(scanFolder(uri))
            }

            books
        }
    }

    private fun scanFolder(treeUri: Uri): List<Book> {
        val result = mutableListOf<Book>()
        val docFile = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()

        docFile.listFiles().forEach { file ->
            if (file.isFile) {
                when {
                    file.name?.endsWith(".pdf", true) == true ->
                        result.add(createPdfBook(file.uri))

                    file.name?.endsWith(".epub", true) == true ->
                        result.add(createEpubBook(file.uri))
                }
            }
        }
        return result
    }


}*/
