package com.madrigalsolu.nuestrodia.compose.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest

@Composable
fun PhotoImage(
  uri: String,
  contentDescription: String,
  contentScale: ContentScale,
  modifier: Modifier = Modifier,
  onImageLoaded: ((width: Int, height: Int) -> Unit)? = null,
) {
  var state by remember(uri) {
    mutableStateOf(if (uri.isBlank()) LoadState.Error else LoadState.Loading)
  }
  val context = LocalContext.current
  val request = remember(uri, context) {
    ImageRequest.Builder(context)
      .data(uri)
      // Event photos may be private; keep image reuse in memory only until the process is gone.
      .diskCachePolicy(CachePolicy.DISABLED)
      .memoryCachePolicy(CachePolicy.ENABLED)
      .build()
  }

  Box(
    modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
    contentAlignment = Alignment.Center,
  ) {
    if (uri.isNotBlank()) {
      AsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = Modifier.fillMaxSize(),
        onLoading = { state = LoadState.Loading },
        onSuccess = { result ->
          state = LoadState.Success
          onImageLoaded?.invoke(result.result.image.width, result.result.image.height)
        },
        onError = { state = LoadState.Error },
      )
    }

    when (state) {
      LoadState.Loading -> Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CircularProgressIndicator()
        Text(
          "Cargando fotografía…",
          style = MaterialTheme.typography.bodySmall,
          textAlign = TextAlign.Center,
        )
      }
      LoadState.Error -> Text(
        text = "No se pudo cargar esta fotografía",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
      LoadState.Success -> Unit
    }
  }
}

private enum class LoadState { Loading, Success, Error }
