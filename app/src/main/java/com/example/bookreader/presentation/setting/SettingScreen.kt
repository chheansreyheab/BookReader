package com.example.bookreader.presentation.setting

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bookreader.R
import com.example.bookreader.presentation.about.AboutScreen
import com.example.bookreader.presentation.about.ReadingPreferencesScreen
import com.example.bookreader.presentation.browse.BrowseScan
import com.example.bookreader.presentation.home.GoalActivity
import com.example.bookreader.presentation.library.LibraryScreen
import com.example.bookreader.presentation.navigator.Screen

object SettingScreen : Screen {

    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)) {
        SettingScreenContent(onNavigate)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingScreenContent(onNavigate: ((Screen) -> Unit)?) {
        val context = LocalContext.current
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.setting),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                windowInsets = WindowInsets(0)
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingItem(
                icon = painterResource(id = R.drawable.ic_theme),
                title = "Dark Mode",
                subtitle = "Enable or disable dark theme",
                onClick = { onNavigate?.invoke(LibraryScreen) }


            )
            SettingItem(
                icon = painterResource(id = R.drawable.ic_language),
                title = "Language",
                subtitle = "Change app language",
                onClick = { onNavigate?.invoke(LibraryScreen) }

            )
            SettingItem(
                icon = painterResource(id = R.drawable.ic_font),
                title = "Reading Preferences",
                subtitle = "font size and text",
                onClick = { onNavigate?.invoke(ReadingPreferencesScreen) }

            )
            SettingItem(
                icon = painterResource(id = R.drawable.ic_library_outline),
                title = "Library",
                subtitle = "Manage saved books",
                onClick = { onNavigate?.invoke(LibraryScreen) }


            )

            SettingItem(
                icon = painterResource(id = R.drawable.ic_browse),
                title = "Browse",
                subtitle = "Search and explore books",
                onClick = { onNavigate?.invoke(BrowseScan) }
            )
            SettingItem(
                icon = painterResource(id = R.drawable.ic_person_celebrate),
                title = "Goal",
                subtitle = "Set reading goal",
                onClick = {
                    context.startActivity(Intent(context, GoalActivity::class.java))
                    }
            )

            SettingItem(
                icon = painterResource(id = R.drawable.ic_info),
                title = "About",
                subtitle = "App version and information",
                onClick = { onNavigate?.invoke(AboutScreen) }
            )
        }
    }
}
