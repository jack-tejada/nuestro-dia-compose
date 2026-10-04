package com.madrigalsolu.nuestrodia.compose.feature.gallery.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Comment
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Photo
import kotlinx.coroutines.CancellationException
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

  private fun requireFirebaseConfigured() {
    check(isFirebaseAvailable) {
      "Firebase no está configurado. Agrega app/google-services.json y vuelve a intentarlo."
    }
  }

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
              val photos = snapshot.documents
                .filter { it.getString("uploadStatus") != "pending" }
                .mapNotNull { doc ->
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
      val remoteEvent = try {
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
      } catch (error: CancellationException) {
        throw error
      } catch (_: Exception) {
        null
      }
      if (remoteEvent != null) return remoteEvent
    }
    return eventStore.getEvent(eventId)
  }

  override suspend fun deletePhoto(photo: Photo, currentUserId: String): Result<Unit> {
    if (photo.ownerUid != currentUserId) {
      return Result.failure(IllegalAccessException("Solo el autor puede eliminar esta fotografía."))
    }

    return try {
      requireFirebaseConfigured()
      require(photo.storagePath.isNotBlank()) { "No se encontró la ruta de almacenamiento de la fotografía." }
      try {
        FirebaseStorage.getInstance()
          .reference
          .child(photo.storagePath)
          .delete()
          .await()
      } catch (error: StorageException) {
        if (error.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw error
      }

      FirebaseFirestore.getInstance()
        .collection("events")
        .document(photo.eventId)
        .collection("photos")
        .document(photo.id)
        .delete()
        .await()

      eventStore.deletePhoto(photo.id, currentUserId)
      Result.success(Unit)
    } catch (error: CancellationException) {
      throw error
    } catch (error: Exception) {
      Result.failure(error)
    }
  }

  override suspend fun toggleLike(photo: Photo, userId: String): Result<Photo> {
    if (userId.isBlank()) {
      return Result.failure(IllegalArgumentException("Inicia sesión para indicar que te gusta esta fotografía."))
    }

    return try {
      requireFirebaseConfigured()
      val firestore = FirebaseFirestore.getInstance()
      val photoReference = firestore
        .collection("events")
        .document(photo.eventId)
        .collection("photos")
        .document(photo.id)
      val updatedPhoto = firestore.runTransaction { transaction ->
        val snapshot = transaction.get(photoReference)
        check(snapshot.exists()) { "No se encontró la fotografía." }
        val currentUids = (snapshot.get("likedByUids") as? List<*>)
          ?.filterIsInstance<String>()
          ?.distinct()
          .orEmpty()
        val alreadyLiked = userId in currentUids
        val updatedUids = if (alreadyLiked) currentUids.filterNot { it == userId } else currentUids + userId
        transaction.update(
          photoReference,
          mapOf("likedByUids" to updatedUids, "likesCount" to updatedUids.size),
        )
        photo.copy(likedByUids = updatedUids, likesCount = updatedUids.size)
      }.await()

      eventStore.addPhoto(updatedPhoto)
      Result.success(updatedPhoto)
    } catch (error: CancellationException) {
      throw error
    } catch (error: Exception) {
      Result.failure(error)
    }
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
    val normalizedText = text.trim()
    if (authorUid.isBlank() || normalizedText.isBlank()) {
      return Result.failure(IllegalArgumentException("Ingresa un comentario y asegúrate de iniciar sesión."))
    }

    return try {
      requireFirebaseConfigured()
      val comment = Comment(
        id = UUID.randomUUID().toString(),
        photoId = photoId,
        authorUid = authorUid,
        authorName = authorName.ifBlank { "Invitado" },
        text = normalizedText,
        timestamp = System.currentTimeMillis(),
      )
      FirebaseFirestore.getInstance()
        .collection("events")
        .document(eventId)
        .collection("photos")
        .document(photoId)
        .collection("comments")
        .document(comment.id)
        .set(comment)
        .await()

      eventStore.addComment(comment)
      Result.success(comment)
    } catch (error: CancellationException) {
      throw error
    } catch (error: Exception) {
      Result.failure(error)
    }
  }
}
