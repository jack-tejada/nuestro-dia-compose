package com.madrigalsolu.nuestrodia.compose.feature.gallery.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Comment
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.Photo
import com.madrigalsolu.nuestrodia.compose.theme.ExpressiveSpringBouncy
import com.madrigalsolu.nuestrodia.compose.theme.expressivePress
import com.madrigalsolu.nuestrodia.compose.ui.util.PhotoImage
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhotoDetailDialog(
  photo: Photo,
  currentUserId: String,
  commentsFlow: Flow<List<Comment>>,
  onDismiss: () -> Unit,
  onDeletePhoto: (Photo) -> Unit,
  onToggleLike: (Photo) -> Unit,
  onAddComment: (String) -> Unit,
) {
  var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
  var commentInput by rememberSaveable { mutableStateOf("") }
  val comments by commentsFlow.collectAsState(initial = emptyList())

  val isLiked = photo.likedByUids.contains(currentUserId)

  // m3e: Spring entrance zoom animation
  var isEntered by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    isEntered = true
  }
  val dialogScale by animateFloatAsState(
    targetValue = if (isEntered) 1f else 0.92f,
    animationSpec = ExpressiveSpringBouncy,
    label = "DetailDialogZoom",
  )

  val formattedDate = remember(photo.timestamp) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    sdf.format(Date(photo.timestamp))
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 24.dp)
        .graphicsLayer {
          scaleX = dialogScale
          scaleY = dialogScale
        },
      shape = RoundedCornerShape(32.dp), // m3e: expressive 32.dp rounded panel
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
      ),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(photo.aspectRatio.coerceIn(0.5f, 2.0f))
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
          contentAlignment = Alignment.Center,
        ) {
          PhotoImage(
            uri = photo.uriString,
            contentDescription = "Fotografía compartida por ${photo.ownerName}",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Compartida por: ${photo.ownerName}",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = formattedDate,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          // m3e: Tactile Like toggle button
          val likeInteraction = remember { MutableInteractionSource() }
          Surface(
            color = if (isLiked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(50),
            modifier = Modifier
              .expressivePress(pressedScale = 0.90f, interactionSource = likeInteraction)
              .clickable(
                interactionSource = likeInteraction,
                indication = null,
                onClick = { onToggleLike(photo) },
              ),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
              Text(
                text = if (isLiked) {
                  if (photo.likesCount > 1) "Te gusta (+${photo.likesCount - 1})" else "Te gusta"
                } else {
                  if (photo.likesCount > 0) "Me gusta (${photo.likesCount})" else "Me gusta"
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (isLiked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        // Comments / Dedications section
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Dedicatorias y mensajes",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
          )

          Spacer(modifier = Modifier.height(8.dp))

          if (comments.isEmpty()) {
            Text(
              text = "Aún no hay dedicatorias. ¡Dejale unas palabras a los agasajados!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 8.dp),
            )
          } else {
            Column(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .verticalScroll(rememberScrollState()),
            ) {
              comments.forEach { comment ->
                Surface(
                  color = MaterialTheme.colorScheme.surfaceContainerLow,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                      )
                      val commentDate = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(comment.timestamp))
                      Text(
                        text = commentDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = comment.text,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Comment input field and send button
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = commentInput,
              onValueChange = { commentInput = it },
              placeholder = { Text("Escribí un mensaje...") },
              singleLine = true,
              shape = RoundedCornerShape(24.dp),
              modifier = Modifier.weight(1f),
            )

            val sendInteraction = remember { MutableInteractionSource() }
            Button(
              onClick = {
                if (commentInput.isNotBlank()) {
                  onAddComment(commentInput.trim())
                  commentInput = ""
                }
              },
              enabled = commentInput.isNotBlank(),
              interactionSource = sendInteraction,
              shape = RoundedCornerShape(50),
              modifier = Modifier.expressivePress(pressedScale = 0.92f, interactionSource = sendInteraction),
            ) {
              Text("Enviar")
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (photo.ownerUid == currentUserId) {
            val deleteInteraction = remember { MutableInteractionSource() }
            TextButton(
              onClick = { showDeleteConfirmation = true },
              interactionSource = deleteInteraction,
              modifier = Modifier.expressivePress(pressedScale = 0.94f, interactionSource = deleteInteraction),
              colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
              ),
            ) {
              Text("Eliminar foto")
            }
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }

          val closeInteraction = remember { MutableInteractionSource() }
          OutlinedButton(
            onClick = onDismiss,
            interactionSource = closeInteraction,
            shape = RoundedCornerShape(50),
            modifier = Modifier.expressivePress(pressedScale = 0.94f, interactionSource = closeInteraction),
          ) {
            Text("Cerrar")
          }
        }
      }
    }
  }

  if (showDeleteConfirmation) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmation = false },
      title = { Text("¿Eliminar fotografía?") },
      text = {
        Text("Esta acción eliminará la fotografía del álbum compartido de forma permanente. Solo vos como autor podés realizar esta acción.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteConfirmation = false
            onDeletePhoto(photo)
            onDismiss()
          },
          colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
          ),
        ) {
          Text("Eliminar")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirmation = false }) {
          Text("Cancelar")
        }
      },
    )
  }
}
