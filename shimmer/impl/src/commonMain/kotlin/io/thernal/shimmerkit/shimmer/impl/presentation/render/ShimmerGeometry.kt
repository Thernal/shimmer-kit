package io.thernal.shimmerkit.shimmer.impl.presentation.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerArea

/** The element's [size] inset by [paddingPx] on every side; never negative. */
internal fun insetArea(
    size: Size,
    paddingPx: Float,
): ShimmerArea {
    val padding = paddingPx.coerceAtLeast(0f)
    return ShimmerArea(
        topLeft = Offset(
            x = padding,
            y = padding,
        ),
        size = Size(
            width = (size.width - padding * SIDES).coerceAtLeast(0f),
            height = (size.height - padding * SIDES).coerceAtLeast(0f),
        ),
    )
}

/** [shape]'s outline over [area], as the path the placeholder is clipped to. */
internal fun clipPathOf(
    shape: Shape,
    area: ShimmerArea,
    layoutDirection: LayoutDirection,
    density: Density,
): Path {
    val outline = shape.createOutline(
        size = area.size,
        layoutDirection = layoutDirection,
        density = density,
    )
    return Path().apply {
        when (outline) {
            is Outline.Rectangle -> addRect(outline.rect)
            is Outline.Rounded -> addRoundRect(outline.roundRect)
            is Outline.Generic -> addPath(outline.path)
        }
        translate(area.topLeft)
    }
}

private const val SIDES = 2f
