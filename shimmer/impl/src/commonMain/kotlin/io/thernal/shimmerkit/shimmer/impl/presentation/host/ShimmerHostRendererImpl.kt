package io.thernal.shimmerkit.shimmer.impl.presentation.host

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalInspectionMode
import io.thernal.shimmerkit.shimmer.api.presentation.host.ShimmerHostRenderer
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle

/** The host's cycle. Without a host it stands still, half-way — the look a preview gets. */
internal val LocalShimmerProgress = staticCompositionLocalOf<State<Float>> { mutableFloatStateOf(STILL_PROGRESS) }

/** False when the host stands still (reduce motion, previews): a per-item clock then stands still too. */
internal val LocalShimmerAnimated = staticCompositionLocalOf { true }

/** Where a still placeholder sits in its cycle. */
internal const val STILL_PROGRESS = 0.5f

/** The host's half of the contract: the cycle, and whether it moves at all. */
class ShimmerHostRendererImpl : ShimmerHostRenderer {
    @Composable
    override fun Render(
        style: ShimmerStyle,
        isAnimated: Boolean,
        content: @Composable () -> Unit,
    ) {
        val isMoving = isAnimated && !isReduceMotionEnabled() && !LocalInspectionMode.current
        val progress = if (isMoving) {
            rememberHostProgress(durationMillis = style.durationMillis)
        } else {
            remember { mutableFloatStateOf(STILL_PROGRESS) }
        }

        CompositionLocalProvider(
            LocalShimmerProgress provides progress,
            LocalShimmerAnimated provides isMoving,
            content = content,
        )
    }
}

@Composable
private fun rememberHostProgress(durationMillis: Int): State<Float> {
    val transition = rememberInfiniteTransition(label = HOST_TRANSITION_LABEL)
    val animated = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(
                offsetMillis = (durationMillis * FIRST_FRAME_PROGRESS).toInt(),
                offsetType = StartOffsetType.FastForward,
            ),
        ),
        label = HOST_PROGRESS_LABEL,
    )

    // On the first frame the transition may still report its initial value, which draws a placeholder
    // with no highlight at all; show the point the fast-forward starts from until it runs.
    var isStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isStarted = true
    }
    val firstFrame = remember { mutableFloatStateOf(FIRST_FRAME_PROGRESS) }

    return if (isStarted) {
        animated
    } else {
        firstFrame
    }
}

/** Whether the platform asks apps to keep motion to a minimum. Read when the host composes. */
@Composable
internal expect fun isReduceMotionEnabled(): Boolean

private const val HOST_TRANSITION_LABEL = "shimmer-host"
private const val HOST_PROGRESS_LABEL = "shimmer-host-progress"
private const val FIRST_FRAME_PROGRESS = 0.33f
