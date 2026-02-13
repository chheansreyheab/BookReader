package com.example.bookreader.presentation.main

import BookReaderTheme
import HomeScreen
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowInsetsControllerCompat
import com.example.bookreader.R
import com.example.bookreader.presentation.history.HistoryScreen
import com.example.bookreader.presentation.navigator.BottomNavItem
import com.example.bookreader.presentation.setting.SettingScreen

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookReaderTheme(dynamicColor = false) {
                MainScreen()
            }
        }

    }
}


@Composable
fun MainScreen() {
    val view = LocalView.current
    val color = MaterialTheme.colorScheme.surfaceContainer

    SideEffect {
        val window = (view.context as Activity).window
        window.navigationBarColor = color.toArgb()

        WindowInsetsControllerCompat(window, view)
            .isAppearanceLightNavigationBars = true
    }
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,// Background color of the bar
                contentColor = MaterialTheme.colorScheme.onSurface// Default color for items

            ) {
                items.forEach { item ->
                    val isSelected = item == selectedItem
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedItem = item },
                        label = { Text(stringResource(item.title)) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = if (isSelected) item.selectedIcon else item.unselectedIcon
                                ),
                                contentDescription = stringResource(item.tooltip)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },


    ) { padding ->
        Box(modifier = Modifier.padding(padding,
            )
            ) {
            selectedItem.screen.Content()
        }
    }

}




