package io.thernal.shimmerkit.shimmer.impl.presentation.render

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import io.thernal.shimmerkit.shimmer.api.presentation.config.ShimmerConfig
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle
import io.thernal.shimmerkit.shimmer.impl.presentation.host.LocalShimmerProgress

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
    val progress = LocalShimmerProgress.current
    if (LocalInspectionMode.current) {
        return if (config.isEnabled) {
            ShimmerAnimState(
                contentAlpha = 0f,
                shimmerAlpha = 1f,
                progress = progress,
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
        progress = progress,
    )
}

private const val FADE_LABEL = "shimmer-fade"
