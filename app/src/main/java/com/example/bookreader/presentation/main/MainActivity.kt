package com.example.bookreader.presentation.main

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.bookreader.R
import com.example.bookreader.presentation.browse.BrowseScreen
import com.example.bookreader.presentation.history.HistoryScreen
import com.example.bookreader.presentation.home.HomeScreen
import com.example.bookreader.presentation.library.LibraryScreen
import com.example.bookreader.presentation.navigator.BottomNavItem
import com.example.bookreader.presentation.setting.SettingScreen
import com.example.bookreader.ui.theme.BookReaderTheme

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            BookReaderTheme {
                MainScreen()
            }
        }

    }
}


@Composable
fun MainScreen() {

    val items = listOf(
        BottomNavItem(
            screen = HomeScreen,
            title = R.string.home,
            tooltip = R.string.home,
            selectedIcon = R.drawable.ic_home_filled,
            unselectedIcon = R.drawable.ic_home_outline
        ),
        BottomNavItem(
            screen = HistoryScreen,
            title = R.string.history,
            tooltip = R.string.history,
            selectedIcon = R.drawable.ic_history_filled,
            unselectedIcon = R.drawable.ic_history_outline
        ),
        BottomNavItem(
            screen = SettingScreen,
            title = R.string.setting,
            tooltip = R.string.setting,
            selectedIcon = R.drawable.ic_setting_filled,
            unselectedIcon = R.drawable.ic_setting_outline
        )
    )

    var selectedItem by remember { mutableStateOf(items.first()) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val isSelected = item == selectedItem

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedItem = item },
                        label = { Text(stringResource(item.title)) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = if (isSelected)
                                        item.selectedIcon
                                    else
                                        item.unselectedIcon
                                ),
                                contentDescription = stringResource(item.tooltip)
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            selectedItem.screen.Content()
        }
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun ScreenContent(text: String, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BookReaderTheme {
        Greeting("Android")
    }
}