package com.madrigalsolu.nuestrodia.compose.feature.capture.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.madrigalsolu.nuestrodia.compose.feature.auth.ui.AuthFeedbackBanner
import com.madrigalsolu.nuestrodia.compose.feature.auth.ui.AuthHeader
import com.madrigalsolu.nuestrodia.compose.feature.auth.ui.AuthPrimaryButton
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.UploadState
import com.madrigalsolu.nuestrodia.compose.theme.ExpressiveIntSizeSpring
import com.madrigalsolu.nuestrodia.compose.theme.ExpressiveSpringSmooth
import com.madrigalsolu.nuestrodia.compose.theme.expressivePress

import com.madrigalsolu.nuestrodia.compose.ui.util.PhotoImage

@Composable
fun CapturePreviewScreen(
  uriString: String,
  eventId: String,
  currentUserId: String,
  currentUserName: String,
  viewModel: CaptureViewModel,
  onPhotoUploaded: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val uploadState by viewModel.uploadState.collectAsState()

  var imageWidth by remember(uriString) { mutableIntStateOf(1200) }
  var imageHeight by remember(uriString) { mutableIntStateOf(800) }
  val imageAspectRatio = imageWidth.toFloat() / imageHeight

  LaunchedEffect(uploadState) {
    if (uploadState is UploadState.Success) {
      onPhotoUploaded()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(24.dp)
      .animateContentSize(animationSpec = ExpressiveIntSizeSpring), // m3e: fluid spring layout updates
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth(),
    ) {
      AuthHeader(
        title = "Confirmar fotografía",
        subtitle = "Revisá la imagen antes de compartirla en el álbum colectivo",
        badgeText = "NUEVA FOTO",
      )

      Spacer(modifier = Modifier.height(24.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(imageAspectRatio.coerceIn(0.5f, 2.0f))
          .clip(MaterialTheme.shapes.extraLarge)
          .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
      ) {
        PhotoImage(
          uri = uriString,
          contentDescription = "Vista previa de la foto seleccionada",
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize(),
          onImageLoaded = { width, height ->
            if (width > 0 && height > 0) {
              imageWidth = width
              imageHeight = height
            }
          },
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      when (val state = uploadState) {
        is UploadState.Uploading -> {
          // m3e: Smoothly animated progress value
          val animatedProgress by animateFloatAsState(
            targetValue = state.progress,
            animationSpec = ExpressiveSpringSmooth,
            label = "UploadProgressBar",
          )
          val progressPercent = (animatedProgress * 100).toInt()

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .fillMaxWidth()
              .semantics { contentDescription = "Subiendo foto: $progressPercent por ciento" },
          ) {
            LinearProgressIndicator(
              progress = { animatedProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Subiendo foto... $progressPercent%",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
        is UploadState.Error -> {
          AuthFeedbackBanner(
            errorMessage = state.message,
            modifier = Modifier.padding(bottom = 12.dp),
          )
        }
        else -> Unit
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      when (val state = uploadState) {
        is UploadState.Error -> {
          AuthPrimaryButton(
            text = "Reintentar subida",
            onClick = {
              viewModel.retryUpload(
                eventId = eventId,
                ownerUid = currentUserId,
                ownerName = currentUserName,
              )
            },
          )
          val cancelInteraction = remember { MutableInteractionSource() }
          OutlinedButton(
            onClick = onCancel,
            interactionSource = cancelInteraction,
            shape = RoundedCornerShape(50),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .expressivePress(pressedScale = 0.95f, interactionSource = cancelInteraction),
          ) {
            Text("Cancelar", style = MaterialTheme.typography.labelLarge)
          }
        }
        is UploadState.Uploading -> {
          // Actions disabled during upload
        }
        else -> {
          AuthPrimaryButton(
            text = "Subir foto",
            onClick = {
              viewModel.uploadPhoto(
                eventId = eventId,
                imageUri = Uri.parse(uriString),
                ownerUid = currentUserId,
                ownerName = currentUserName,
                width = imageWidth,
                height = imageHeight,
              )
            },
          )

          val cancelInteraction = remember { MutableInteractionSource() }
          OutlinedButton(
            onClick = onCancel,
            interactionSource = cancelInteraction,
            shape = RoundedCornerShape(50),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .expressivePress(pressedScale = 0.95f, interactionSource = cancelInteraction), // m3e: tactile spring press
          ) {
            Text("Elegir otra", style = MaterialTheme.typography.labelLarge)
          }
        }
      }
    }
  }
}
