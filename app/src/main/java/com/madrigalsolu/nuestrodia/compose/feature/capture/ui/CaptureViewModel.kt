package com.madrigalsolu.nuestrodia.compose.feature.capture.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.CaptureRepository
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.UploadState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CaptureViewModel(
  private val repository: CaptureRepository,
) : ViewModel() {

  private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
  val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

  private var lastAttemptUri: Uri? = null
  private var lastWidth: Int = 0
  private var lastHeight: Int = 0
  private var lastPhotoId: String? = null
  private var activeUploadJob: Job? = null
  private var attemptGeneration = 0L
  private var activeAttempt: Long? = null

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
    if (activeUploadJob != null) return
    lastAttemptUri = imageUri
    lastWidth = width
    lastHeight = height
    val photoId = UUID.randomUUID().toString()
    lastPhotoId = photoId
    startUpload(eventId, imageUri, ownerUid, ownerName, width, height, photoId)
  }

  private fun startUpload(
    eventId: String,
    imageUri: Uri,
    ownerUid: String,
    ownerName: String,
    width: Int,
    height: Int,
    photoId: String,
  ) {
    if (activeUploadJob != null) return
    val attempt = ++attemptGeneration
    activeAttempt = attempt
    _uploadState.value = UploadState.Uploading(0.05f)
    val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
      runCatching {
        repository.uploadPhoto(
          eventId = eventId,
          photoId = photoId,
          imageUri = imageUri,
          width = width,
          height = height,
          ownerUid = ownerUid,
          ownerName = ownerName,
          onProgress = { progress ->
            if (activeAttempt == attempt) {
              _uploadState.value = UploadState.Uploading(progress)
            }
          },
        )
      }.onSuccess { photo ->
        if (activeAttempt == attempt) {
          _uploadState.value = UploadState.Success(photo)
        }
      }.onFailure { error ->
        if (error is CancellationException) throw error
        if (activeAttempt == attempt) {
          _uploadState.value = UploadState.Error(
            message = error.message ?: "No se pudo subir la foto. Revisa tu conexión e intenta de nuevo.",
            retryable = true,
          )
        }
      }
    }
    activeUploadJob = job
    job.invokeOnCompletion {
      if (activeAttempt == attempt) activeAttempt = null
      if (activeUploadJob === job) activeUploadJob = null
    }
    job.start()
  }

  fun retryUpload(eventId: String, ownerUid: String, ownerName: String) {
    if (activeUploadJob != null) return
    val uri = lastAttemptUri ?: return
    val photoId = lastPhotoId ?: return
    startUpload(
      eventId = eventId,
      imageUri = uri,
      ownerUid = ownerUid,
      ownerName = ownerName,
      width = lastWidth,
      height = lastHeight,
      photoId = photoId,
    )
  }

  fun reset() {
    ++attemptGeneration
    activeAttempt = null
    activeUploadJob?.cancel()
    lastPhotoId = null
    _uploadState.value = UploadState.Idle
  }
}
