package com.example.bookreader.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.data.FileItem
import com.tom_roush.pdfbox.pdmodel.PDDocument
import org.jsoup.Jsoup
import java.util.zip.ZipInputStream

class Utils {

    fun ByteArray.toImageBitmap() =
        BitmapFactory.decodeByteArray(this, 0, this.size)?.asImageBitmap()
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

                val metadata = if (name.endsWith(".epub", true)) {
                    getEpubMetadata(context, contentUri)
                } else {
                    val pdfMeta = getPdfMetadata(context, contentUri)
                    Triple(pdfMeta.first ?: name, pdfMeta.second ?: "Unknown", null)
                }

                books.add(
                    Book(
                        title = metadata.first.substringBeforeLast("."),
                        author = metadata.second,
                        coverRes = R.drawable.ic_book,
                        coverBytes = metadata.third,
                        currentRead = 0,
                        totalRead = 0,
                        uriString = contentUri.toString()
                    )
                )
            }
        }

        return books
    }

    fun getPdfMetadata(context: Context, uri: Uri): Pair<String?, String?> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    val title = document.documentInformation.title ?: uri.lastPathSegment
                    val author = document.documentInformation.author ?: "Unknown"
                    title to author
                }
            } ?: (null to null)
        } catch (e: Exception) {
            e.printStackTrace()
            null to null
        }
    }




    fun getEpubMetadata(context: Context, uri: Uri): Triple<String, String, ByteArray?> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val zip = ZipInputStream(inputStream)
                var title = "Unknown"
                var author = "Unknown"
                var coverBytes: ByteArray? = null
                var opfContent: String? = null

                // Find OPF
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith(".opf")) {
                        opfContent = zip.readBytes().toString(Charsets.UTF_8)
                        break
                    }
                    entry = zip.nextEntry
                }

                if (opfContent != null) {
                    val doc = Jsoup.parse(opfContent, "", org.jsoup.parser.Parser.xmlParser())
                    title = doc.getElementsByTag("title").firstOrNull()?.text() ?: "Unknown"
                    author = doc.getElementsByTag("creator").firstOrNull()?.text() ?: "Unknown"

                    val coverId = doc.selectFirst("meta[name=cover]")?.attr("content")
                    val coverPath = coverId?.let { doc.selectFirst("item[id=$it]")?.attr("href") }
                        ?: doc.select("item[media-type^=image]").firstOrNull()?.attr("href")

                    if (coverPath != null) {
                        context.contentResolver.openInputStream(uri)?.use { secondStream ->
                            val secondZip = ZipInputStream(secondStream)
                            var secondEntry = secondZip.nextEntry
                            while (secondEntry != null) {
                                if (secondEntry.name.endsWith(coverPath)) {
                                    coverBytes = secondZip.readBytes()
                                    break
                                }
                                secondEntry = secondZip.nextEntry
                            }
                        }
                    }
                }

                Triple(title, author, coverBytes)
            } ?: Triple("Unknown", "Unknown", null)
        } catch (e: Exception) {
            e.printStackTrace()
            Triple("Unknown", "Unknown", null)
        }
    }











}
