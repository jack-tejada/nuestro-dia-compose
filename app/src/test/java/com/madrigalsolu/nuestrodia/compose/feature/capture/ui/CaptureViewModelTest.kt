package com.madrigalsolu.nuestrodia.compose.feature.capture.ui

import android.net.Uri
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.CaptureRepository
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Photo
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.UploadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CaptureViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @Before fun setMainDispatcher() = Dispatchers.setMain(dispatcher)
  @After fun resetMainDispatcher() = Dispatchers.resetMain()

  @Test fun initialUploadStateIsIdle() = runTest(dispatcher) {
    val fakeRepo = FakeCaptureRepository()
    val viewModel = CaptureViewModel(fakeRepo)
    assertEquals(UploadState.Idle, viewModel.uploadState.value)
  }

  @Test fun successfulUploadEmitsSuccessState() = runTest(dispatcher) {
    val expectedPhoto = Photo(
      id = "p1",
      eventId = "boda-1",
      ownerUid = "u1",
      ownerName = "Ana",
      uriString = "content://photo/1",
      storagePath = "path/1",
      timestamp = 1000L,
      width = 1200,
      height = 800,
      aspectRatio = 1.5f,
    )
    val fakeRepo = FakeCaptureRepository(result = Result.success(expectedPhoto))
    val viewModel = CaptureViewModel(fakeRepo)

    val uri = runCatching { Uri.parse("content://photo/1") }.getOrDefault(Uri.EMPTY)
    viewModel.uploadPhoto("boda-1", uri, "u1", "Ana", 1200, 800)
    advanceUntilIdle()

    assertEquals(UploadState.Success(expectedPhoto), viewModel.uploadState.value)
  }

  @Test fun failedUploadEmitsErrorStateWithRetryOption() = runTest(dispatcher) {
    val fakeRepo = FakeCaptureRepository(result = Result.failure(IllegalStateException("Error de red")))
    val viewModel = CaptureViewModel(fakeRepo)

    val uri = runCatching { Uri.parse("content://photo/1") }.getOrDefault(Uri.EMPTY)
    viewModel.uploadPhoto("boda-1", uri, "u1", "Ana", 1200, 800)
    advanceUntilIdle()

    val state = viewModel.uploadState.value
    assertTrue(state is UploadState.Error)
    val error = state as UploadState.Error
    assertEquals(true, error.retryable)
    assertEquals("Error de red", error.message)
  }

  @Test fun resetRestoresIdleState() = runTest(dispatcher) {
    val fakeRepo = FakeCaptureRepository()
    val viewModel = CaptureViewModel(fakeRepo)

    val uri = runCatching { Uri.parse("content://photo/1") }.getOrDefault(Uri.EMPTY)
    viewModel.uploadPhoto("boda-1", uri, "u1", "Ana", 1200, 800)
    advanceUntilIdle()

    viewModel.reset()
    assertEquals(UploadState.Idle, viewModel.uploadState.value)
  }
}

private class FakeCaptureRepository(
  private val result: Result<Photo> = Result.success(
    Photo(
      id = "p1",
      eventId = "boda-1",
      ownerUid = "u1",
      ownerName = "Ana",
      uriString = "content://photo/1",
      storagePath = "path/1",
      timestamp = 1000L,
      width = 1200,
      height = 800,
      aspectRatio = 1.5f,
    )
  ),
) : CaptureRepository {
  override suspend fun uploadPhoto(
    eventId: String,
    imageUri: Uri,
    width: Int,
    height: Int,
    ownerUid: String,
    ownerName: String,
    onProgress: (Float) -> Unit,
  ): Photo {
    onProgress(1.0f)
    return result.getOrThrow()
  }

  override suspend fun deletePhoto(photo: Photo, requesterUid: String): Boolean = true
}
