package com.madrigalsolu.nuestrodia.compose.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.madrigalsolu.nuestrodia.compose.theme.ExpressiveIntSizeSpring

@Composable
fun SignInScreen(
  viewModel: AuthViewModel,
  onSignedIn: () -> Unit,
  onNavigateToRegister: () -> Unit,
  onNavigateToResetPassword: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsState()
  var email by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  val focusManager = LocalFocusManager.current

  // m3e: Smooth entrance spring animation
  var contentVisible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    contentVisible = true
  }

  LaunchedEffect(uiState.user) {
    if (uiState.user != null) {
      onSignedIn()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 32.dp)
      .imePadding(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    AnimatedVisibility(
      visible = contentVisible,
      enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = ExpressiveIntSizeSpring),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .animateContentSize(animationSpec = ExpressiveIntSizeSpring), // m3e: fluid spring expansion
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        AuthHeader(
          badgeText = "ACCESO AL EVENTO",
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthFeedbackBanner(
          errorMessage = uiState.errorMessage,
          noticeMessage = uiState.notice,
        )

        if (uiState.errorMessage != null || uiState.notice != null) {
          Spacer(modifier = Modifier.height(16.dp))
        }

        AuthTextField(
          value = email,
          onValueChange = {
            email = it
            viewModel.clearFeedback()
          },
          label = "Correo electrónico",
          enabled = !uiState.isLoading,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
          ),
          keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
          ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthPasswordField(
          value = password,
          onValueChange = {
            password = it
            viewModel.clearFeedback()
          },
          label = "Contraseña",
          enabled = !uiState.isLoading,
          imeAction = ImeAction.Done,
          onImeAction = {
            focusManager.clearFocus()
            viewModel.signIn(email, password)
          },
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthPrimaryButton(
          text = "Iniciar sesión",
          onClick = {
            focusManager.clearFocus()
            viewModel.signIn(email, password)
          },
          isLoading = uiState.isLoading,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
          onClick = {
            viewModel.clearFeedback()
            onNavigateToResetPassword()
          },
          enabled = !uiState.isLoading,
        ) {
          Text(
            text = "¿Olvidaste tu contraseña?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "¿No tenés cuenta?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          TextButton(
            onClick = {
              viewModel.clearFeedback()
              onNavigateToRegister()
            },
            enabled = !uiState.isLoading,
          ) {
            Text(
              text = "Registrate acá",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
    }
  }
}
