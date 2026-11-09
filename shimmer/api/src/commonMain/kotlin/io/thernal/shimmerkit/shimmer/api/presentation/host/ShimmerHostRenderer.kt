package io.thernal.shimmerkit.shimmer.api.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import io.thernal.shimmerkit.shimmer.api.presentation.effect.PlainShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle

internal val LocalShimmerStyle = staticCompositionLocalOf { ShimmerStyle() }
internal val LocalShimmerEffect = staticCompositionLocalOf<ShimmerEffect> { PlainShimmerEffect }

/**
 * Runs the one cycle synced placeholders follow. `impl` owns the transition and the platform's reduce-motion
 * setting; `api` exposes only the [ShimmerHost] composable below.
 */
interface ShimmerHostRenderer {
    @Composable
    fun Render(
        style: ShimmerStyle,
        isAnimated: Boolean,
        content: @Composable () -> Unit,
    )
}

val LocalShimmerHostRenderer =
    compositionLocalOf<ShimmerHostRenderer> { PreviewShimmerHostRenderer }

/** Runs no cycle and shows the content: a host is what an app's whole screen sits under. */
private object PreviewShimmerHostRenderer : ShimmerHostRenderer {
    @Composable
    override fun Render(
        style: ShimmerStyle,
        isAnimated: Boolean,
        content: @Composable () -> Unit,
    ) {
        content()
    }
}

/**
 * The root of everything that shimmers: one cycle every synced placeholder follows, and the [effect] and
 * [style] placeholders take when their config names none. Compose it once, near the app's root; a part of
 * the screen that needs another effect or speed sits under a host of its own, and follows that one.
 *
 * [isAnimated] false stands every placeholder still in the style's colours. The platform's reduce-motion
 * setting stands them still too, whatever this says.
 */
@Composable
fun ShimmerHost(
    effect: ShimmerEffect,
    style: ShimmerStyle = ShimmerStyle(),
    isAnimated: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalShimmerStyle provides style,
        LocalShimmerEffect provides effect,
    ) {
        LocalShimmerHostRenderer.current.Render(
            style = style,
            isAnimated = isAnimated,
            content = content,
        )
    }
}
