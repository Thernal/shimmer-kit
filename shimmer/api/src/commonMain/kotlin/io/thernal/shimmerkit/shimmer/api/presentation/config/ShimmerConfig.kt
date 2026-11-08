package io.thernal.shimmerkit.shimmer.api.presentation.config

import androidx.compose.runtime.Immutable
import io.thernal.shimmerkit.shimmer.api.presentation.clock.ShimmerClock
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle

/**
 * What the placeholders below a `ShimmerLayout` do — or one placeholder, through `Modifier.shimmer { … }`.
 */
@Immutable
data class ShimmerConfig(
    /** Loading: the placeholder replaces the content. When it turns false the content fades in. */
    val isEnabled: Boolean = false,
    /**
     * The content stays drawn and the effect paints over its own pixels only — an icon or a line of text
     * shimmering in its own shape. Otherwise the placeholder fills the element's area, clipped to the style's
     * shape, and the content is hidden.
     */
    val isOverlay: Boolean = false,
    val clock: ShimmerClock = ShimmerClock.Synced,
    /** The look; null takes the host's. */
    val style: ShimmerStyle? = null,
    /** The effect; null takes the host's. */
    val effect: ShimmerEffect? = null,
)
