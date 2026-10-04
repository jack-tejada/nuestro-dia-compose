package com.madrigalsolu.nuestrodia.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.madrigalsolu.nuestrodia.compose.feature.auth.data.FirebaseAuthRepository
import com.madrigalsolu.nuestrodia.compose.feature.auth.ui.AuthViewModel
import com.madrigalsolu.nuestrodia.compose.feature.auth.ui.RegisterScreen
import com.madrigalsolu.nuestrodia.compose.feature.auth.ui.ResetPasswordScreen
import com.madrigalsolu.nuestrodia.compose.feature.auth.ui.SignInScreen
import com.madrigalsolu.nuestrodia.compose.feature.auth.ui.TermsScreen
import com.madrigalsolu.nuestrodia.compose.feature.capture.data.DefaultCaptureRepository
import com.madrigalsolu.nuestrodia.compose.feature.capture.ui.CapturePreviewScreen
import com.madrigalsolu.nuestrodia.compose.feature.capture.ui.CaptureViewModel
import com.madrigalsolu.nuestrodia.compose.feature.display.ui.TvDisplayScreen
import com.madrigalsolu.nuestrodia.compose.feature.gallery.data.DefaultGalleryRepository
import com.madrigalsolu.nuestrodia.compose.feature.gallery.ui.GalleryScreen
import com.madrigalsolu.nuestrodia.compose.feature.gallery.ui.GalleryViewModel
import com.madrigalsolu.nuestrodia.compose.feature.profile.ui.ProfileScreen

@Composable
fun MainNavigation() {
  val context = LocalContext.current
  val authRepository = remember(context) { FirebaseAuthRepository(context) }
  val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
  val authUiState by authViewModel.uiState.collectAsState()

  val captureRepository = remember(context) { DefaultCaptureRepository(context) }
  val captureViewModel: CaptureViewModel = viewModel { CaptureViewModel(captureRepository) }

  val galleryRepository = remember(context) { DefaultGalleryRepository(context) }

  val initialNavKey = remember {
    if (authRepository.currentUser() != null) GalleryKey() else SignInKey
  }
  val backStack = rememberNavBackStack(initialNavKey)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<SignInKey> {
          SignInScreen(
            viewModel = authViewModel,
            onSignedIn = {
              backStack.clear()
              backStack.add(GalleryKey())
            },
            onNavigateToRegister = { backStack.add(RegisterKey) },
            onNavigateToResetPassword = { backStack.add(ResetPasswordKey) },
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
          )
        }
        entry<RegisterKey> {
          RegisterScreen(
            viewModel = authViewModel,
            onRegistered = {
              backStack.clear()
              backStack.add(GalleryKey())
            },
            onNavigateToSignIn = { backStack.removeLastOrNull() },
            onNavigateToTerms = { backStack.add(TermsKey) },
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
          )
        }
        entry<ResetPasswordKey> {
          ResetPasswordScreen(
            viewModel = authViewModel,
            onNavigateToSignIn = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
          )
        }
        entry<TermsKey> {
          TermsScreen(
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
          )
        }
        entry<GalleryKey> { key ->
          val galleryViewModel: GalleryViewModel = viewModel(key = key.eventId) {
            GalleryViewModel(repository = galleryRepository, eventId = key.eventId)
          }
          GalleryScreen(
            viewModel = galleryViewModel,
            currentUserId = authUiState.user?.id ?: "guest-user",
            currentUserName = authUiState.user?.displayName ?: "Invitado",
            onNavigateToCapturePreview = { uriString ->
              backStack.add(CapturePreviewKey(uriString = uriString, eventId = key.eventId))
            },
            onNavigateToTvDisplay = {
              backStack.add(TvDisplayKey(eventId = key.eventId))
            },
            onNavigateToProfile = {
              backStack.add(ProfileKey)
            },
            onSignOut = {
              authViewModel.signOut()
              backStack.clear()
              backStack.add(SignInKey)
            },
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
          )
        }
        entry<CapturePreviewKey> { key ->
          CapturePreviewScreen(
            uriString = key.uriString,
            eventId = key.eventId,
            currentUserId = authUiState.user?.id ?: "guest-user",
            currentUserName = authUiState.user?.displayName ?: "Invitado",
            viewModel = captureViewModel,
            onPhotoUploaded = {
              captureViewModel.reset()
              backStack.removeLastOrNull()
            },
            onCancel = {
              captureViewModel.reset()
              backStack.removeLastOrNull()
            },
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
          )
        }
        entry<TvDisplayKey> { key ->
          TvDisplayScreen(
            eventId = key.eventId,
            repository = galleryRepository,
            onExit = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize(),
          )
        }
        entry<ProfileKey> {
          ProfileScreen(
            userId = authUiState.user?.id ?: "guest-user",
            userName = authUiState.user?.displayName ?: "Invitado",
            userEmail = "",
            eventId = "boda-principal",
            galleryRepository = galleryRepository,
            onNavigateToTerms = { backStack.add(TermsKey) },
            onSignOut = {
              authViewModel.signOut()
              backStack.clear()
              backStack.add(SignInKey)
            },
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
          )
        }
      },
  )
}
