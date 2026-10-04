package com.madrigalsolu.nuestrodia.compose.feature.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper to open the application system settings screen.
 */
fun openAppSettings(context: Context) {
  val intent = Intent(
    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
    Uri.fromParts("package", context.packageName, null),
  ).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  context.startActivity(intent)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

/**
 * Controller for requesting camera permissions with explanation and settings fallback.
 */
class CameraPermissionController(
  val requestCameraPermission: () -> Unit,
)

/**
 * Composable camera permission handler.
 * - Never requests at launch; only invokes when user requests.
 * - Shows rationale dialog if denied once.
 * - Shows app settings guidance dialog if permanently denied.
 * - Offers fallback to photo picker in both dialogs.
 */
@Composable
fun rememberCameraPermissionController(
  onPermissionGranted: () -> Unit,
  onFallbackToPicker: () -> Unit,
): CameraPermissionController {
  val context = LocalContext.current
  val activity = remember(context) { context.findActivity() }

  var showRationaleDialog by rememberSaveable { mutableStateOf(false) }
  var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { isGranted ->
    if (isGranted) {
      showRationaleDialog = false
      showSettingsDialog = false
      onPermissionGranted()
    } else {
      val shouldShowRationale = activity != null &&
        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)

      if (shouldShowRationale) {
        showRationaleDialog = true
      } else {
        // Permanently denied or user selected "Don't ask again"
        showSettingsDialog = true
      }
    }
  }

  fun requestPermission() {
    val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
      onPermissionGranted()
    } else {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  if (showRationaleDialog) {
    CameraPermissionRationaleDialog(
      onConfirm = {
        showRationaleDialog = false
        permissionLauncher.launch(Manifest.permission.CAMERA)
      },
      onDismiss = {
        showRationaleDialog = false
        onFallbackToPicker()
      },
    )
  }

  if (showSettingsDialog) {
    CameraPermissionSettingsDialog(
      onOpenSettings = {
        showSettingsDialog = false
        openAppSettings(context)
      },
      onDismiss = {
        showSettingsDialog = false
        onFallbackToPicker()
      },
    )
  }

  return remember {
    CameraPermissionController(requestCameraPermission = ::requestPermission)
  }
}

@Composable
fun CameraPermissionRationaleDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Permiso de cámara",
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Text(
        text = "Para capturar fotos en el momento del festejo, necesitamos acceso a la cámara. Si preferís no otorgarlo, podés seleccionar fotos de tu dispositivo.",
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    confirmButton = {
      Button(onClick = onConfirm) {
        Text("Permitir cámara")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Elegir de galería")
      }
    },
  )
}

@Composable
fun CameraPermissionSettingsDialog(
  onOpenSettings: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Cámara deshabilitada",
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Text(
        text = "El acceso a la cámara está desactivado en la configuración de la aplicación. Podés activarlo en Ajustes para tomar fotos nuevas o continuar eligiendo fotos de tu galería.",
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    confirmButton = {
      Button(onClick = onOpenSettings) {
        Text("Abrir ajustes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Elegir de galería")
      }
    },
  )
}
