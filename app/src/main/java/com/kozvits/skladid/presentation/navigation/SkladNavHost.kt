package com.kozvits.skladid.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kozvits.skladid.presentation.capture.CaptureScreen
import com.kozvits.skladid.presentation.home.HomeScreen
import com.kozvits.skladid.presentation.labelpreview.LabelPreviewScreen
import com.kozvits.skladid.presentation.recognition.RecognitionScreen
import com.kozvits.skladid.presentation.settings.SettingsScreen
import com.kozvits.skladid.presentation.storage.StorageScreen

@Composable
fun SkladNavHost(navController: NavHostController = rememberNavController()) {
    val draftViewModel: ProductDraftViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddProduct = {
                    draftViewModel.reset()
                    navController.navigate(Screen.Capture.route)
                },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Capture.route) {
            CaptureScreen(
                onCaptureComplete = { itemPhotoPath, tagPhotoPath ->
                    draftViewModel.setPhotos(itemPhotoPath, tagPhotoPath)
                    navController.navigate(Screen.Recognition.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Recognition.route) {
            val draft by draftViewModel.draft.collectAsState()
            RecognitionScreen(
                itemPhotoPath = draft.itemPhotoPath.orEmpty(),
                tagPhotoPath = draft.tagPhotoPath.orEmpty(),
                onConfirmed = { name, manufacturer, category, specs, barcode, recognizedText ->
                    draftViewModel.setProductFields(name, manufacturer, category, specs)
                    draftViewModel.setRecognition(barcode, recognizedText)
                    navController.navigate(Screen.Storage.route)
                }
            )
        }

        composable(Screen.Storage.route) {
            StorageScreen(
                onConfirmed = { warehouse, rack, shelf, cell ->
                    draftViewModel.setStorageAddress(
                        com.kozvits.skladid.domain.model.StorageAddress(warehouse, rack, shelf, cell)
                    )
                    navController.navigate(Screen.LabelPreview.route)
                }
            )
        }

        composable(Screen.LabelPreview.route) {
            val draft by draftViewModel.draft.collectAsState()
            LabelPreviewScreen(
                product = draft.toProduct(),
                onPrintSuccess = {
                    draftViewModel.reset()
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
