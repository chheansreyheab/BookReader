package com.example.bookreader.presentation.browse

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bookreader.R
import com.example.bookreader.presentation.navigator.Screen
import com.example.bookreader.presentation.setting.SettingScreen

object BrowseScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)?) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.browse),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate?.invoke(SettingScreen) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )

            // This Column fills the rest of the screen and centers content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f), // ← takes remaining space after TopAppBar
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_document_scanner),
                    contentDescription = "Scan Document",
                    modifier = Modifier.size(100.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)

                )

                Text(
                    text = "Expand your library with your downloaded books!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Discover new titles and add your favorites today.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))


                Button(
                    onClick = { onNavigate?.invoke(BrowseScan) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Set up scanning")
                }
            }
        }
    }


}

