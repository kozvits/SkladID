package com.kozvits.skladid.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Capture : Screen("capture")
    data object Recognition : Screen("recognition")
    data object Storage : Screen("storage")
    data object LabelPreview : Screen("label_preview")

    /** Printing an already-saved product from the Home list, identified by its Room id. */
    data object LabelPreviewExisting : Screen("label_preview/{productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun createRoute(productId: Long) = "label_preview/$productId"
    }

    data object Settings : Screen("settings")
}
