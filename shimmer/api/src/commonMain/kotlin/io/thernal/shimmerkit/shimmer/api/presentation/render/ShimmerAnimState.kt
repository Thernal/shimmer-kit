package io.thernal.shimmerkit.shimmer.api.presentation.render

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import io.thernal.shimmerkit.shimmer.api.presentation.clock.ShimmerClock
import io.thernal.shimmerkit.shimmer.api.presentation.config.ShimmerConfig
import io.thernal.shimmerkit.shimmer.api.presentation.host.LocalShimmerAnimated
import io.thernal.shimmerkit.shimmer.api.presentation.host.LocalShimmerProgress
import io.thernal.shimmerkit.shimmer.api.presentation.host.STILL_PROGRESS
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle

/** What the draw node reads each frame. The progress is read in draw only, so a frame never recomposes. */
@Immutable
internal data class ShimmerAnimState(
    val contentAlpha: Float,
    val shimmerAlpha: Float,
    val progress: State<Float>,
)

/** Null when there is nothing to draw: not loading, and the fade into the content has finished. */
@Composable
internal fun rememberShimmerAnimState(
    config: ShimmerConfig,
    style: ShimmerStyle,
): ShimmerAnimState? {
    if (LocalInspectionMode.current) {
        val still = remember { mutableFloatStateOf(STILL_PROGRESS) }
        return if (config.isEnabled) {
            ShimmerAnimState(
                contentAlpha = 0f,
                shimmerAlpha = 1f,
                progress = still,
            )
        } else {
            null
        }
    }

    val shimmerAlpha by animateFloatAsState(
        targetValue = if (config.isEnabled) {
            1f
        } else {
            0f
        },
        animationSpec = tween(
            durationMillis = if (config.isEnabled) {
                0
            } else {
                style.fadeOutMillis
            },
            easing = EaseInOutSine,
        ),
        label = FADE_LABEL,
    )
    if (!config.isEnabled && shimmerAlpha == 0f) {
        return null
    }

    return ShimmerAnimState(
        contentAlpha = 1f - shimmerAlpha,
        shimmerAlpha = shimmerAlpha,
        progress = rememberClockProgress(
            clock = config.clock,
            durationMillis = style.durationMillis,
        ),
    )
}

@Composable
private fun rememberClockProgress(
    clock: ShimmerClock,
    durationMillis: Int,
): State<Float> {
    val hostProgress = LocalShimmerProgress.current
    return when (clock) {
        ShimmerClock.Synced -> hostProgress

        is ShimmerClock.Staggered -> remember(key1 = hostProgress, key2 = clock) {
            derivedStateOf {
                staggeredProgress(
                    progress = hostProgress.value,
                    index = clock.index,
                    stepFraction = clock.stepFraction,
                )
            }
        }

        ShimmerClock.PerItem -> if (LocalShimmerAnimated.current) {
            rememberOwnProgress(durationMillis = durationMillis)
        } else {
            hostProgress
        }
    }
}

@Composable
private fun rememberOwnProgress(durationMillis: Int): State<Float> {
    val transition = rememberInfiniteTransition(label = OWN_TRANSITION_LABEL)
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = OWN_PROGRESS_LABEL,
    )
}

/** [progress] moved on by [index] steps of [stepFraction], wrapped into [0, 1) — negative indexes too. */
internal fun staggeredProgress(
    progress: Float,
    index: Int,
    stepFraction: Float,
): Float {
    val shifted = (progress + index * stepFraction) % 1f
    return if (shifted < 0f) {
        shifted + 1f
    } else {
        shifted
    }
}

private const val FADE_LABEL = "shimmer-fade"
private const val OWN_TRANSITION_LABEL = "shimmer-own"
private const val OWN_PROGRESS_LABEL = "shimmer-own-progress"
