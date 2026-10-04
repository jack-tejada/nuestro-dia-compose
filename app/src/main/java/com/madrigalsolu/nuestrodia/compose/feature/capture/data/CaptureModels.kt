package com.madrigalsolu.nuestrodia.compose.feature.capture.data

/**
 * Domain model representing an event photo.
 */
data class Photo(
  val id: String = "",
  val eventId: String = "",
  val ownerUid: String = "",
  val ownerName: String = "Invitado",
  val uriString: String = "",
  val storagePath: String = "",
  val timestamp: Long = 0L,
  val width: Int = 1200,
  val height: Int = 800,
  val aspectRatio: Float = 1.5f,
  val likesCount: Int = 0,
  val likedByUids: List<String> = emptyList(),
)

/**
 * Domain model representing a guest comment or wish on a photo.
 */
data class Comment(
  val id: String = "",
  val photoId: String = "",
  val authorUid: String = "",
  val authorName: String = "Invitado",
  val text: String = "",
  val timestamp: Long = 0L,
)

/**
 * States representing the upload lifecycle.
 */
sealed interface UploadState {
  data object Idle : UploadState
  data class Uploading(val progress: Float) : UploadState
  data class Success(val photo: Photo) : UploadState
  data class Error(val message: String, val retryable: Boolean) : UploadState
}
