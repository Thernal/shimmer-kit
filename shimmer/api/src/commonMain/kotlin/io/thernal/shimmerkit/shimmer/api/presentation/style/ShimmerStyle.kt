package io.thernal.shimmerkit.shimmer.api.presentation.style

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How placeholders look and move — whatever the effect. Neutral defaults; an app maps its design tokens
 * onto one value of this in its own design-system module and gives it to `ShimmerHost`.
 */
@Immutable
data class ShimmerStyle(
    /** The placeholder's colour between highlights. */
    val baseColor: Color = ShimmerDefaults.BaseColor,
    /** The colour at the brightest point of the effect. */
    val highlightColor: Color = ShimmerDefaults.HighlightColor,
    /** The placeholder's outline; the effect is clipped to it. */
    val shape: Shape = ShimmerDefaults.Shape,
    /** Inset from the element's bounds, so neighbouring placeholders do not touch. */
    val padding: Dp = ShimmerDefaults.Padding,
    /** One cycle of the effect: the host's style sets it, for every placeholder below. */
    val durationMillis: Int = ShimmerDefaults.DURATION_MILLIS,
    /** How long the placeholder takes to fade into the real content once loading ends. */
    val fadeOutMillis: Int = ShimmerDefaults.FADE_OUT_MILLIS,
)

/** The kit's fallbacks — for previews and tests; an app's look comes from its own tokens. */
object ShimmerDefaults {
    val BaseColor: Color = Color(color = 0xFFE3E5E8)
    val HighlightColor: Color = Color(color = 0xFFF5F6F7)
    val Shape: Shape = RoundedCornerShape(size = 8.dp)
    val Padding: Dp = 0.dp
    const val DURATION_MILLIS: Int = 1600
    const val FADE_OUT_MILLIS: Int = 300
}
