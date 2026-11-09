package io.thernal.shimmerkit.shimmer.impl.presentation.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import io.thernal.shimmerkit.shimmer.api.presentation.config.ShimmerConfig
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.modifier.ShimmerRenderer
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle
import io.thernal.shimmerkit.shimmer.impl.presentation.render.ShimmerElement
import io.thernal.shimmerkit.shimmer.impl.presentation.render.rememberShimmerAnimState

class ShimmerRendererImpl : ShimmerRenderer {
    @Composable
    override fun rememberModifier(
        config: ShimmerConfig,
        style: ShimmerStyle,
        effect: ShimmerEffect,
    ): Modifier {
        val state = rememberShimmerAnimState(
            config = config,
            style = style,
        )

        return if (state == null) {
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
