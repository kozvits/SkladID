package com.kozvits.skladid.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Capture : Screen("capture")
    data object Recognition : Screen("recognition")
    data object Storage : Screen("storage")
    data object LabelPreview : Screen("label_preview")
    data object Settings : Screen("settings")
}
