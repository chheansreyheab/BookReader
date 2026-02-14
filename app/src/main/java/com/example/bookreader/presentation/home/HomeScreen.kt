package com.example.bookreader.presentation.home

import Preferences
import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.data.Category
import com.example.bookreader.data.FileItem
import com.example.bookreader.data.toBook
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

        // Load scanned local books
        var localBooks by remember { mutableStateOf(scanAllLocalBooks(context)) }

        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.library),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                windowInsets = WindowInsets(0),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(painter = painterResource(R.drawable.ic_search), contentDescription = "Search")
                    }
                    Image(
                        painter = painterResource(R.drawable.profile_placeholder),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                }
            )

            GoalCard(currentBooks = continueReading.size, totalBooks = 10)

            Spacer(modifier = Modifier.height(16.dp))

            // Continue Reading
            Text(
                text = "Continue Reading",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
            val categories = listOf(Category("Fiction"), Category("Self-help"), Category("Science"), Category("History"))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(category.name)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Local Books
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Local Books", style = MaterialTheme.typography.titleMedium)
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(localBooks) { fileItem ->
                    LocalBookCard(fileItem.toBook()) { clickedBook ->
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

    @Composable
    fun GoalCard(currentBooks: Int, totalBooks: Int) {
        val progress = currentBooks.toFloat() / totalBooks
        Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
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
                        onReadClick(book) // update progress
                        Utils().openBook(context, book.uriString) // open the actual file
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
                .width(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(8.dp)
                .clickable { onClick(book) }
        ) {
            Image(
                painter = painterResource(book.coverRes),
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.height(160.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp))
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

    // Helper: scan all local books from saved folders
    private fun scanAllLocalBooks(context: Context): List<FileItem> {
        val prefs = Preferences(context)
        return prefs.getFolders().flatMap { folderUri ->
            Utils().scanFolderForBooks(context, folderUri)
        }
    }
}
