package com.example.bookreader.presentation.setting

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bookreader.R
import com.example.bookreader.core.ThemeMode
import com.example.bookreader.core.ThemePreference
import com.example.bookreader.presentation.about.AboutScreen
import com.example.bookreader.presentation.about.ReadingPreferencesScreen
import com.example.bookreader.presentation.browse.BrowseScan
import com.example.bookreader.presentation.home.GoalActivity
import com.example.bookreader.presentation.library.LibraryScreen
import com.example.bookreader.presentation.navigator.Screen
import kotlinx.coroutines.launch

object SettingScreen : Screen {

    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)) {
        SettingScreenContent(onNavigate)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingScreenContent(onNavigate: ((Screen) -> Unit)?) {

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var showThemeDialog by remember { mutableStateOf(false) }
        val themePreference = remember { ThemePreference(context) }
        val themeMode by themePreference.themeFlow
            .collectAsState(initial = ThemeMode.SYSTEM)
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

            // Theme item
            SettingItem(
                icon = painterResource(id = R.drawable.ic_theme),
                title = "Theme",
                subtitle = themeMode.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                onClick = { showThemeDialog = true }
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
                subtitle = "Font size and text",
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
                    context.startActivity(
                        Intent(context, GoalActivity::class.java)
                    )
                }
            )

            SettingItem(
                icon = painterResource(id = R.drawable.ic_info),
                title = "About",
                subtitle = "App version and information",
                onClick = { onNavigate?.invoke(AboutScreen) }
            )
        }

        if (showThemeDialog) {
            ChangeThemeDialog(
                currentTheme = themeMode,
                onThemeSelected = { selected ->
                    scope.launch {
                        themePreference.saveTheme(selected)
                    }
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }
    }


    @Composable
    fun ChangeThemeDialog(
        currentTheme: ThemeMode,
        onThemeSelected: (ThemeMode) -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeOption("Light", ThemeMode.LIGHT, currentTheme, onThemeSelected)
                    ThemeOption("Dark", ThemeMode.DARK, currentTheme, onThemeSelected)
                    ThemeOption("System Default", ThemeMode.SYSTEM, currentTheme, onThemeSelected)
                }
            },
            confirmButton = {}
        )
    }

    @Composable
    private fun ThemeOption(
        title: String,
        mode: ThemeMode,
        currentTheme: ThemeMode,
        onThemeSelected: (ThemeMode) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onThemeSelected(mode) }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = currentTheme == mode,
                onClick = { onThemeSelected(mode) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title)
        }
    }
}
