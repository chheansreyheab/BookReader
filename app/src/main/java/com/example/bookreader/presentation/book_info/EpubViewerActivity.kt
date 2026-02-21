package com.example.bookreader.presentation.book_info

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bookreader.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipInputStream

class EpubViewerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_URI = "extra_epub_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.getParcelableExtra<Uri>(EXTRA_URI) ?: run {
            finish()
            return
        }

        setContent {
            EpubViewerScreen(uri) { finish() }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EpubViewerScreen(uri: Uri, onBack: () -> Unit) {

        val context = LocalContext.current
        var htmlFile by remember { mutableStateOf<File?>(null) }

        LaunchedEffect(uri) {
            val dir = extractEpub(context, uri)
            htmlFile = dir?.let { findFirstHtmlFile(it) }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("EPUB Reader") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->

            htmlFile?.let { file ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    factory = {
                        WebView(it).apply {
                            settings.javaScriptEnabled = true
                            settings.allowFileAccess = true
                            loadUrl("file://${file.absolutePath}")
                        }
                    }
                )
            }
        }
    }

    private suspend fun extractEpub(context: Context, uri: Uri): File? =
        withContext(Dispatchers.IO) {
            try {
                val tempDir = File(context.cacheDir, "epub_temp")
                if (tempDir.exists()) tempDir.deleteRecursively()
                tempDir.mkdirs()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    ZipInputStream(input).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            val file = File(tempDir, entry.name)
                            if (entry.isDirectory) {
                                file.mkdirs()
                            } else {
                                file.parentFile?.mkdirs()
                                file.outputStream().use { zip.copyTo(it) }
                            }
                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                    }
                }

                tempDir
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private fun findFirstHtmlFile(dir: File): File? {
        return dir.walkTopDown()
            .firstOrNull { it.extension in listOf("html", "xhtml") }
    }
}