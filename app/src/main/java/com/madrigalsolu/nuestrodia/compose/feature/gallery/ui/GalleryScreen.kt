package com.madrigalsolu.nuestrodia.compose.feature.gallery.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Photo
import com.madrigalsolu.nuestrodia.compose.feature.permissions.rememberCameraPermissionController
import com.madrigalsolu.nuestrodia.compose.theme.ExpressiveIntSizeSpring
import com.madrigalsolu.nuestrodia.compose.theme.expressivePress
import com.madrigalsolu.nuestrodia.compose.ui.util.PhotoImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
  viewModel: GalleryViewModel,
  currentUserId: String,
  currentUserName: String = "Invitado",
  onNavigateToCapturePreview: (uriString: String) -> Unit,
  onNavigateToTvDisplay: () -> Unit,
  onNavigateToProfile: () -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val event by viewModel.event.collectAsState()
  val selectedPhoto by viewModel.selectedPhoto.collectAsState()

  var showQrDialog by rememberSaveable { mutableStateOf(false) }
  var showSignOutConfirmation by rememberSaveable { mutableStateOf(false) }
  var pendingCameraPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }

  // Photo Picker: Android standard, requires no runtime permission
  val pickMediaLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { uri ->
    if (uri != null) {
      onNavigateToCapturePreview(uri.toString())
    }
  }

  // Camera take picture launcher
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
  ) { success ->
    if (success && pendingCameraPhotoUri != null) {
      onNavigateToCapturePreview(pendingCameraPhotoUri!!)
    }
  }

  fun launchCamera() {
    val photoFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, photoFile)
    pendingCameraPhotoUri = uri.toString()
    takePictureLauncher.launch(uri)
  }

  // Camera permission manager with denial and settings guidance
  val cameraPermissionController = rememberCameraPermissionController(
    onPermissionGranted = { launchCamera() },
    onFallbackToPicker = {
      pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    },
  )

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        // Detect touches to reset inactivity timer and restore newest-first order
        detectTapGestures(
          onPress = {
            viewModel.onUserInteraction()
          },
        )
      },
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = event?.name ?: "Nuestro Día",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.primary,
            )
            if (event != null) {
              // m3e: Organic tonal pill chip displaying event details in top bar
              Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(top = 2.dp),
              ) {
                Text(
                  text = "${event!!.code} • ${event!!.date}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
          }
        },
        actions = {
          TextButton(onClick = onNavigateToProfile) {
            Text("Perfil")
          }
          TextButton(onClick = { showQrDialog = true }) {
            Text("Código QR")
          }
          TextButton(onClick = onNavigateToTvDisplay) {
            Text("Modo TV")
          }
          TextButton(onClick = { showSignOutConfirmation = true }) {
            Text("Salir")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
        ),
      )
    },
    floatingActionButton = {
      val cameraInteraction = remember { MutableInteractionSource() }
      val pickerInteraction = remember { MutableInteractionSource() }

      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 8.dp),
      ) {
        // m3e: Expressive pill shape and tactile spring compression on FABs
        ExtendedFloatingActionButton(
          onClick = {
            viewModel.onUserInteraction()
            cameraPermissionController.requestCameraPermission()
          },
          interactionSource = cameraInteraction,
          shape = RoundedCornerShape(50),
          modifier = Modifier.expressivePress(pressedScale = 0.93f, interactionSource = cameraInteraction),
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
          Text("Tomar foto")
        }

        ExtendedFloatingActionButton(
          onClick = {
            viewModel.onUserInteraction()
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          },
          interactionSource = pickerInteraction,
          shape = RoundedCornerShape(50),
          modifier = Modifier.expressivePress(pressedScale = 0.93f, interactionSource = pickerInteraction),
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
          contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
          Text("Elegir foto")
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
    ) {
      when (val state = uiState) {
        is GalleryUiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
          }
        }
        is GalleryUiState.Empty -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center,
          ) {
            Card(
              shape = MaterialTheme.shapes.extraLarge,
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
              ),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  text = "Álbum en blanco",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = "Aún no hay fotos en este evento. Sé el primero en compartir un recuerdo.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                )
              }
            }
          }
        }
        is GalleryUiState.Error -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = state.message,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.error,
              textAlign = TextAlign.Center,
            )
          }
        }
        is GalleryUiState.Success -> {
          GalleryContent(
            photos = state.photos,
            isShuffled = state.isShuffled,
            onPhotoClick = { photo -> viewModel.selectPhoto(photo) },
          )
        }
      }
    }
  }

  selectedPhoto?.let { photo ->
    PhotoDetailDialog(
      photo = photo,
      currentUserId = currentUserId,
      commentsFlow = viewModel.getComments(photo.id),
      onDismiss = { viewModel.selectPhoto(null) },
      onDeletePhoto = { photoToDelete ->
        viewModel.deletePhoto(photoToDelete, currentUserId)
      },
      onToggleLike = { photoToLike ->
        viewModel.toggleLike(photoToLike, currentUserId)
      },
      onAddComment = { text ->
        viewModel.addComment(photo.id, currentUserId, currentUserName, text)
      },
    )
  }

  if (showQrDialog && event != null) {
    QrEventDialog(
      event = event!!,
      onDismiss = { showQrDialog = false },
    )
  }

  if (showSignOutConfirmation) {
    AlertDialog(
      onDismissRequest = { showSignOutConfirmation = false },
      title = { Text("Cerrar sesión") },
      text = { Text("¿Deseás salir de tu cuenta?") },
      confirmButton = {
        Button(
          onClick = {
            showSignOutConfirmation = false
            onSignOut()
          },
        ) {
          Text("Cerrar sesión")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSignOutConfirmation = false }) {
          Text("Cancelar")
        }
      },
    )
  }
}

