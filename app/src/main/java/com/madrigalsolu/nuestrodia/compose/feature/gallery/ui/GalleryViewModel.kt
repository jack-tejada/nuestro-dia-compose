package com.madrigalsolu.nuestrodia.compose.feature.gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Comment
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Photo
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.Event
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.GalleryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface GalleryUiState {
  data object Loading : GalleryUiState
  data object Empty : GalleryUiState
  data class Success(val photos: List<Photo>, val isShuffled: Boolean = false) : GalleryUiState
  data class Error(val message: String) : GalleryUiState
}

class GalleryViewModel(
  private val repository: GalleryRepository,
  private val eventId: String,
  private val inactivityTimeoutMs: Long = 300_000L, // 5 minutes default
) : ViewModel() {

  private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
  val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

  private val _event = MutableStateFlow<Event?>(null)
  val event: StateFlow<Event?> = _event.asStateFlow()

  private val _selectedPhoto = MutableStateFlow<Photo?>(null)
  val selectedPhoto: StateFlow<Photo?> = _selectedPhoto.asStateFlow()

  private var rawPhotos: List<Photo> = emptyList()
  private var isShuffled = false
  private var inactivityJob: Job? = null
  private val shuffleSeed = System.currentTimeMillis()

  init {
    loadEvent()
    observePhotos()
  }

  private fun loadEvent() {
    viewModelScope.launch {
      runCatching { repository.getEvent(eventId) }
        .onSuccess { _event.value = it }
    }
  }

  private fun observePhotos() {
    viewModelScope.launch {
      repository.getPhotos(eventId)
        .catch { error ->
          _uiState.value = GalleryUiState.Error(
            error.message ?: "Ocurrió un error al cargar la galería del evento.",
          )
        }
        .collect { photos ->
          rawPhotos = photos
          updateDisplayList()
          // Update selected photo if it's currently open
          _selectedPhoto.value?.let { currentSelected ->
            photos.firstOrNull { it.id == currentSelected.id }?.let { updated ->
              _selectedPhoto.value = updated
            }
          }
          restartInactivityTimer()
        }
    }
  }

  /**
   * Resets the inactivity timer and restores newest-first order immediately on interaction.
   */
  fun onUserInteraction() {
    if (isShuffled) {
      isShuffled = false
      updateDisplayList()
    }
    restartInactivityTimer()
  }

  private fun restartInactivityTimer() {
    inactivityJob?.cancel()
    if (rawPhotos.isEmpty()) return

    inactivityJob = viewModelScope.launch {
      delay(inactivityTimeoutMs)
      // Deterministic session shuffle on 5-minute inactivity
      isShuffled = true
      updateDisplayList()
    }
  }

  private fun updateDisplayList() {
    if (rawPhotos.isEmpty()) {
      _uiState.value = GalleryUiState.Empty
      return
    }

    val orderedPhotos = if (isShuffled) {
      rawPhotos.shuffled(Random(shuffleSeed))
    } else {
      rawPhotos.sortedByDescending { it.timestamp }
    }

    _uiState.value = GalleryUiState.Success(
      photos = orderedPhotos,
      isShuffled = isShuffled,
    )
  }

  fun selectPhoto(photo: Photo?) {
    _selectedPhoto.value = photo
    onUserInteraction()
  }

  fun deletePhoto(photo: Photo, currentUserId: String, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      repository.deletePhoto(photo, currentUserId)
        .onSuccess {
          if (_selectedPhoto.value?.id == photo.id) {
            _selectedPhoto.value = null
          }
          onSuccess()
        }
        .onFailure { error ->
          _uiState.value = GalleryUiState.Error(
            error.message ?: "No se pudo eliminar la fotografía.",
          )
        }
    }
  }

  fun toggleLike(photo: Photo, userId: String) {
    viewModelScope.launch {
      repository.toggleLike(photo, userId)
        .onSuccess { updatedPhoto ->
          if (_selectedPhoto.value?.id == updatedPhoto.id) {
            _selectedPhoto.value = updatedPhoto
          }
        }
    }
  }

  fun getComments(photoId: String): Flow<List<Comment>> {
    return repository.getComments(photoId, eventId)
  }

  fun addComment(
    photoId: String,
    authorUid: String,
    authorName: String,
    text: String,
    onSuccess: () -> Unit = {},
  ) {
    if (text.isBlank()) return
    viewModelScope.launch {
      repository.addComment(photoId, eventId, authorUid, authorName, text)
        .onSuccess { onSuccess() }
    }
  }
}
