package com.example.amulet.core.design.foundation.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale

fun slideInFromRight(): EnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
)

fun slideOutToLeft(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
)

fun fadeInFast(): EnterTransition = fadeIn(
    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
)

fun fadeOutFast(): ExitTransition = fadeOut(
    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
)

@Composable
fun ButtonPressEffect(
    pressed: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "buttonPress"
    )
    Box(modifier = modifier.scale(scale)) {
        content()
    }
}

@Composable
fun BreathingPulse(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (alpha: Float, scale: Float) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "breathing")
    val alpha by transition.animateFloat(
        initialValue = if (isPlaying) 0.5f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingAlpha"
    )
    val scale by transition.animateFloat(
        initialValue = if (isPlaying) 0.8f else 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )
    Box(modifier = modifier.scale(scale).alpha(alpha)) {
        content(alpha, scale)
    }
}
