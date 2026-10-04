package com.madrigalsolu.nuestrodia.compose.feature.gallery.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Comment
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Photo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface GalleryRepository {
  fun getPhotos(eventId: String): Flow<List<Photo>>
  suspend fun getEvent(eventId: String): Event
  suspend fun deletePhoto(photo: Photo, currentUserId: String): Result<Unit>
  suspend fun toggleLike(photo: Photo, userId: String): Result<Photo>
  fun getComments(photoId: String, eventId: String = "boda-principal"): Flow<List<Comment>>
  suspend fun addComment(photoId: String, eventId: String = "boda-principal", authorUid: String, authorName: String, text: String): Result<Comment>
}

class DefaultGalleryRepository(
  private val context: Context? = null,
  private val eventStore: InMemoryEventPhotoStore = InMemoryEventPhotoStore,
) : GalleryRepository {

  private val isFirebaseAvailable: Boolean
    get() = runCatching {
      context != null && FirebaseApp.getApps(context.applicationContext).isNotEmpty()
    }.getOrDefault(false)

  override fun getPhotos(eventId: String): Flow<List<Photo>> {
    if (isFirebaseAvailable) {
      return callbackFlow {
        val firestore = FirebaseFirestore.getInstance()
        val registration = firestore.collection("events")
          .document(eventId)
          .collection("photos")
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .addSnapshotListener { snapshot, error ->
            if (error != null) {
              // Fallback to local store on listener error
              trySend(eventStore.currentPhotos(eventId))
              return@addSnapshotListener
            }
            if (snapshot != null) {
              val photos = snapshot.documents.mapNotNull { doc ->
                runCatching {
                  @Suppress("UNCHECKED_CAST")
                  val likedList = (doc.get("likedByUids") as? List<String>).orEmpty()
                  Photo(
                    id = doc.getString("id") ?: doc.id,
                    eventId = doc.getString("eventId") ?: eventId,
                    ownerUid = doc.getString("ownerUid") ?: "",
                    ownerName = doc.getString("ownerName") ?: "Invitado",
                    uriString = doc.getString("uriString") ?: "",
                    storagePath = doc.getString("storagePath") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    width = doc.getLong("width")?.toInt() ?: 1200,
                    height = doc.getLong("height")?.toInt() ?: 800,
                    aspectRatio = doc.getDouble("aspectRatio")?.toFloat() ?: 1.5f,
                    likesCount = doc.getLong("likesCount")?.toInt() ?: likedList.size,
                    likedByUids = likedList,
                  )
                }.getOrNull()
              }
              // Sync local store
              photos.forEach { eventStore.addPhoto(it) }
              trySend(photos)
            }
          }
        awaitClose { registration.remove() }
      }
    }
    return eventStore.getPhotosFlow(eventId)
  }

  override suspend fun getEvent(eventId: String): Event {
    if (isFirebaseAvailable) {
      val remoteEvent = runCatching {
        val doc = FirebaseFirestore.getInstance()
          .collection("events")
          .document(eventId)
          .get()
          .await()
        if (doc.exists()) {
          Event(
            id = doc.getString("id") ?: eventId,
            name = doc.getString("name") ?: "Boda de Sofía y Mateo",
            date = doc.getString("date") ?: "24 de Octubre de 2026",
            code = doc.getString("code") ?: "SOFIA-MATEO-2026",
          )
        } else null
      }.getOrNull()
      if (remoteEvent != null) return remoteEvent
    }
    return eventStore.getEvent(eventId)
  }

  override suspend fun deletePhoto(photo: Photo, currentUserId: String): Result<Unit> {
    if (photo.ownerUid != currentUserId) {
      return Result.failure(IllegalAccessException("Solo el autor puede eliminar esta fotografía."))
    }

    if (isFirebaseAvailable) {
      runCatching {
        FirebaseFirestore.getInstance()
          .collection("events")
          .document(photo.eventId)
          .collection("photos")
          .document(photo.id)
          .delete()
          .await()

        FirebaseStorage.getInstance()
          .reference
          .child(photo.storagePath)
          .delete()
          .await()
      }
    }

    val deleted = eventStore.deletePhoto(photo.id, currentUserId)
    return if (deleted) {
      Result.success(Unit)
    } else {
      Result.failure(IllegalStateException("No se pudo encontrar la fotografía para eliminar."))
    }
  }

  override suspend fun toggleLike(photo: Photo, userId: String): Result<Photo> {
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

    if (isFirebaseAvailable) {
      runCatching {
        FirebaseFirestore.getInstance()
          .collection("events")
          .document(photo.eventId)
          .collection("photos")
          .document(photo.id)
          .update(
            mapOf(
              "likedByUids" to updatedUids,
              "likesCount" to updatedUids.size,
            ),
          )
          .await()
      }
    }

    eventStore.toggleLike(photo.id, userId)
    return Result.success(updatedPhoto)
  }

  override fun getComments(photoId: String, eventId: String): Flow<List<Comment>> {
    if (isFirebaseAvailable) {
      return callbackFlow {
        val registration = FirebaseFirestore.getInstance()
          .collection("events")
          .document(eventId)
          .collection("photos")
          .document(photoId)
          .collection("comments")
          .orderBy("timestamp", Query.Direction.ASCENDING)
          .addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
              val comments = snapshot.documents.mapNotNull { doc ->
                runCatching {
                  Comment(
                    id = doc.getString("id") ?: doc.id,
                    photoId = doc.getString("photoId") ?: photoId,
                    authorUid = doc.getString("authorUid") ?: "",
                    authorName = doc.getString("authorName") ?: "Invitado",
                    text = doc.getString("text") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L,
                  )
                }.getOrNull()
              }
              trySend(comments)
            }
          }
        awaitClose { registration.remove() }
      }
    }
    return eventStore.getCommentsFlow(photoId)
  }

  override suspend fun addComment(
    photoId: String,
    eventId: String,
    authorUid: String,
    authorName: String,
    text: String,
  ): Result<Comment> {
    val comment = Comment(
      id = UUID.randomUUID().toString(),
      photoId = photoId,
      authorUid = authorUid,
      authorName = authorName.ifBlank { "Invitado" },
      text = text.trim(),
      timestamp = System.currentTimeMillis(),
    )

    if (isFirebaseAvailable) {
      runCatching {
        FirebaseFirestore.getInstance()
          .collection("events")
          .document(eventId)
          .collection("photos")
          .document(photoId)
          .collection("comments")
          .document(comment.id)
          .set(comment)
          .await()
      }
    }

    eventStore.addComment(comment)
    return Result.success(comment)
  }
}
