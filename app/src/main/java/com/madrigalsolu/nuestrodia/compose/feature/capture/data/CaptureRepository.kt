package com.madrigalsolu.nuestrodia.compose.feature.capture.data

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.InMemoryEventPhotoStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

interface CaptureRepository {
  suspend fun uploadPhoto(
    eventId: String,
    photoId: String,
    imageUri: Uri,
    width: Int,
    height: Int,
    ownerUid: String,
    ownerName: String,
    onProgress: (Float) -> Unit = {},
  ): Photo
}

class DefaultCaptureRepository(
  private val context: Context,
  private val eventStore: InMemoryEventPhotoStore = InMemoryEventPhotoStore,
) : CaptureRepository {

  override suspend fun uploadPhoto(
    eventId: String,
    photoId: String,
    imageUri: Uri,
    width: Int,
    height: Int,
    ownerUid: String,
    ownerName: String,
    onProgress: (Float) -> Unit,
  ): Photo {
    require(photoId.isNotBlank()) { "La operación de subida no tiene un identificador válido." }
    val storagePath = "events/$eventId/photos/$ownerUid/$photoId/original"
    val safeWidth = if (width > 0) width else 1200
    val safeHeight = if (height > 0) height else 800
    val aspectRatio = safeWidth.toFloat() / safeHeight.toFloat()
    val appContext = context.applicationContext
    if (FirebaseApp.getApps(appContext).isEmpty()) {
      throw IllegalStateException("Firebase no está configurado. Agrega app/google-services.json y vuelve a intentarlo.")
    }

    val firestore = FirebaseFirestore.getInstance()
    val photoDocument = firestore.collection("events")
      .document(eventId)
      .collection("photos")
      .document(photoId)
    val existing = photoDocument.get(Source.SERVER).await()
    if (existing.getString("uploadStatus") == "ready") {
      val photo = existing.toPhoto(eventId)
      eventStore.addPhoto(photo)
      onProgress(1f)
      return photo
    }

    val timestamp = existing.getLong("timestamp") ?: System.currentTimeMillis()
    val ownerName = ownerName.ifBlank { "Invitado" }
    val pendingData = mapOf(
      "id" to photoId,
      "eventId" to eventId,
      "ownerUid" to ownerUid,
      "ownerName" to ownerName,
      "storagePath" to storagePath,
      "timestamp" to timestamp,
      "width" to safeWidth,
      "height" to safeHeight,
      "aspectRatio" to aspectRatio,
      "uploadStatus" to "pending",
    )
    // The stable Firestore anchor makes interrupted uploads discoverable and retries target the same object.
    photoDocument.set(pendingData).await()

    val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)
    val uploadTask = storageRef.putFile(imageUri)
    val progressListener = OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
      if (taskSnapshot.totalByteCount > 0) {
        val storageProgress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
        onProgress(storageProgress * 0.9f)
      }
    }
    uploadTask.addOnProgressListener(progressListener)
    try {
      uploadTask.await()
    } catch (error: CancellationException) {
      uploadTask.cancel()
      throw error
    } finally {
      uploadTask.removeOnProgressListener(progressListener)
    }

    val finalUriString = storageRef.downloadUrl.await().toString()
    val photoMap = pendingData + mapOf(
      "uriString" to finalUriString,
      "uploadStatus" to "ready",
      "likesCount" to 0,
      "likedByUids" to emptyList<String>(),
    )
    photoDocument.set(photoMap).await()

    val photo = Photo(
      id = photoId,
      eventId = eventId,
      ownerUid = ownerUid,
      ownerName = ownerName,
      uriString = finalUriString,
      storagePath = storagePath,
      timestamp = timestamp,
      width = safeWidth,
      height = safeHeight,
      aspectRatio = aspectRatio,
    )
    eventStore.addPhoto(photo)
    onProgress(1f)
    return photo
  }
}

private fun DocumentSnapshot.toPhoto(fallbackEventId: String): Photo {
  val likedByUids = (get("likedByUids") as? List<*>)?.filterIsInstance<String>().orEmpty()
  return Photo(
    id = getString("id") ?: id,
    eventId = getString("eventId") ?: fallbackEventId,
    ownerUid = getString("ownerUid") ?: "",
    ownerName = getString("ownerName") ?: "Invitado",
    uriString = getString("uriString") ?: "",
    storagePath = getString("storagePath") ?: "",
    timestamp = getLong("timestamp") ?: 0L,
    width = getLong("width")?.toInt() ?: 1200,
    height = getLong("height")?.toInt() ?: 800,
    aspectRatio = getDouble("aspectRatio")?.toFloat() ?: 1.5f,
    likesCount = getLong("likesCount")?.toInt() ?: likedByUids.size,
    likedByUids = likedByUids,
  )
}
