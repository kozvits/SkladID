package com.kozvits.skladid.presentation.capture

import androidx.lifecycle.ViewModel
import com.kozvits.skladid.data.local.PhotoFileFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

enum class CaptureStep { ITEM_PHOTO, TAG_PHOTO, DONE }

data class CaptureUiState(
    val step: CaptureStep = CaptureStep.ITEM_PHOTO,
    val itemPhotoPath: String? = null,
    val tagPhotoPath: String? = null,
    val previewPath: String? = null // set right after a shot, before the user confirms/retakes
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val photoFileFactory: PhotoFileFactory
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState

    fun newOutputFile(): File =
        photoFileFactory.newPhotoFile(if (_uiState.value.step == CaptureStep.ITEM_PHOTO) "item" else "tag")

    fun onPhotoTaken(path: String) {
        _uiState.value = _uiState.value.copy(previewPath = path)
    }

    fun onRetake() {
        _uiState.value = _uiState.value.copy(previewPath = null)
    }

    /** Confirms the current preview, advances to the next step (or DONE after the tag photo). */
    fun onConfirm(): CaptureUiState {
        val current = _uiState.value
        val confirmedPath = current.previewPath ?: return current

        val updated = when (current.step) {
            CaptureStep.ITEM_PHOTO -> current.copy(
                itemPhotoPath = confirmedPath,
                previewPath = null,
                step = CaptureStep.TAG_PHOTO
            )
            CaptureStep.TAG_PHOTO -> current.copy(
                tagPhotoPath = confirmedPath,
                previewPath = null,
                step = CaptureStep.DONE
            )
            CaptureStep.DONE -> current
        }
        _uiState.value = updated
        return updated
    }
}
