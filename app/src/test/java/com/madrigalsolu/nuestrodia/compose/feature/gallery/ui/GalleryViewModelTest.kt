package com.madrigalsolu.nuestrodia.compose.feature.gallery.ui

import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Comment
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Photo
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.Event
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.GalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @Before fun setMainDispatcher() = Dispatchers.setMain(dispatcher)
  @After fun resetMainDispatcher() = Dispatchers.resetMain()

  @Test fun emptyRepositoryYieldsEmptyUiState() = runTest(dispatcher) {
    val fakeRepo = FakeGalleryRepository()
    val viewModel = GalleryViewModel(repository = fakeRepo, eventId = "boda-1")
    advanceUntilIdle()

    assertEquals(GalleryUiState.Empty, viewModel.uiState.value)
  }

  @Test fun photosAreOrderedNewestFirstByTimestamp() = runTest(dispatcher) {
    val older = Photo(
      id = "1",
      eventId = "boda-1",
      ownerUid = "u1",
      ownerName = "Ana",
      uriString = "uri://1",
      storagePath = "path/1",
      timestamp = 1000L,
      width = 1200,
      height = 800,
      aspectRatio = 1.5f,
    )
    val newer = Photo(
      id = "2",
      eventId = "boda-1",
      ownerUid = "u2",
      ownerName = "Carlos",
      uriString = "uri://2",
      storagePath = "path/2",
      timestamp = 2000L,
      width = 1200,
      height = 800,
      aspectRatio = 1.5f,
    )

    val fakeRepo = FakeGalleryRepository(listOf(older, newer))
    val viewModel = GalleryViewModel(repository = fakeRepo, eventId = "boda-1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is GalleryUiState.Success)
    val success = state as GalleryUiState.Success
    assertEquals(listOf("2", "1"), success.photos.map { it.id })
    assertEquals(false, success.isShuffled)
  }

  @Test fun deletingPhotoClearsSelection() = runTest(dispatcher) {
    val photo = Photo(
      id = "1",
      eventId = "boda-1",
      ownerUid = "u1",
      ownerName = "Ana",
      uriString = "uri://1",
      storagePath = "path/1",
      timestamp = 1000L,
      width = 1200,
      height = 800,
      aspectRatio = 1.5f,
    )
    val fakeRepo = FakeGalleryRepository(listOf(photo))
    val viewModel = GalleryViewModel(repository = fakeRepo, eventId = "boda-1")
    advanceUntilIdle()

    viewModel.selectPhoto(photo)
    assertEquals(photo, viewModel.selectedPhoto.value)

    viewModel.deletePhoto(photo, "u1")
    advanceUntilIdle()

    assertEquals(null, viewModel.selectedPhoto.value)
  }

  @Test fun toggleLikeUpdatesLikesCountAndLikedUser() = runTest(dispatcher) {
    val photo = Photo(
      id = "p1",
      eventId = "boda-1",
      ownerUid = "u1",
      ownerName = "Ana",
      uriString = "uri://1",
      storagePath = "path/1",
      timestamp = 1000L,
      likesCount = 0,
      likedByUids = emptyList(),
    )
    val fakeRepo = FakeGalleryRepository(listOf(photo))
    val viewModel = GalleryViewModel(repository = fakeRepo, eventId = "boda-1")
    advanceUntilIdle()

    viewModel.selectPhoto(photo)

    // First toggle: like added
    viewModel.toggleLike(photo, "u2")
    advanceUntilIdle()

    val likedPhoto = viewModel.selectedPhoto.value!!
    assertEquals(1, likedPhoto.likesCount)
    assertTrue(likedPhoto.likedByUids.contains("u2"))

    // Second toggle: like removed
    viewModel.toggleLike(likedPhoto, "u2")
    advanceUntilIdle()

    val unlikedPhoto = viewModel.selectedPhoto.value!!
    assertEquals(0, unlikedPhoto.likesCount)
    assertEquals(false, unlikedPhoto.likedByUids.contains("u2"))
  }

  @Test fun addingCommentAppearsInCommentsFlow() = runTest(dispatcher) {
    val photo = Photo(
      id = "p1",
      eventId = "boda-1",
      ownerUid = "u1",
      ownerName = "Ana",
      uriString = "uri://1",
      storagePath = "path/1",
      timestamp = 1000L,
    )
    val fakeRepo = FakeGalleryRepository(listOf(photo))
    val viewModel = GalleryViewModel(repository = fakeRepo, eventId = "boda-1")
    advanceUntilIdle()

    viewModel.addComment(
      photoId = "p1",
      authorUid = "u2",
      authorName = "Carlos",
      text = "Muchas felicidades a los novios!",
    )
    advanceUntilIdle()

    val comments = viewModel.getComments("p1").first()
    assertEquals(1, comments.size)
    assertEquals("Muchas felicidades a los novios!", comments[0].text)
    assertEquals("Carlos", comments[0].authorName)
  }
}

private class FakeGalleryRepository(
  initialPhotos: List<Photo> = emptyList(),
) : GalleryRepository {
  private val photosFlow = MutableStateFlow(initialPhotos)
  private val commentsFlow = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())

  override fun getPhotos(eventId: String): Flow<List<Photo>> = photosFlow

  override suspend fun getEvent(eventId: String): Event = Event(
    id = eventId,
    name = "Boda de Prueba",
    date = "10/10/2026",
    code = "TEST-2026",
  )

  override suspend fun deletePhoto(photo: Photo, currentUserId: String): Result<Unit> {
    photosFlow.value = photosFlow.value.filter { it.id != photo.id }
    return Result.success(Unit)
  }

  override suspend fun toggleLike(photo: Photo, userId: String): Result<Photo> {
    val alreadyLiked = photo.likedByUids.contains(userId)
    val updatedUids = if (alreadyLiked) {
      photo.likedByUids.filter { it != userId }
    } else {
      photo.likedByUids + userId
    }
    val updated = photo.copy(
      likedByUids = updatedUids,
      likesCount = updatedUids.size,
    )
    photosFlow.value = photosFlow.value.map { if (it.id == photo.id) updated else it }
    return Result.success(updated)
  }

  override fun getComments(photoId: String, eventId: String): Flow<List<Comment>> {
    return commentsFlow.map { it[photoId].orEmpty() }
  }

  override suspend fun addComment(
    photoId: String,
    eventId: String,
    authorUid: String,
    authorName: String,
    text: String,
  ): Result<Comment> {
    val comment = Comment(
      id = "c-${System.currentTimeMillis()}",
      photoId = photoId,
      authorUid = authorUid,
      authorName = authorName,
      text = text,
      timestamp = System.currentTimeMillis(),
    )
    val current = commentsFlow.value[photoId].orEmpty()
    commentsFlow.value = commentsFlow.value + (photoId to (current + comment))
    return Result.success(comment)
  }
}
