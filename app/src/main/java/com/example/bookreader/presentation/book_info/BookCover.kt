package com.example.bookreader.presentation.book_info

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.data.toImageBitmap

@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier) {
    val coverBitmap = remember(book.coverBytes) { book.coverBytes?.toImageBitmap() }

    if (coverBitmap != null) {
        Image(
            bitmap = coverBitmap,
            contentDescription = book.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(book.coverRes ?: R.drawable.ic_folder),
            contentDescription = book.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}