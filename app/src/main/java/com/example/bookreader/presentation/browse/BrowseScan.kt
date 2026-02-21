package com.example.bookreader.presentation.browse

import Preferences
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bookreader.R
import com.example.bookreader.data.FolderItem
import com.example.bookreader.presentation.navigator.Screen
import com.example.bookreader.presentation.setting.SettingScreen

object BrowseScan : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(onNavigate: ((Screen) -> Unit)) {
        val context = LocalContext.current
        val preferences = Preferences(context)

        // Load saved folders as FolderItem objects
        var folders by remember {
            mutableStateOf(preferences.getFolders().map { FolderItem.fromUri(it) })
        }

        val folderPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri: Uri? ->
            uri?.let {
                // Persist permissions for SAF folder
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                // Save folder URI persistently
                preferences.addFolder(it)

                // Refresh folder list
                folders = preferences.getFolders().map { savedUri -> FolderItem.fromUri(savedUri) }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {

            TopAppBar(
                title = { Text("Scan") },
                navigationIcon = {
                    IconButton(onClick = { onNavigate?.invoke(SettingScreen) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )

            // Folder list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                items(folders) { folder ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            painter = painterResource(R.drawable.ic_folder),
                            contentDescription = "Folder"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // Folder title
                            Text(
                                text = folder.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            // Folder path
                            Text(
                                text = folder.path,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Delete folder
                        IconButton(onClick = {
                            preferences.removeFolder(folder.uriString)
                            folders = preferences.getFolders().map { FolderItem.fromUri(it) }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { folderPickerLauncher.launch(null) }) {
                    Text("Add Folder")
                }
            }
        }
    }
}
