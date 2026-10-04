package com.madrigalsolu.nuestrodia.compose.feature.display.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Photo
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.Event
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.GalleryRepository
import com.madrigalsolu.nuestrodia.compose.theme.rememberExpressiveAmbientScale
import com.madrigalsolu.nuestrodia.compose.ui.util.PhotoImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Landscape, read-only presentation screen for TV and projection.
 * - Zero upload, delete, or account controls.
 * - Living ambient zoom (Ken Burns / Google Photos screensaver feel).
 * - Network-loss resilient: continues cycling already loaded photos.
 */
@Composable
fun TvDisplayScreen(
  eventId: String,
  repository: GalleryRepository,
  onExit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var cachedPhotos by rememberSaveable { mutableStateOf<List<Photo>>(emptyList()) }
  var event by remember { mutableStateOf<Event?>(null) }
  var currentIndex by rememberSaveable { mutableIntStateOf(0) }

  // m3e: Living ambient breathing zoom mimicking Pixel ambient screensaver
  val ambientScale by rememberExpressiveAmbientScale(minScale = 1.0f, maxScale = 1.04f, durationMillis = 10000)

  val photosFlow = remember(eventId) {
    repository.getPhotos(eventId).catch {
      // Network loss resilience: keep existing cachedPhotos without crashing
    }
  }
  val livePhotos by photosFlow.collectAsState(initial = emptyList())

  LaunchedEffect(livePhotos) {
    if (livePhotos.isNotEmpty()) {
      cachedPhotos = livePhotos
    }
  }

  LaunchedEffect(eventId) {
    runCatching { repository.getEvent(eventId) }
      .onSuccess { event = it }
  }

  // Slideshow transition: cycle every 8 seconds
  LaunchedEffect(cachedPhotos.size) {
    if (cachedPhotos.isNotEmpty()) {
      while (true) {
        delay(8000)
        currentIndex = (currentIndex + 1) % cachedPhotos.size
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black),
  ) {
    if (cachedPhotos.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(32.dp),
        ) {
          Text(
            text = event?.name ?: "Nuestro Día",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Esperando fotografías del evento. Subí recuerdos desde tu teléfono para verlos proyectados aquí.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
          )
        }
      }
    } else {
      val safeIndex = currentIndex.coerceIn(0, cachedPhotos.size - 1)
      val currentPhoto = cachedPhotos[safeIndex]

      Crossfade(
        targetState = currentPhoto,
        animationSpec = tween(durationMillis = 800),
        label = "TvSlideshowCrossfade",
      ) { photo ->
        TvPhotoImage(photo = photo, scale = ambientScale)
      }

      // m3e: Expressive pill badge overlay with photographer credits and timestamp
      Surface(
        color = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(24.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
          Column {
            Text(
              text = currentPhoto.ownerName,
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
            )
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
              .format(Date(currentPhoto.timestamp))
            Text(
              text = formattedDate,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.8f),
            )
          }
          if (event != null) {
            Spacer(modifier = Modifier.width(16.dp))
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
              shape = RoundedCornerShape(50),
            ) {
              Text(
                text = event!!.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }
      }
    }

    // Discreet exit button at top-end corner
    OutlinedButton(
      onClick = onExit,
      shape = RoundedCornerShape(50),
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(16.dp),
    ) {
      Text("Salir de pantalla", color = Color.White)
    }
  }
}

@Composable
private fun TvPhotoImage(photo: Photo, scale: Float) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      },
    contentAlignment = Alignment.Center,
  ) {
    PhotoImage(
      uri = photo.uriString,
      contentDescription = "Fotograf\u00eda de ${photo.ownerName}",
      contentScale = ContentScale.Fit,
      modifier = Modifier.fillMaxSize(),
    )
  }
}
