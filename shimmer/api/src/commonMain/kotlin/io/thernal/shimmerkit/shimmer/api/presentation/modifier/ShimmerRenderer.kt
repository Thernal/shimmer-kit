package io.thernal.shimmerkit.shimmer.api.presentation.modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.platform.LocalInspectionMode
import io.thernal.shimmerkit.shimmer.api.presentation.config.LocalShimmerConfig
import io.thernal.shimmerkit.shimmer.api.presentation.config.ShimmerConfig
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.host.LocalShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.host.LocalShimmerStyle
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle

/**
 * Turns one element into a placeholder. `impl` owns the draw node, the fade and the clocks; `api` exposes only
 * the [shimmer] modifier below. [style] and [effect] are already resolved: the config's own, or the host's.
 */
interface ShimmerRenderer {
    @Composable
    fun rememberModifier(
        config: ShimmerConfig,
        style: ShimmerStyle,
        effect: ShimmerEffect,
    ): Modifier
}

val LocalShimmerRenderer =
    compositionLocalOf<ShimmerRenderer> { PreviewShimmerRenderer }

/**
 * The renderer when none is installed. In a preview a placeholder is a still fill in the base colour, so a
 * screen previews without `impl`, and an overlay leaves the element as it is. Anywhere else it changes nothing:
 * the filler showing through gives a missing installation away, where a still fill would pass for one that
 * only forgot to animate.
 */
private object PreviewShimmerRenderer : ShimmerRenderer {
    @Composable
    override fun rememberModifier(
        config: ShimmerConfig,
        style: ShimmerStyle,
        effect: ShimmerEffect,
    ): Modifier {
        val isStillFill = config.isEnabled && !config.isOverlay && LocalInspectionMode.current
        return if (isStillFill) {
            Modifier.drawWithCache {
                val outline = style.shape.createOutline(
                    size = size,
                    layoutDirection = layoutDirection,
                    density = this,
                )
                onDrawWithContent {
                    drawOutline(
                        outline = outline,
                        color = style.baseColor,
                    )
                }
            }
        } else {
            Modifier
        }
    }
}

/**
 * Makes this element a placeholder while the ambient `ShimmerLayout` is loading — or while [overrides]
 * says so. [overrides] gets the ambient config with the host's style and effect filled in:
 *
 * ```
 * Modifier.shimmer()                                                 // the layout's config as it is
 * Modifier.shimmer { it.copy(isOverlay = true) }                     // shimmer inside an icon's own pixels
 * Modifier.shimmer { it.copy(clock = ShimmerClock.Staggered(index)) } // a wave down a list
 * ```
 *
 * While it is a placeholder its content is hidden from screen readers too — the text under a placeholder
 * is filler. Say "loading" once, on the container, in the app's own words.
 */
fun Modifier.shimmer(overrides: @Composable (ShimmerConfig) -> ShimmerConfig = { it }): Modifier {
    return composed {
        val ambient = LocalShimmerConfig.current
        val hostStyle = LocalShimmerStyle.current
        val hostEffect = LocalShimmerEffect.current
        val config = overrides(
            ambient.copy(
                style = ambient.style ?: hostStyle,
                effect = ambient.effect ?: hostEffect,
            ),
        )
        LocalShimmerRenderer.current.rememberModifier(
            config = config,
            style = config.style ?: hostStyle,
            effect = config.effect ?: hostEffect,
        )
    }
}
