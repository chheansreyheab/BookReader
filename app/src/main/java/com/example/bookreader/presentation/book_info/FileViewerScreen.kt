package com.example.bookreader.presentation.book_info

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bookreader.R
import com.example.bookreader.presentation.navigator.Screen
import org.jsoup.Jsoup
import java.util.zip.ZipInputStream

object FileViewerScreen : Screen {

    var selectedFileUri: Uri? = null

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)?) {
        val context = LocalContext.current
        val uri = selectedFileUri ?: return

        var isPdf by remember { mutableStateOf(false) }
        val pdfPages = remember { mutableStateListOf<ImageBitmap>() }
        val epubPages = remember { mutableStateListOf<ImageBitmap>() }
        val epubText = remember { mutableStateListOf<String>() }

        LaunchedEffect(uri) {
            try {
                when {
                    uri.toString().endsWith(".pdf", true) -> {
                        isPdf = true
                        val pages = loadPdfPages(context, uri)
                        pdfPages.clear()
                        pdfPages.addAll(pages)
                    }
                    uri.toString().endsWith(".epub", true) -> {
                        isPdf = false
                        val (cover, texts) = loadEpub(context, uri)
                        epubPages.clear()
                        cover?.let { epubPages.add(it) }
                        epubText.clear()
                        epubText.addAll(texts)
                    }
                    else -> {
                        Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Cannot load file", Toast.LENGTH_SHORT).show()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isPdf) "PDF Reader" else "EPUB Reader") },
                    navigationIcon = {
                        IconButton(onClick = { onNavigate?.invoke(DetailBookInfo) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isPdf) {
                    pdfPages.forEach { page ->
                        Image(
                            bitmap = page,
                            contentDescription = "PDF page",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                } else {
                    // Show EPUB cover first if available
                    epubPages.forEach { page ->
                        Image(
                            bitmap = page,
                            contentDescription = "EPUB cover",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                    // Then show EPUB text
                    epubText.forEach { chapterText ->
                        Text(
                            text = chapterText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    private fun loadPdfPages(context: Context, uri: Uri): List<ImageBitmap> {
        val pages = mutableListOf<ImageBitmap>()
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    for (i in 0 until renderer.pageCount) {
                        renderer.openPage(i).use { page ->
                            val bitmap = android.graphics.Bitmap.createBitmap(
                                page.width, page.height,
                                android.graphics.Bitmap.Config.ARGB_8888
                            )
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            pages.add(bitmap.asImageBitmap())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pages
    }

    /**
     * Load EPUB: returns Pair(coverImage, List of chapter texts)
     */
    private fun loadEpub(context: Context, uri: Uri): Pair<ImageBitmap?, List<String>> {
        var coverBitmap: ImageBitmap? = null
        val texts = mutableListOf<String>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val zipEntries = mutableListOf<Pair<String, ByteArray>>()
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        zipEntries.add(entry.name to zip.readBytes())
                        entry = zip.nextEntry
                    }
                }

                // 1️⃣ Find container.xml
                val containerEntry = zipEntries.find { it.first.equals("META-INF/container.xml", true) }
                val opfPath = containerEntry?.let {
                    val doc = Jsoup.parse(String(it.second), "", org.jsoup.parser.Parser.xmlParser())
                    doc.selectFirst("rootfile")?.attr("full-path")
                }

                // 2️⃣ Parse OPF to get spine and cover
                val opfEntry = zipEntries.find { it.first == opfPath }
                if (opfEntry != null) {
                    val doc = Jsoup.parse(String(opfEntry.second), "", org.jsoup.parser.Parser.xmlParser())

                    // 2a️⃣ Extract cover image
                    val coverId = doc.selectFirst("meta[name=cover]")?.attr("content")
                        ?: doc.select("item[media-type^=image]").firstOrNull()?.attr("id")
                    val coverPath = coverId?.let { id ->
                        doc.selectFirst("item[id=$id]")?.attr("href")
                    }

                    if (coverPath != null) {
                        val coverEntry = zipEntries.find { it.first.endsWith(coverPath, true) }
                        coverEntry?.let {
                            coverBitmap = android.graphics.BitmapFactory.decodeByteArray(it.second, 0, it.second.size)?.asImageBitmap()
                        }
                    }

                    // 2b️⃣ Extract text in reading order (spine)
                    val manifest = doc.select("manifest > item").associate { it.attr("id") to it.attr("href") }
                    doc.select("spine > itemref").forEach { itemref ->
                        val idref = itemref.attr("idref")
                        val filePath = manifest[idref]
                        val fileData = zipEntries.find { it.first.endsWith(filePath ?: "", true) }?.second
                        fileData?.let {
                            val text = Jsoup.parse(String(it)).body()?.text()?.trim()
                            if (!text.isNullOrEmpty()) texts.add(text)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return coverBitmap to texts
    }
}
