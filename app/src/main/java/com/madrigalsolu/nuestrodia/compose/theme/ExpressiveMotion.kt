package com.madrigalsolu.nuestrodia.compose.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

// m3e: Material 3 Expressive spring physics specs matching Google Pixel tactile motion
val ExpressiveSpringBouncy = spring<Float>(
  dampingRatio = Spring.DampingRatioMediumBouncy,
  stiffness = Spring.StiffnessMediumLow,
)

val ExpressiveSpringSmooth = spring<Float>(
  dampingRatio = Spring.DampingRatioNoBouncy,
  stiffness = Spring.StiffnessMedium,
)

val ExpressiveIntOffsetSpring = spring<IntOffset>(
  dampingRatio = Spring.DampingRatioMediumBouncy,
  stiffness = Spring.StiffnessMediumLow,
)

val ExpressiveIntSizeSpring = spring<IntSize>(
  dampingRatio = Spring.DampingRatioNoBouncy,
  stiffness = Spring.StiffnessMedium,
)

/**
 * m3e: Tactile spring compression on press.
 * Provides the springy physical feedback signature of Pixel apps.
 */
fun Modifier.expressivePress(
  pressedScale: Float = 0.95f,
  interactionSource: InteractionSource? = null,
): Modifier = composed {
  val source = interactionSource ?: remember { MutableInteractionSource() }
  val isPressed by source.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) pressedScale else 1f,
    animationSpec = ExpressiveSpringBouncy,
    label = "ExpressivePressScale",
  )
  this.graphicsLayer {
    scaleX = scale
    scaleY = scale
  }
}

/**
 * m3e: Clickable modifier combining click dispatch with spring press scaling.
 */
fun Modifier.expressiveClickable(
  pressedScale: Float = 0.95f,
  onClick: () -> Unit,
): Modifier = composed {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) pressedScale else 1f,
    animationSpec = ExpressiveSpringBouncy,
    label = "ExpressivePressScale",
  )
  this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .clickable(
      interactionSource = interactionSource,
      indication = null,
      onClick = onClick,
    )
}

/**
 * m3e / ponytail: Ambient breathing scale transition for TV presentation / ambient photo view.
 * Emulates the gentle Ken Burns living motion of Google Photos screensavers without third-party libraries.
 */
@Composable
fun rememberExpressiveAmbientScale(
  minScale: Float = 1.0f,
  maxScale: Float = 1.04f,
  durationMillis: Int = 8000,
): State<Float> {
  val infiniteTransition = rememberInfiniteTransition(label = "ExpressiveAmbientZoom")
  return infiniteTransition.animateFloat(
    initialValue = minScale,
    targetValue = maxScale,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "AmbientZoomScale",
  )
}
