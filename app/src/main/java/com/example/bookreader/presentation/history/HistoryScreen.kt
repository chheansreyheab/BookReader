package com.example.bookreader.presentation.history

import Utils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.data.HistoryEntry
import com.example.bookreader.data.toImageBitmap
import com.example.bookreader.presentation.home.HomeViewModel
import com.example.bookreader.presentation.navigator.Screen
import java.util.*

object HistoryScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)?) {
        val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        val history = viewModel.history

        Column(modifier = Modifier.fillMaxSize()) {
            // Single TopAppBar
            TopAppBar(
                title = { Text(text = stringResource(R.string.history), style = MaterialTheme.typography.titleLarge) },
                windowInsets = WindowInsets(0),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(painter = painterResource(R.drawable.ic_search), contentDescription = "Search")
                    }
                    IconButton(onClick = {}) {
                        Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = "Delete")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // History content
            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reading history yet", color = Color.Gray)
                }
            } else {
                HistoryList(history)
            }
        }
    }

    @Composable
    fun HistoryList(history: List<HistoryEntry>) {

        // Newest first
        val sortedHistory = history.sortedByDescending { it.timestamp }

        val grouped = Utils().groupHistoryByDate(sortedHistory)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            grouped.forEach { (dateLabel, entries) ->
                item {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(entries) { entry ->
                    HistoryBookItem(entry.book)
                }
            }
        }
    }


    @Composable
    fun HistoryBookItem(book: Book) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (book.coverBytes != null) {
                val bitmap = remember(book.coverBytes) { book.coverBytes!!.toImageBitmap() }
                Image(
                    bitmap = bitmap!!,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Image(
                    painter = painterResource(book.coverRes ?: R.drawable.ic_book),
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(book.title, style = MaterialTheme.typography.bodyMedium)
                Text(book.author, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }


}
