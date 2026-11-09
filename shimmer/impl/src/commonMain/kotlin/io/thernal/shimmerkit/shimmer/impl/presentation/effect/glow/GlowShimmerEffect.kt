package io.thernal.shimmerkit.shimmer.impl.presentation.effect.glow

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerArea
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerPaint
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max

/**
 * A soft highlight breathing out from the placeholder's centre: it brightens to the style's highlight colour
 * half-way through a cycle and dims back to the base colour at its end. Nothing travels, so it suits a
 * single large placeholder — an image, a card — better than a list of rows.
 */
@Immutable
data class GlowShimmerEffect(
    /** How far the glow reaches, as a fraction of the area's longer side. */
    val radiusFraction: Float = DEFAULT_RADIUS_FRACTION,
) : ShimmerEffect {
    override fun prepare(
        area: ShimmerArea,
        style: ShimmerStyle,
        density: Density,
        layoutDirection: LayoutDirection,
    ): ShimmerPaint {
        val radius = max(a = area.size.width, b = area.size.height) * radiusFraction
        if (radius <= 0f) {
            val still = SolidColor(value = style.baseColor)
            return ShimmerPaint { still }
        }
        val center = Offset(
            x = area.topLeft.x + area.size.width / 2f,
            y = area.topLeft.y + area.size.height / 2f,
        )
        return ShimmerPaint { progress ->
            val glow = lerp(
                start = style.baseColor,
                stop = style.highlightColor,
                fraction = glowIntensity(progress = progress),
            )
            Brush.radialGradient(
                colors = listOf(glow, style.baseColor),
                center = center,
                radius = radius,
            )
        }
    }
}

/** 0 at the start and end of a cycle, 1 half-way, easing in and out like a breath. */
internal fun glowIntensity(progress: Float): Float {
    return (1f - cos(progress * 2f * PI.toFloat())) / 2f
}

private const val DEFAULT_RADIUS_FRACTION = 0.75f
