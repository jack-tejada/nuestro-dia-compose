package com.madrigalsolu.nuestrodia.compose.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Condiciones del evento",
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
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = "Privacidad y uso del álbum compartido",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
      )

      Text(
        text = "Te damos la bienvenida a Nuestro Día. Antes de participar, te pedimos que leas detenidamente las condiciones de uso de las fotografías y de tu cuenta.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )

      TermsSectionCard(
        title = "Uso de cámara y galería",
        body = "La aplicación accede a tu cámara y a la galería únicamente cuando decidís capturar o elegir una fotografía para subir. No se realiza ningún acceso en segundo plano ni sin tu confirmación explícita.",
      )

      TermsSectionCard(
        title = "Galería compartida en tiempo real",
        body = "Las imágenes que compartas se incorporan a la galería del evento en tiempo real y resultan visibles para todos los invitados y organizadores autorizados.",
      )

      TermsSectionCard(
        title = "Derechos sobre tus fotografías",
        body = "Mantenés el derecho a solicitar o eliminar en cualquier momento las fotos que vos hayas subido. Por respeto a los demás invitados, no es posible eliminar fotos compartidas por otras personas.",
      )

      TermsSectionCard(
        title = "Distinción entre cuenta y fotos",
        body = "Eliminar tu cuenta y eliminar tus fotos son procedimientos independientes. Si eliminás tu cuenta, las imágenes que subiste previamente seguirán visibles en el evento, a menos que las borres de forma individual antes de darte de baja.",
      )

      Spacer(modifier = Modifier.height(16.dp))

      AuthPrimaryButton(
        text = "Entendido, volver",
        onClick = onBack,
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun TermsSectionCard(
  title: String,
  body: String,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
