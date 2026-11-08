package io.thernal.shimmerkit.sample.shared

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle

/**
 * The one file an app writes to give the placeholders its look: its design tokens mapped onto [ShimmerStyle].
 * The sample's tokens are a handful of literals; an app reads its own theme here.
 */
internal fun sampleShimmerStyle(): ShimmerStyle {
    return ShimmerStyle(
        baseColor = Color(color = 0xFFE7E3F3),
        highlightColor = Color(color = 0xFFF7F5FC),
        shape = RoundedCornerShape(size = 12.dp),
        padding = 1.dp,
    )
}
