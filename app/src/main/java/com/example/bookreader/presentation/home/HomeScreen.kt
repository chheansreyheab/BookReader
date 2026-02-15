package com.example.bookreader.presentation.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.data.toImageBitmap
import com.example.bookreader.presentation.book_info.DetailBookInfo
import com.example.bookreader.presentation.navigator.Screen
import com.example.bookreader.utils.Utils


object HomeScreen : Screen {

    var selectedBook by mutableStateOf<Book?>(null)
    var continueReading by mutableStateOf(listOf<Book>())

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)?) {

        val context = LocalContext.current
        val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

        val localBooks by viewModel.localBooks.collectAsState()
        val continueReading by viewModel.continueReading.collectAsState()
        val scanning by viewModel.scanning.collectAsState()
        val hasPermission = viewModel.hasPermission

        var showPermissionDialog by remember { mutableStateOf(!hasPermission) }

        // Lifecycle observer to check permissions when returning from settings
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val granted = Environment.isExternalStorageManager()
                        if (granted && !hasPermission) {
                            showPermissionDialog = false
                            viewModel.scanDeviceBooks()
                        }
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {

                TopAppBar(
                    title = { Text("Library", style = MaterialTheme.typography.titleLarge) },
                    actions = { /*... same as before ...*/ },
                    windowInsets = WindowInsets(0)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (showPermissionDialog) {
                    RequestPermissionDialog(
                        onGrant = {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.data = Uri.parse("package:${context.packageName}")
                            context.startActivity(intent)
                        }
                    )
                } else {
                    // --- Main content ---
                    Column(modifier = Modifier.fillMaxSize()) {
                        GoalCard(currentBooks = continueReading.size, totalBooks = 10)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Continue Reading
                        Text("Continue Reading", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(continueReading) { book ->
                                ContinueReadingCard(book) { clickedBook ->
                                    selectedBook = clickedBook
                                    viewModel.addToContinueReading(clickedBook)
                                    onNavigate?.invoke(DetailBookInfo)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // All Books Grid
                        Text("All Books", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            items(localBooks.size) { index ->
                                val book = localBooks[index]
                                LocalBookCard(book) { clickedBook ->
                                    selectedBook = clickedBook
                                    viewModel.addToContinueReading(clickedBook)
                                    onNavigate?.invoke(DetailBookInfo)
                                }
                            }
                        }
                    }
                }

                // --- Scanning overlay ---
                AnimatedVisibility(visible = scanning, enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.width(250.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Scanning your device for books...", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }


    // --- Permission Dialog ---
    @Composable
    fun RequestPermissionDialog(onGrant: () -> Unit) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Storage Permission") },
            text = { Text("Please allow access to all files to scan your books.") },
            confirmButton = { TextButton(onClick = onGrant) { Text("Grant Access") } }
        )
    }

    // --- Reusable Composables ---
    @Composable
    fun GoalCard(currentBooks: Int, totalBooks: Int) {
        val progress = currentBooks.toFloat() / totalBooks
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Goal", style = MaterialTheme.typography.titleMedium)
                Text("$currentBooks/$totalBooks books", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }

    @Composable
    fun ContinueReadingCard(book: Book, onReadClick: (Book) -> Unit) {
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .width(260.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (book.coverBytes != null) {
                val imageBitmap = remember(book.coverBytes) {
                    book.coverBytes!!.toImageBitmap()
                }

                imageBitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(100.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            } else {
                Image(
                    painter = painterResource(book.coverRes ?: R.drawable.ic_book),
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(100.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(book.author, fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                ProgressReading(book.currentRead, book.totalRead)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        onReadClick(book)
                        Utils().openBook(context, book.uriString)
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(painter = painterResource(R.drawable.ic_book), contentDescription = "Read")
                            Text("Read", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    IconButton(onClick = {}) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(painter = painterResource(R.drawable.ic_headphone), contentDescription = "Play")
                            Text("Play", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LocalBookCard(book: Book, onClick: (Book) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onClick(book) }
                .padding(8.dp)
        ) {

            if (book.coverBytes != null) {
                val imageBitmap = remember(book.coverBytes) {
                    book.coverBytes!!.toImageBitmap()
                }

                imageBitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(120.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            } else {
                Image(
                    painter = painterResource(book.coverRes ?: R.drawable.ic_folder),
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(book.title, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(book.author, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    @Composable
    fun ProgressReading(current: Int, total: Int) {
        val progress = current.toFloat() / total
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }



    @Composable
    fun FilterChip(name: String) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = name, fontSize = 14.sp, color = MaterialTheme.colorScheme.surfaceContainer)
        }
    }
}
