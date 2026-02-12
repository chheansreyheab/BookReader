package com.example.bookreader.presentation.navigator

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BottomNavItem(
    val screen: Screen,
    @StringRes val title: Int,
    @StringRes val tooltip: Int,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int
)