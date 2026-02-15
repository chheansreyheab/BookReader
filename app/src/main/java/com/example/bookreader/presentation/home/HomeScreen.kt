package com.example.bookreader.presentation.home

import Preferences
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.bookreader.data.Category
import com.example.bookreader.presentation.book_info.DetailBookInfo
import com.example.bookreader.presentation.navigator.Screen
import com.example.bookreader.utils.Utils

object HomeScreen : Screen {

    var selectedBook by mutableStateOf<Book?>(null)
    var continueReading by mutableStateOf(listOf<Book>())

    @RequiresApi(Build.VERSION_CODES.R)
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)?) {

        val context = LocalContext.current
        val prefs = Preferences(context)

        // --- Device books ---
        var localBooks by remember { mutableStateOf(listOf<Book>()) }

        // --- Permission & scanning state ---
        var hasPermission by remember { mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()) }
        var showPermissionDialog by remember { mutableStateOf(!hasPermission) }
        var scanning by remember { mutableStateOf(false) }

        // --- Observe lifecycle to detect returning from settings ---
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val granted = Environment.isExternalStorageManager()
                        if (granted && !hasPermission) {
                            hasPermission = true
                            showPermissionDialog = false
                        }
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        // --- Scan books when permission granted ---
        LaunchedEffect(hasPermission) {
            if (hasPermission && localBooks.isEmpty()) {
                scanning = true
                val books = Utils().getAllDeviceBooks(context)
                localBooks = books
                val continueUris = prefs.getContinueReading()
                continueReading = books.filter { continueUris.contains(it.uriString) }
                scanning = false
            }
        }

        // --- Main UI ---
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize()) {

                // --- Top AppBar always visible ---
                TopAppBar(
                    title = { Text("Library", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(painter = painterResource(R.drawable.ic_search), contentDescription = "Search")
                        }
                        Image(
                            painter = painterResource(R.drawable.profile_placeholder),
                            contentDescription = "Profile",
                            modifier = Modifier.padding(end = 12.dp).size(36.dp).clip(CircleShape)
                        )
                    },
                    windowInsets = WindowInsets(0)

                )

                Spacer(modifier = Modifier.height(8.dp))

                // --- Content ---
                when {
                    showPermissionDialog -> {
                        RequestPermissionDialog(
                            onGrant = {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                intent.data = Uri.parse("package:${context.packageName}")
                                context.startActivity(intent)
                            }
                        )
                    }
                    else -> {
                        // Main content
                        Column(modifier = Modifier.fillMaxSize()) {
                            GoalCard(currentBooks = continueReading.size, totalBooks = 10)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Continue Reading
                            Text(
                                "Continue Reading",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(continueReading) { book ->
                                    ContinueReadingCard(book) { clickedBook ->
                                        selectedBook = clickedBook
                                        onNavigate?.invoke(DetailBookInfo)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Categories
                            val categories = listOf(
                                Category("Fiction"),
                                Category("Self-help"),
                                Category("Science"),
                                Category("History")
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { category -> FilterChip(category.name) }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // All Books Grid
                            Text(
                                "All Books",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
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
                                        if (!continueReading.any { it.title == clickedBook.title }) {
                                            continueReading = continueReading + clickedBook
                                        }
                                        onNavigate?.invoke(DetailBookInfo)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Scanning overlay ---
            AnimatedVisibility(
                visible = scanning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.width(250.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Scanning your device for books...",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
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
            Image(
                painter = painterResource(book.coverRes),
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(100.dp).height(140.dp).clip(RoundedCornerShape(20.dp))
            )
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
            Image(
                painter = painterResource(book.coverRes),
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.height(120.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
            )
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
