package com.example.bookreader.presentation.book_info

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.presentation.home.HomeScreen
import com.example.bookreader.presentation.home.HomeScreen.ProgressReading
import com.example.bookreader.presentation.navigator.Screen
import com.example.bookreader.presentation.setting.SettingScreen

object DetailBookInfo : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)?) {
        Column(
            modifier = Modifier.Companion.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    /*Text(
                        text = stringResource(""),
                        style = MaterialTheme.typography.titleLarge
                    )*/
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigate?.invoke(HomeScreen)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )
            BookInfo(HomeScreen.selectedBook)

        }
    }

    @Composable
    fun BookInfo(book: Book?) {
        if (book == null) return

        Row {
            Image(
                painter = painterResource(book.coverRes),
                contentDescription = book.title,
                modifier = Modifier
                    .height(200.dp)
                    .width(150.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Text(
                    text = book.author,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                ProgressReading(book.currentRead, book.totalRead)


            }
        }

    }


}