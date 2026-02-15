package com.example.bookreader.presentation.book_info

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.DisplayMetrics
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bookreader.R
import com.example.bookreader.presentation.navigator.Screen

object PdfViewerScreen : Screen {

    var selectedPdfUri: Uri? = null

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)?) {
        val context = LocalContext.current
        val uri = selectedPdfUri ?: return

        val pdfPages = remember { mutableStateListOf<ImageBitmap>() }

        LaunchedEffect(uri) {
            val pages = loadPdfPages(context, uri)
            pdfPages.clear()
            pdfPages.addAll(pages)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { onNavigate?.invoke(DetailBookInfo) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    },
                    windowInsets = WindowInsets(0)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                pdfPages.forEach { page ->
                    Image(
                        bitmap = page,
                        contentDescription = "PDF page",
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }


    private fun loadPdfPages(context: Context, uri: Uri): List<ImageBitmap> {
        val pages = mutableListOf<ImageBitmap>()
        try {
            val descriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return pages
            PdfRenderer(descriptor).use { renderer ->

                // Get screen width in pixels
                val metrics: DisplayMetrics = context.resources.displayMetrics
                val screenWidth = metrics.widthPixels

                for (i in 0 until renderer.pageCount) {
                    renderer.openPage(i).use { page ->

                        // Calculate scaled height to maintain aspect ratio
                        val scale = screenWidth.toFloat() / page.width
                        val scaledHeight = (page.height * scale).toInt()

                        val bitmap = android.graphics.Bitmap.createBitmap(
                            screenWidth,
                            scaledHeight,
                            android.graphics.Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        pages.add(bitmap.asImageBitmap())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pages
    }
}
