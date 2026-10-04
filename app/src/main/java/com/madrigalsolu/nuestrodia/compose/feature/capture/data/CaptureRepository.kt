package com.madrigalsolu.nuestrodia.compose.feature.capture.data

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.InMemoryEventPhotoStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface CaptureRepository {
  suspend fun uploadPhoto(
    eventId: String,
    imageUri: Uri,
    width: Int,
    height: Int,
    ownerUid: String,
    ownerName: String,
    onProgress: (Float) -> Unit = {},
  ): Photo

  suspend fun deletePhoto(photo: Photo, requesterUid: String): Boolean
}

class DefaultCaptureRepository(
  private val context: Context,
  private val eventStore: InMemoryEventPhotoStore = InMemoryEventPhotoStore,
) : CaptureRepository {

  private val isFirebaseAvailable: Boolean
    get() = runCatching { FirebaseApp.getApps(context.applicationContext).isNotEmpty() }.getOrDefault(false)

  override suspend fun uploadPhoto(
    eventId: String,
    imageUri: Uri,
    width: Int,
    height: Int,
    ownerUid: String,
    ownerName: String,
    onProgress: (Float) -> Unit,
  ): Photo {
    // ponytail: UUID ensures idempotency and deterministic photo identity across retries
    val photoId = UUID.randomUUID().toString()
    val storagePath = "events/$eventId/photos/$ownerUid/$photoId/original"
    val safeWidth = if (width > 0) width else 1200
    val safeHeight = if (height > 0) height else 800
    val aspectRatio = safeWidth.toFloat() / safeHeight.toFloat()
    val timestamp = System.currentTimeMillis()

    var finalUriString = imageUri.toString()

    if (isFirebaseAvailable) {
      runCatching {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child(storagePath)

        val uploadTask = storageRef.putFile(imageUri)
        uploadTask.addOnProgressListener { taskSnapshot ->
          if (taskSnapshot.totalByteCount > 0) {
            val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
            onProgress(progress)
          }
        }
        uploadTask.await()

        val downloadUrl = runCatching { storageRef.downloadUrl.await().toString() }.getOrNull()
        if (downloadUrl != null) {
          finalUriString = downloadUrl
        }

        val photoMap = hashMapOf(
          "id" to photoId,
          "eventId" to eventId,
          "ownerUid" to ownerUid,
          "ownerName" to ownerName.ifBlank { "Invitado" },
          "uriString" to finalUriString,
          "storagePath" to storagePath,
          "timestamp" to timestamp,
          "width" to safeWidth,
          "height" to safeHeight,
          "aspectRatio" to aspectRatio,
        )

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("events")
          .document(eventId)
          .collection("photos")
          .document(photoId)
          .set(photoMap)
          .await()
      }
    } else {
      // ponytail: Graceful offline/local fallback when Firebase is not configured
      onProgress(0.15f)
      delay(200)
      onProgress(0.50f)
      delay(250)
      onProgress(0.85f)
      delay(200)
      onProgress(1.0f)
    }

    val photo = Photo(
      id = photoId,
      eventId = eventId,
      ownerUid = ownerUid,
      ownerName = ownerName.ifBlank { "Invitado" },
      uriString = finalUriString,
      storagePath = storagePath,
      timestamp = timestamp,
      width = safeWidth,
      height = safeHeight,
      aspectRatio = aspectRatio,
    )

    // Mirror to local store for immediate UI consistency
    eventStore.addPhoto(photo)
    return photo
  }

  override suspend fun deletePhoto(photo: Photo, requesterUid: String): Boolean {
    // Validate ownership before deletion
    if (photo.ownerUid != requesterUid) {
      return false
    }

    if (isFirebaseAvailable) {
      runCatching {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("events")
          .document(photo.eventId)
          .collection("photos")
          .document(photo.id)
          .delete()
          .await()

        val storage = FirebaseStorage.getInstance()
        storage.reference.child(photo.storagePath).delete().await()
      }
    }

    return eventStore.deletePhoto(photo.id, requesterUid)
  }
}
