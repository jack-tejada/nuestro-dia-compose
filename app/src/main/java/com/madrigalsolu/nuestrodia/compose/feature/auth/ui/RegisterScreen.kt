package com.madrigalsolu.nuestrodia.compose.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.madrigalsolu.nuestrodia.compose.theme.ExpressiveIntSizeSpring

@Composable
fun RegisterScreen(
  viewModel: AuthViewModel,
  onRegistered: () -> Unit,
  onNavigateToSignIn: () -> Unit,
  onNavigateToTerms: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsState()
  var fullName by rememberSaveable { mutableStateOf("") }
  var email by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var confirmPassword by rememberSaveable { mutableStateOf("") }
  var acceptedTerms by rememberSaveable { mutableStateOf(false) }
  var hasSubmitted by rememberSaveable { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current

  // m3e: Smooth entrance spring animation
  var contentVisible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    contentVisible = true
  }

  LaunchedEffect(uiState.user) {
    if (uiState.user != null) {
      onRegistered()
    }
  }

  // Inline validations
  val nameError = if (hasSubmitted && fullName.trim().length < 2) "Ingresá tu nombre completo." else null
  val emailError = if (hasSubmitted && validateEmail(email) != null) "Ingresá un correo electrónico válido." else null
  val passwordError = if (hasSubmitted) {
    when {
      password.length < 8 -> "Usá al menos 8 caracteres."
      password.none(Char::isDigit) -> "Sumá al menos un número."
      password.none { !it.isLetterOrDigit() } -> "Sumá al menos un carácter especial."
      else -> null
    }
  } else null
  val confirmPasswordError = if (hasSubmitted && password != confirmPassword) "Las contraseñas no coinciden." else null
  val termsError = if (hasSubmitted && !acceptedTerms) "Para crear tu cuenta, necesitás aceptar las condiciones de uso." else null

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 32.dp)
      .imePadding(),
    horizontalAlignment = Alignment.CenterHorizontally,
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
          title = "Crear cuenta",
          subtitle = "Registrate para ser parte del álbum compartido",
          badgeText = "NUEVO INVITADO",
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthFeedbackBanner(
          errorMessage = uiState.errorMessage,
          noticeMessage = uiState.notice,
        )

        if (uiState.errorMessage != null || uiState.notice != null) {
          Spacer(modifier = Modifier.height(16.dp))
        }

        AuthTextField(
          value = fullName,
          onValueChange = {
            fullName = it
            viewModel.clearFeedback()
          },
          label = "Nombre completo",
          errorMessage = nameError,
          enabled = !uiState.isLoading,
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next,
          ),
          keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
          ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
          value = email,
          onValueChange = {
            email = it
            viewModel.clearFeedback()
          },
          label = "Correo electrónico",
          errorMessage = emailError,
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
          errorMessage = passwordError,
          enabled = !uiState.isLoading,
          imeAction = ImeAction.Next,
          onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthPasswordField(
          value = confirmPassword,
          onValueChange = {
            confirmPassword = it
            viewModel.clearFeedback()
          },
          label = "Confirmar contraseña",
          errorMessage = confirmPasswordError,
          enabled = !uiState.isLoading,
          imeAction = ImeAction.Done,
          onImeAction = { focusManager.clearFocus() },
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !uiState.isLoading) {
              acceptedTerms = !acceptedTerms
              viewModel.clearFeedback()
            },
        ) {
          Checkbox(
            checked = acceptedTerms,
            onCheckedChange = {
              acceptedTerms = it
              viewModel.clearFeedback()
            },
            enabled = !uiState.isLoading,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Acepto las condiciones de uso y privacidad",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(
              onClick = onNavigateToTerms,
              contentPadding = PaddingValues(0.dp),
            ) {
              Text(
                text = "Ver condiciones",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }

        if (termsError != null) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = termsError,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuthPrimaryButton(
          text = "Registrarme",
          onClick = {
            hasSubmitted = true
            focusManager.clearFocus()
            viewModel.register(fullName, email, password, confirmPassword, acceptedTerms)
          },
          isLoading = uiState.isLoading,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "¿Ya tenés una cuenta?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          TextButton(
            onClick = {
              viewModel.clearFeedback()
              onNavigateToSignIn()
            },
            enabled = !uiState.isLoading,
          ) {
            Text(
              text = "Iniciá sesión",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
    }
  }
}
