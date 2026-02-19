package com.example.bookreader.presentation.home

import com.example.bookreader.data.Book

sealed class HomeUiState {
    object Loading : HomeUiState()
    object PermissionRequired : HomeUiState()
    data class Content(val localBooks: List<Book>, val continueReading: List<Book>) : HomeUiState()
}
