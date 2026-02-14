package com.example.bookreader.presentation.navigator

import androidx.compose.runtime.Composable

interface Screen {
    @Composable
    fun Content(onNavigate: ((Screen) -> Unit)? = null)
}
