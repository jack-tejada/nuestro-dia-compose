package com.madrigalsolu.nuestrodia.compose.feature.gallery.data

import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Comment
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory source of truth for events, photos, likes, and comments.
 * // ponytail: Single in-memory store acts as local fallback when Firebase Storage & Firestore are not yet configured.
 */
object InMemoryEventPhotoStore {
  private val defaultEvent = Event(
    id = "boda-principal",
    name = "Boda de Sofía y Mateo",
    date = "24 de Octubre de 2026",
    code = "SOFIA-MATEO-2026",
  )

  private val _events = MutableStateFlow<Map<String, Event>>(mapOf(defaultEvent.id to defaultEvent))
  private val _photos = MutableStateFlow<List<Photo>>(emptyList())
  private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())

  fun getPhotosFlow(eventId: String): Flow<List<Photo>> {
    return _photos.map { list -> list.filter { it.eventId == eventId } }
  }

  fun getEvent(eventId: String): Event {
    return _events.value[eventId] ?: defaultEvent.copy(id = eventId)
  }

  fun addPhoto(photo: Photo) {
    _photos.value = listOf(photo) + _photos.value.filter { it.id != photo.id }
  }

  fun deletePhoto(photoId: String, requesterUid: String): Boolean {
    val photo = _photos.value.firstOrNull { it.id == photoId } ?: return false
    if (photo.ownerUid != requesterUid) {
      return false
    }
    _photos.value = _photos.value.filter { it.id != photoId }
    return true
  }

  fun currentPhotos(eventId: String): List<Photo> {
    return _photos.value.filter { it.eventId == eventId }
  }

  fun toggleLike(photoId: String, userId: String): Photo? {
    val photo = _photos.value.firstOrNull { it.id == photoId } ?: return null
    val alreadyLiked = photo.likedByUids.contains(userId)
    val updatedUids = if (alreadyLiked) {
      photo.likedByUids.filter { it != userId }
    } else {
      photo.likedByUids + userId
    }
    val updatedPhoto = photo.copy(
      likedByUids = updatedUids,
      likesCount = updatedUids.size,
    )
    _photos.value = _photos.value.map { if (it.id == photoId) updatedPhoto else it }
    return updatedPhoto
  }

  fun getCommentsFlow(photoId: String): Flow<List<Comment>> {
    return _comments.map { map -> map[photoId].orEmpty() }
  }

  fun addComment(comment: Comment) {
    val currentList = _comments.value[comment.photoId].orEmpty()
    _comments.value = _comments.value + (comment.photoId to (currentList + comment))
  }
}