/**
 * ponytail: Custom lightweight justified row layout preserving image aspect ratio without cropping.
 */
@Composable
private fun GalleryContent(
  photos: List<Photo>,
  isShuffled: Boolean,
  onPhotoClick: (Photo) -> Unit,
  modifier: Modifier = Modifier,
) {
  // Group photos into rows of 2 (or 1 if remaining)
  val photoRows = remember(photos) { photos.chunked(2) }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // m3e: Animated entrance and exit for shuffled notice banner with spring expansion
    item {
      AnimatedVisibility(
        visible = isShuffled,
        enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = ExpressiveIntSizeSpring),
        exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = ExpressiveIntSizeSpring),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
          Text(
            text = "Mostrando recuerdos aleatorios (tocá la pantalla para volver al orden reciente)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
          )
        }
      }
    }

    items(photoRows) { rowPhotos ->
      if (rowPhotos.size == 2) {
        val p1 = rowPhotos[0]
        val p2 = rowPhotos[1]
        val ratio1 = p1.aspectRatio.coerceIn(0.5f, 2.0f)
        val ratio2 = p2.aspectRatio.coerceIn(0.5f, 2.0f)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Box(
            modifier = Modifier
              .weight(ratio1)
              .aspectRatio(ratio1),
          ) {
            GalleryThumbnail(photo = p1, onClick = { onPhotoClick(p1) })
          }
          Box(
            modifier = Modifier
              .weight(ratio2)
              .aspectRatio(ratio2),
          ) {
            GalleryThumbnail(photo = p2, onClick = { onPhotoClick(p2) })
          }
        }
      } else {
        val single = rowPhotos[0]
        val ratio = single.aspectRatio.coerceIn(0.7f, 2.2f)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio),
        ) {
          GalleryThumbnail(photo = single, onClick = { onPhotoClick(single) })
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
private fun GalleryThumbnail(
  photo: Photo,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }

  Box(
    modifier = modifier
      .fillMaxSize()
      .clip(MaterialTheme.shapes.medium)
      .background(MaterialTheme.colorScheme.surfaceContainerHigh)
      .expressivePress(pressedScale = 0.94f, interactionSource = interactionSource) // m3e: tactile bounce on photo press
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
      ),
    contentAlignment = Alignment.BottomStart,
  ) {
    PhotoImage(
      uri = photo.uriString,
      contentDescription = "Fotografía de ${photo.ownerName}",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    Surface(
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
      shape = RoundedCornerShape(50), // m3e: organic pill badge
      modifier = Modifier.padding(6.dp),
    ) {
      Text(
        text = photo.ownerName,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
      )
    }

    if (photo.likesCount > 0) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        shape = RoundedCornerShape(50),
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(6.dp),
      ) {
        Text(
          text = "${photo.likesCount} me gusta",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
      }
    }
  }
}
