package com.example.bookreader.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemePreference(private val context: Context) {

    private val THEME_KEY = stringPreferencesKey("theme_mode")

    val themeFlow: Flow<ThemeMode> =
        context.dataStore.data.map { preferences ->
            ThemeMode.valueOf(
                preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name
            )
        }

    suspend fun saveTheme(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }
}