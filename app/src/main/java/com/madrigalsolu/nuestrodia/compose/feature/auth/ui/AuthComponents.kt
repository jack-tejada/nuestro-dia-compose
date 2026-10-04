package com.madrigalsolu.nuestrodia.compose.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.madrigalsolu.nuestrodia.compose.theme.ExpressiveIntSizeSpring
import com.madrigalsolu.nuestrodia.compose.theme.expressivePress

/**
 * Editorial wedding header with serif display title, expressive tonal pill badge, and clean subtitle.
 */
@Composable
fun AuthHeader(
  title: String = "Nuestro Día",
  subtitle: String = "Compartí recuerdos del evento",
  badgeText: String = "ÁLBUM COLABORATIVO",
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // m3e: Organic tonal pill badge accent typical of Google Pixel Expressive designs
    Surface(
      color = MaterialTheme.colorScheme.tertiaryContainer,
      shape = RoundedCornerShape(50),
      modifier = Modifier.padding(bottom = 12.dp),
    ) {
      Text(
        text = badgeText,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
      )
    }

    Text(
      text = title,
      style = MaterialTheme.typography.headlineLarge,
      color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

/**
 * Reusable outlined text field with persistent label, error supporting text, and visual transformation support.
 */
@Composable
fun AuthTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  errorMessage: String? = null,
  enabled: Boolean = true,
  singleLine: Boolean = true,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  trailingIcon: @Composable (() -> Unit)? = null,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    isError = errorMessage != null,
    supportingText = errorMessage?.let {
      { Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
    },
    enabled = enabled,
    singleLine = singleLine,
    shape = MaterialTheme.shapes.medium,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    trailingIcon = trailingIcon,
    modifier = modifier.fillMaxWidth(),
  )
}

/**
 * Password input field with show/hide toggle.
 */
@Composable
fun AuthPasswordField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String = "Contraseña",
  modifier: Modifier = Modifier,
  errorMessage: String? = null,
  enabled: Boolean = true,
  imeAction: ImeAction = ImeAction.Done,
  onImeAction: () -> Unit = {},
) {
  var passwordVisible by rememberSaveable { mutableStateOf(false) }

  AuthTextField(
    value = value,
    onValueChange = onValueChange,
    label = label,
    modifier = modifier,
    errorMessage = errorMessage,
    enabled = enabled,
    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Password,
      imeAction = imeAction,
    ),
    keyboardActions = KeyboardActions(
      onDone = { onImeAction() },
      onNext = { onImeAction() },
    ),
    trailingIcon = {
      // ponytail: Text button toggle avoids adding heavy material-icons-extended dependency
      TextButton(
        onClick = { passwordVisible = !passwordVisible },
        enabled = enabled,
      ) {
        Text(
          text = if (passwordVisible) "Ocultar" else "Mostrar",
          style = MaterialTheme.typography.labelSmall,
        )
      }
    },
    singleLine = true,
  )
}

/**
 * Primary action button with loading spinner indicator and expressive spring compression.
 */
@Composable
fun AuthPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  isLoading: Boolean = false,
) {
  val interactionSource = remember { MutableInteractionSource() }

  Button(
    onClick = onClick,
    enabled = enabled && !isLoading,
    interactionSource = interactionSource,
    shape = MaterialTheme.shapes.large,
    modifier = modifier
      .fillMaxWidth()
      .height(48.dp)
      .expressivePress(pressedScale = 0.96f, interactionSource = interactionSource), // m3e: tactile spring bounce
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        strokeWidth = 2.dp,
        color = MaterialTheme.colorScheme.onPrimary,
      )
    } else {
      Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
  }
}

/**
 * Feedback banner displaying error or success/notice messages with smooth spring expansion.
 */
@Composable
fun AuthFeedbackBanner(
  errorMessage: String? = null,
  noticeMessage: String? = null,
  modifier: Modifier = Modifier,
) {
  val isVisible = errorMessage != null || noticeMessage != null

  // m3e: Fluid spring expansion and fade entrance without jarring layout jumps
  AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = ExpressiveIntSizeSpring),
    exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = ExpressiveIntSizeSpring),
    modifier = modifier.animateContentSize(animationSpec = ExpressiveIntSizeSpring),
  ) {
    when {
      errorMessage != null -> {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          )
        }
      }
      noticeMessage != null -> {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = noticeMessage,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          )
        }
      }
    }
  }
}
