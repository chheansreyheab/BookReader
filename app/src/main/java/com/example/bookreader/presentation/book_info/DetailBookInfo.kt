package com.example.bookreader.presentation.book_info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookreader.R
import com.example.bookreader.data.HistoryEntry
import com.example.bookreader.data.toImageBitmap
import com.example.bookreader.presentation.home.HomeScreen
import com.example.bookreader.presentation.home.HomeScreen.ProgressReading
import com.example.bookreader.presentation.home.HomeViewModel
import com.example.bookreader.presentation.navigator.Screen

object DetailBookInfo : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)?) {
        val book = HomeScreen.selectedBook ?: return
        val context = LocalContext.current
        val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            TopAppBar(
                title = { Text("Book Details", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate?.invoke(HomeScreen) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Cover & Info ---
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically // <-- center the details relative to cover
            ) {
                // Book Cover
                if (book.coverBytes != null) {
                    val bitmap = remember(book.coverBytes) { book.coverBytes!!.toImageBitmap() }
                    Image(
                        bitmap = bitmap!!,
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(150.dp)
                            .height(220.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(book.coverRes ?: R.drawable.ic_book),
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(150.dp)
                            .height(220.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Book Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center, // internal spacing
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Icon(
                            painter = painterResource(R.drawable.ic_person),
                            contentDescription = "Rating",
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    ProgressReading(book.currentRead, book.totalRead)
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            // --- Action Buttons Full Width ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // space between buttons
            ) {
                BookActionButton(
                    iconRes = R.drawable.ic_book,
                    label = "Read",
                    backgroundColor = Color(0xFF2196F3),
                    contentColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    onClick = {
                        viewModel.addToContinueReading(book)
                        viewModel.addToHistory(book)

                        val uri = Uri.parse(book.uriString)
                        val intent = Intent(context, PdfViewerActivity::class.java).apply {
                            putExtra(PdfViewerActivity.EXTRA_URI, uri)
                        }
                        context.startActivity(intent)
                    }
                )




                BookActionButton(
                    iconRes = R.drawable.ic_headphone,
                    label = "Play",
                    backgroundColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    onClick = { /* play audio */ }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // --- Description Text below buttons ---
            Text(
                text = book.description.ifBlank { "No description" },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

        }
    }

    @Composable
    fun BookActionButton(
        iconRes: Int,
        label: String,
        onClick: () -> Unit,
        backgroundColor: Color = Color.LightGray,
        contentColor: Color = Color.Black,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = contentColor
            )
        }
    }
}
