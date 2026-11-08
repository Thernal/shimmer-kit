package io.thernal.shimmerkit.shimmer.sweep.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerArea
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerPaint
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle

/**
 * A highlight band, tilted by [tiltDegrees], travelling across the placeholder from its start edge to its end
 * edge once a cycle — in from outside the area and out past it, so a cycle starts and ends on the base colour.
 */
@Immutable
data class SweepShimmerEffect(
    /** The band's width as a fraction of the area's width. */
    val widthFraction: Float = DEFAULT_WIDTH_FRACTION,
    /** The band's lean from vertical, in degrees; clamped short of ±90. */
    val tiltDegrees: Float = DEFAULT_TILT_DEGREES,
    /** 0: the band fades in and out smoothly, brightest at its centre. 1: hard edges, bright across. */
    val sharpness: Float = DEFAULT_SHARPNESS,
) : ShimmerEffect {
    override fun prepare(
        area: ShimmerArea,
        style: ShimmerStyle,
        density: Density,
        layoutDirection: LayoutDirection,
    ): ShimmerPaint {
        val band = sweepBand(
            area = area,
            widthFraction = widthFraction,
            tiltDegrees = tiltDegrees,
            isRightToLeft = layoutDirection == LayoutDirection.Rtl,
        )
        if (band == null) {
            val still = SolidColor(value = style.baseColor)
            return ShimmerPaint { still }
        }
        val stops = sweepColorStops(
            baseColor = style.baseColor,
            highlightColor = style.highlightColor,
            sharpness = sharpness,
        )
        return ShimmerPaint { progress ->
            val (start, end) = band.endsAt(progress = progress)
            Brush.linearGradient(
                colorStops = stops,
                start = start,
                end = end,
            )
        }
    }
}

/**
 * The band's geometry over one area: where its gradient starts and ends at any progress. Null when the area or
 * the band has no width — there is nothing to sweep.
 */
internal fun sweepBand(
    area: ShimmerArea,
    widthFraction: Float,
    tiltDegrees: Float,
    isRightToLeft: Boolean,
): SweepBand? {
    val bandWidth = area.size.width * widthFraction.coerceAtLeast(0f)
    if (area.size.width <= 0f || area.size.height <= 0f || bandWidth <= 0f) {
        return null
    }
    // Clamped, so cos(±90°) = 0 never divides and tan never runs to infinity.
    val radians = tiltDegrees.coerceIn(-MAX_TILT_DEGREES, MAX_TILT_DEGREES) * DEGREES_TO_RADIANS
    val cos = kotlin.math.cos(radians).coerceAtLeast(MIN_COS)
    val sin = kotlin.math.sin(radians)
    val tiltOffset = sin / cos * area.size.height
    val halfWidthX = bandWidth / (2f * cos)
    val sideOffset = kotlin.math.abs(tiltOffset) / 2f + halfWidthX
    return SweepBand(
        area = area,
        cos = cos,
        sin = sin,
        halfWidthX = halfWidthX,
        sideOffset = sideOffset,
        travel = area.size.width + sideOffset * 2f,
        isRightToLeft = isRightToLeft,
    )
}

@Immutable
internal class SweepBand(
    private val area: ShimmerArea,
    private val cos: Float,
    private val sin: Float,
    private val halfWidthX: Float,
    val sideOffset: Float,
    val travel: Float,
    private val isRightToLeft: Boolean,
) {
    /** The band's centre, from the area's left edge, at [progress]: outside on the start side at 0, outside on the end side at 1. */
    fun centerAt(progress: Float): Float {
        val fromStart = -sideOffset + travel * progress
        return if (isRightToLeft) {
            area.size.width - fromStart
        } else {
            fromStart
        }
    }

    /** The gradient's start and end points at [progress]. */
    fun endsAt(progress: Float): Pair<Offset, Offset> {
        val centerX = area.topLeft.x + centerAt(progress = progress)
        val midY = area.topLeft.y + area.size.height / 2f
        val dx = halfWidthX * cos
        val dy = halfWidthX * sin
        return Pair(
            first = Offset(
                x = centerX - dx,
                y = midY + dy,
            ),
            second = Offset(
                x = centerX + dx,
                y = midY - dy,
            ),
        )
    }
}

/**
 * The band's colour stops for [sharpness]: 0 fades in and out over the whole band, peaking at its centre; towards 1
 * the fades shrink to the edges until the band is a hard-edged block of highlight.
 */
internal fun sweepColorStops(
    baseColor: Color,
    highlightColor: Color,
    sharpness: Float,
): Array<Pair<Float, Color>> {
    val clamped = sharpness.coerceIn(0f, 1f)
    if (clamped <= 0f) {
        return arrayOf(
            0f to baseColor,
            HALF to highlightColor,
            1f to baseColor,
        )
    }
    val edge = HALF * (1f - clamped)
    return arrayOf(
        0f to baseColor,
        edge to highlightColor,
        1f - edge to highlightColor,
        1f to baseColor,
    )
}

private const val DEFAULT_WIDTH_FRACTION = 0.6f
private const val DEFAULT_TILT_DEGREES = 15f
private const val DEFAULT_SHARPNESS = 0f
private const val MAX_TILT_DEGREES = 89f
private const val MIN_COS = 0.001f
private const val HALF = 0.5f
private const val DEGREES_TO_RADIANS = (kotlin.math.PI / 180.0).toFloat()
