package com.madrigalsolu.nuestrodia.compose.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.GalleryRepository
import com.madrigalsolu.nuestrodia.compose.theme.expressivePress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  userId: String,
  userName: String,
  userEmail: String,
  eventId: String,
  galleryRepository: GalleryRepository,
  onNavigateToTerms: () -> Unit,
  onSignOut: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showSignOutConfirmation by rememberSaveable { mutableStateOf(false) }

  val photos by galleryRepository.getPhotos(eventId).collectAsState(initial = emptyList())
  val myPhotos = remember(photos, userId) { photos.filter { it.ownerUid == userId } }
  val totalLikesReceived = remember(myPhotos) { myPhotos.sumOf { it.likesCount } }

  val initials = remember(userName) {
    userName.split(" ")
      .filter { it.isNotBlank() }
      .take(2)
      .mapNotNull { it.firstOrNull()?.uppercaseChar() }
      .joinToString("")
      .ifBlank { "IN" }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Mi perfil",
            style = MaterialTheme.typography.titleLarge,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Volver",
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
        ),
      )
    },
    containerColor = MaterialTheme.colorScheme.background,
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // m3e: Organic avatar container with user initials
      Box(
        modifier = Modifier
          .size(96.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = initials,
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = userName.ifBlank { "Invitado del evento" },
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
      )

      if (userEmail.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = userEmail,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Statistics Section
      Text(
        text = "Tu actividad en el evento",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        StatCard(
          title = "Fotos subidas",
          value = "${myPhotos.size}",
          modifier = Modifier.weight(1f),
        )

        StatCard(
          title = "Me gusta recibidos",
          value = "$totalLikesReceived",
          modifier = Modifier.weight(1f),
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Actions section
      val termsInteraction = remember { MutableInteractionSource() }
      OutlinedButton(
        onClick = onNavigateToTerms,
        interactionSource = termsInteraction,
        shape = RoundedCornerShape(50),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .expressivePress(pressedScale = 0.95f, interactionSource = termsInteraction),
      ) {
        Text(
          text = "Condiciones y privacidad",
          style = MaterialTheme.typography.labelLarge,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      val signOutInteraction = remember { MutableInteractionSource() }
      Button(
        onClick = { showSignOutConfirmation = true },
        interactionSource = signOutInteraction,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.errorContainer,
          contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .expressivePress(pressedScale = 0.95f, interactionSource = signOutInteraction),
      ) {
        Text(
          text = "Cerrar sesión",
          style = MaterialTheme.typography.labelLarge,
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (showSignOutConfirmation) {
    AlertDialog(
      onDismissRequest = { showSignOutConfirmation = false },
      title = { Text("Cerrar sesión") },
      text = { Text("¿Estás seguro de que querés salir de tu cuenta?") },
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

@Composable
private fun StatCard(
  title: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
  }
}
