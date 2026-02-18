package com.example.bookreader.presentation.book_info

import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bookreader.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfViewerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_URI = "extra_pdf_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pdfUri = intent.getParcelableExtra<Uri>(EXTRA_URI) ?: run {
            finish()
            return
        }

        setContent {
            PdfViewerScreenContent(pdfUri) { finish() }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PdfViewerScreenContent(pdfUri: Uri, onBack: () -> Unit) {
        val context = LocalContext.current
        val rendererState = remember { mutableStateOf<PdfRenderer?>(null) }
        val pageCount = rendererState.value?.pageCount ?: 0

        // Open PDF once
        LaunchedEffect(pdfUri) {
            rendererState.value?.close()
            rendererState.value = openPdfRenderer(context, pdfUri)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PDF Viewer") },
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                painter =
                                    painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(pageCount) { index ->
                    rendererState.value?.let { renderer ->
                        PdfPageItem(context, renderer, index)
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose { rendererState.value?.close() }
        }
    }

    @Composable
    private fun PdfPageItem(context: android.content.Context, renderer: PdfRenderer, pageIndex: Int) {
        var bitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

        LaunchedEffect(pageIndex) {
            bitmap = renderPage(context, renderer, pageIndex)
        }

        bitmap?.let { bmp ->
            Image(
                bitmap = bmp,
                contentDescription = "PDF page",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }

    private suspend fun renderPage(
        context: android.content.Context,
        renderer: PdfRenderer,
        pageIndex: Int
    ): androidx.compose.ui.graphics.ImageBitmap? = withContext(Dispatchers.IO) {
        try {
            renderer.openPage(pageIndex).use { page ->
                val metrics: DisplayMetrics = context.resources.displayMetrics
                val screenWidth = metrics.widthPixels
                val scale = screenWidth.toFloat() / page.width
                val scaledHeight = (page.height * scale).toInt()

                val bitmap = android.graphics.Bitmap.createBitmap(
                    screenWidth,
                    scaledHeight,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap.asImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun openPdfRenderer(context: android.content.Context, uri: Uri): PdfRenderer? {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.let { PdfRenderer(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
