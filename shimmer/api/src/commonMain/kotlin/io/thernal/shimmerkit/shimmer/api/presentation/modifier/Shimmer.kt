package io.thernal.shimmerkit.shimmer.api.presentation.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.clearAndSetSemantics
import io.thernal.shimmerkit.shimmer.api.presentation.config.LocalShimmerConfig
import io.thernal.shimmerkit.shimmer.api.presentation.config.ShimmerConfig
import io.thernal.shimmerkit.shimmer.api.presentation.host.LocalShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.host.LocalShimmerStyle
import io.thernal.shimmerkit.shimmer.api.presentation.render.ShimmerElement
import io.thernal.shimmerkit.shimmer.api.presentation.render.rememberShimmerAnimState

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
        val style = config.style ?: hostStyle
        val effect = config.effect ?: hostEffect
        val state = rememberShimmerAnimState(
            config = config,
            style = style,
        )

        if (state == null) {
            Modifier
        } else {
            val semantics = if (config.isEnabled) {
                Modifier.clearAndSetSemantics {}
            } else {
                Modifier
            }
            semantics.then(
                ShimmerElement(
                    state = state,
                    isOverlay = config.isOverlay,
                    style = style,
                    effect = effect,
                ),
            )
        }
    }
}
