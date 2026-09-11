package com.kozvits.skladid.presentation.capture

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kozvits.skladid.R
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileResults
import android.graphics.BitmapFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CaptureScreen(
    onCaptureComplete: (itemPhotoPath: String, tagPhotoPath: String) -> Unit,
    onBack: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(uiState.step) {
        if (uiState.step == CaptureStep.DONE) {
            onCaptureComplete(uiState.itemPhotoPath.orEmpty(), uiState.tagPhotoPath.orEmpty())
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!cameraPermissionState.status.isGranted) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.capture_permission_required))
                }
                return@Scaffold
            }

            val previewPath = uiState.previewPath
            if (previewPath != null) {
                PhotoPreview(
                    photoPath = previewPath,
                    onRetake = viewModel::onRetake,
                    onConfirm = { viewModel.onConfirm() }
                )
            } else {
                CameraCaptureView(
                    stepLabel = if (uiState.step == CaptureStep.ITEM_PHOTO) {
                        stringResource(R.string.capture_photo_item)
                    } else {
                        stringResource(R.string.capture_photo_tag)
                    },
                    outputFile = { viewModel.newOutputFile() },
                    onPhotoTaken = viewModel::onPhotoTaken
                )
            }
        }
    }
}

@Composable
private fun CameraCaptureView(
    stepLabel: String,
    outputFile: () -> java.io.File,
    onPhotoTaken: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val capture = ImageCapture.Builder().build()
                    imageCapture = capture

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        capture
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stepLabel, color = androidx.compose.ui.graphics.Color.White)
            FloatingActionButton(onClick = {
                val capture = imageCapture ?: return@FloatingActionButton
                val file = outputFile()
                val options = OutputFileOptions.Builder(file).build()
                capture.takePicture(
                    options,
                    ContextCompat.getMainExecutor(context),
                    object : OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: OutputFileResults) {
                            onPhotoTaken(file.absolutePath)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            // Surfaced to the user as a generic capture failure; retry is just
                            // tapping the shutter again since no state has changed.
                        }
                    }
                )
            }) {
                Icon(Icons.Filled.Camera, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PhotoPreview(
    photoPath: String,
    onRetake: () -> Unit,
    onConfirm: () -> Unit
) {
    val bitmap = remember(photoPath) { BitmapFactory.decodeFile(photoPath)?.asImageBitmap() }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Close, contentDescription = null)
                Text(stringResource(R.string.capture_retake))
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.capture_confirm))
            }
        }
    }
}
