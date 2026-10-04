package com.madrigalsolu.nuestrodia.compose.feature.capture.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.CaptureRepository
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.UploadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaptureViewModel(
  private val repository: CaptureRepository,
) : ViewModel() {

  private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
  val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

  private var lastAttemptUri: Uri? = null
  private var lastWidth: Int = 0
  private var lastHeight: Int = 0

  /**
   * Uploads the selected photo with progress reporting.
   * ponytail: Never silently uploads; only runs upon explicit user confirmation.
   */
  fun uploadPhoto(
    eventId: String,
    imageUri: Uri,
    ownerUid: String,
    ownerName: String,
    width: Int,
    height: Int,
  ) {
    lastAttemptUri = imageUri
    lastWidth = width
    lastHeight = height

    viewModelScope.launch {
      _uploadState.value = UploadState.Uploading(0.05f)
      runCatching {
        repository.uploadPhoto(
          eventId = eventId,
          imageUri = imageUri,
          width = width,
          height = height,
          ownerUid = ownerUid,
          ownerName = ownerName,
          onProgress = { progress ->
            _uploadState.value = UploadState.Uploading(progress)
          },
        )
      }.onSuccess { photo ->
        _uploadState.value = UploadState.Success(photo)
      }.onFailure { error ->
        _uploadState.value = UploadState.Error(
          message = error.message ?: "No se pudo subir la foto. Revisá tu conexión e intentá de nuevo.",
          retryable = true,
        )
      }
    }
  }

  fun retryUpload(eventId: String, ownerUid: String, ownerName: String) {
    val uri = lastAttemptUri ?: return
    uploadPhoto(
      eventId = eventId,
      imageUri = uri,
      ownerUid = ownerUid,
      ownerName = ownerName,
      width = lastWidth,
      height = lastHeight,
    )
  }

  fun reset() {
    _uploadState.value = UploadState.Idle
  }
}
