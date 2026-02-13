import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookreader.R
import com.example.bookreader.data.Book
import com.example.bookreader.data.Category
import com.example.bookreader.presentation.navigator.Screen

object HomeScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.library),
                        style = MaterialTheme.typography.titleLarge
                    )

                },
                windowInsets = WindowInsets(0),
                /*navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = "Menu"
                        )
                    }
                },*/
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "Search"
                        )
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
            GoalCard(currentBooks = 1, totalBooks = 4)
            Spacer(modifier = Modifier.height(16.dp))

            // Continue Reading
            Text(
                text = "Continue Reading",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp)
            )

            val continueReadingBooks = listOf(
                Book("The Alchemist", "Paulo Coelho", R.drawable.book_cover_placeholder, 1, 5),
                Book("1984", "George Orwell", R.drawable.book_cover_placeholder, 3, 10),
                Book("Sapiens", "Yuval Noah Harari", R.drawable.book_cover_placeholder, 2, 8)
            )


            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(continueReadingBooks) { book ->
                    ContinueReadingCard(book)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))


            // Categories Filter Chips
            val categories = listOf(
                Category("Local default"),
                Category("Fiction"),
                Category("Self-help"),
                Category("Science"),
                Category("History")
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(category.name)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))


            // Popular Books Section
            Text(
                text = "Popular Books",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(continueReadingBooks) { book ->
                    PopularBookCard(book)
                }
            }
        }
    }

    @Composable
    fun GoalCard(currentBooks: Int = 1, totalBooks: Int = 4) {
        val progress = currentBooks.toFloat() / totalBooks

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Goal",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "1/4 books",
                    style = MaterialTheme.typography.titleSmall
                )
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
                        .fillMaxWidth(fraction = progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

        }
    }

    @Composable
    fun ContinueReadingCard(book: Book) {
        Row(
            modifier = Modifier
                .width(260.dp) // Wider card for horizontal layout
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(book.coverRes),
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)

            ) {
                Text(
                    text = book.title,
                    fontSize = 16.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = book.author,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProgressReading(
                    currentRead = book.currentRead,
                    totalRead = book.totalRead
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    IconButton(onClick = {}) {
                        Column {
                            Icon(
                                painter = painterResource(R.drawable.ic_book),
                                contentDescription = "read"
                            )
                            Text(
                                text = "Read",
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )
                        }

                    }
                    IconButton(onClick = {}) {
                        Column {
                            Icon(
                                painter = painterResource(R.drawable.ic_headphone),
                                contentDescription = "play"
                            )
                            Text(
                                text = "Play",
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )
                        }

                    }

                }

            }
        }
    }

    @Composable
    fun ProgressReading(
        currentRead: Int,
        totalRead: Int
    ) {
        val progress = currentRead.toFloat() / totalRead

        Column {
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
    }


    @Composable
    fun PopularBookCard(book: Book) {
        Column(
            modifier = Modifier
                .width(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(8.dp)
        ) {
            Image(
                painter = painterResource(book.coverRes),
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(book.title, fontSize = 14.sp, maxLines = 1)
            Text(book.author, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
        }
    }

    @Composable
    fun FilterChip(name: String) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }


}
