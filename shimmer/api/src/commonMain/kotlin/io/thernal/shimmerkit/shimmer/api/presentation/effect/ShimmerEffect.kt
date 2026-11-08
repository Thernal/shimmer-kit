package io.thernal.shimmerkit.shimmer.api.presentation.effect

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle

/**
 * What a placeholder is painted with. The kit's own effects are features an app takes or leaves
 * (`SweepShimmerEffect`, `GlowShimmerEffect`); an app may write one of its own.
 *
 * An effect is a value: make it a `data class` or a `data object`. Placeholders keep what [prepare]
 * returned until the effect, the style, the area or the density changes — equal effects are not
 * prepared again.
 */
@Stable
interface ShimmerEffect {
    /** Whatever depends on the area and the style alone — geometry, colour stops — is computed here, once. */
    fun prepare(
        area: ShimmerArea,
        style: ShimmerStyle,
        density: Density,
        layoutDirection: LayoutDirection,
    ): ShimmerPaint
}

/** One placeholder's paint through a cycle. */
fun interface ShimmerPaint {
    /**
     * The brush the placeholder's [ShimmerArea] is filled with at [progress], which runs from 0 to 1
     * over one cycle and starts again. Called every frame: keep it to arithmetic.
     */
    fun brushAt(progress: Float): Brush
}

/** The rectangle a placeholder fills, inside the style's padding; the fill is clipped to the style's shape. */
@Immutable
data class ShimmerArea(
    val topLeft: Offset,
    val size: Size,
)

/**
 * The effect when none is installed: the style's base colour, still. It needs no feature of the kit, so a
 * preview, a test or an app that took no effect still shows its placeholders.
 */
data object PlainShimmerEffect : ShimmerEffect {
    override fun prepare(
        area: ShimmerArea,
        style: ShimmerStyle,
        density: Density,
        layoutDirection: LayoutDirection,
    ): ShimmerPaint {
        val brush = SolidColor(value = style.baseColor)
        return ShimmerPaint { brush }
    }
}
