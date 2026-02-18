import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.data.BookMeta
import com.example.bookreader.data.HistoryEntry
import com.tom_roush.pdfbox.pdmodel.PDDocument
import org.jsoup.Jsoup
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.zip.ZipInputStream

class Utils {

    fun ByteArray.toImageBitmap() =
        BitmapFactory.decodeByteArray(this, 0, this.size)?.asImageBitmap()

    // --------------------------------------------------
    // SAFER DEVICE BOOK SCAN
    // --------------------------------------------------

    fun getAllDeviceBooks(context: Context): List<Book> {
        val books = mutableListOf<Book>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE
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
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)
                val fileName = cursor.getString(nameColumn)
                val mimeType = cursor.getString(mimeColumn)
                val contentUri = ContentUris.withAppendedId(uri, id)

                val meta = when (mimeType) {

                    "application/pdf" ->
                        getPdfMetadata(context, contentUri, fileName)

                    "application/epub+zip" ->
                        getEpubMetadata(context, contentUri)

                    else ->
                        BookMeta(fileName, "Unknown", null, "No description")
                }

                books.add(
                    Book(
                        title = meta.title.substringBeforeLast("."),
                        author = meta.author,
                        coverRes = R.drawable.ic_book,
                        coverBytes = meta.cover,
                        currentRead = 0,
                        totalRead = 0,
                        uriString = contentUri.toString(),
                        description = meta.description
                    )
                )
            }
        }

        return books
    }

    // --------------------------------------------------
    // IMPROVED PDF METADATA
    // --------------------------------------------------

    private fun getPdfCover(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->

                PdfRenderer(descriptor).use { renderer ->

                    if (renderer.pageCount > 0) {
                        renderer.openPage(0).use { page ->

                            val bitmap = Bitmap.createBitmap(
                                page.width,
                                page.height,
                                Bitmap.Config.ARGB_8888
                            )

                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )

                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
                            return stream.toByteArray()
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun getPdfMetadata(
        context: Context,
        uri: Uri,
        fallbackName: String
    ): BookMeta {

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->

                PDDocument.load(inputStream).use { document ->

                    val info = document.documentInformation

                    val title = info.title?.takeIf { it.isNotBlank() } ?: fallbackName
                    val author = info.author?.takeIf { it.isNotBlank() } ?: "Unknown"
                    val description = info.subject?.takeIf { it.isNotBlank() }
                        ?: "No description"
                    val cover = getPdfCover(context, uri)
                    BookMeta(
                        title = title.trim(),
                        author = author.trim(),
                        cover = cover, // keep lazy loading cover
                        description = description.trim()
                    )
                }
            } ?: BookMeta(fallbackName, "Unknown", null, "No description")

        } catch (e: Exception) {
            BookMeta(fallbackName, "Unknown", null, "No description")
        }
    }


    // --------------------------------------------------
    // SAFER FILE OPENING
    // --------------------------------------------------

    fun openBook(context: Context, uriString: String) {

        val uri = Uri.parse(uriString)

        val mimeType = when {
            uriString.endsWith(".pdf", true) -> "application/pdf"
            uriString.endsWith(".epub", true) -> "application/epub+zip"
            else -> "*/*"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No app found to open this file",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    // --------------------------------------------------
    // SAFE EPUB METADATA (NO FULL MEMORY LOAD)
    // --------------------------------------------------

    private fun getEpubMetadata(
        context: Context,
        uri: Uri
    ): BookMeta {

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->

                ZipInputStream(inputStream).use { zip ->

                    var title = "Unknown"
                    var author = "Unknown"
                    var description = "No description"

                    var coverId: String? = null
                    var coverPath: String? = null
                    val entries = mutableMapOf<String, ByteArray>()

                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            entries[entry.name] = zip.readBytes()
                        }
                        entry = zip.nextEntry
                    }

                    // 1 Find container.xml
                    val containerXml = entries["META-INF/container.xml"]
                        ?.toString(Charsets.UTF_8)

                    var opfPath: String? = null

                    if (containerXml != null) {
                        val doc = Jsoup.parse(containerXml, "", org.jsoup.parser.Parser.xmlParser())
                        opfPath = doc.selectFirst("rootfile")?.attr("full-path")
                    }

                    // 2 Read OPF
                    if (opfPath != null && entries.containsKey(opfPath)) {

                        val opfContent = entries[opfPath]!!.toString(Charsets.UTF_8)
                        val doc = Jsoup.parse(opfContent, "", org.jsoup.parser.Parser.xmlParser())

                        title = doc.selectFirst("dc|title")?.text()
                            ?.takeIf { it.isNotBlank() } ?: "Unknown"

                        author = doc.selectFirst("dc|creator")?.text()
                            ?.takeIf { it.isNotBlank() } ?: "Unknown"

                        description = doc.selectFirst("dc|description")?.text()
                            ?.takeIf { it.isNotBlank() }
                            ?: "No description"

                        // 3 Find cover id
                        coverId = doc.selectFirst("meta[name=cover]")?.attr("content")

                        // 4 Find cover file path
                        val coverHref = coverId?.let {
                            doc.selectFirst("item[id=$it]")?.attr("href")
                        }

                        if (coverHref != null) {
                            val basePath = opfPath.substringBeforeLast("/", "")
                            coverPath =
                                if (basePath.isNotEmpty()) "$basePath/$coverHref"
                                else coverHref
                        }
                    }

                    val coverBytes = coverPath?.let { entries[it] }

                    return BookMeta(title, author, coverBytes, description)
                }
            }
        } catch (e: Exception) {
        }

        return BookMeta("Unknown", "Unknown", null, "No description")
    }



    fun groupHistoryByDate(history: List<HistoryEntry>): Map<String, List<HistoryEntry>> {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        return history.groupBy { entry ->
            val cal = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
            when {
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"

                cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"

                else -> dateFormat.format(Date(entry.timestamp))
            }
        }.toSortedMap(compareByDescending { it }) // newest date first
    }





}
