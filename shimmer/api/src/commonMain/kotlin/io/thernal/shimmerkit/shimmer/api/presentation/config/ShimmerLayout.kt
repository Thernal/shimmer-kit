package io.thernal.shimmerkit.shimmer.api.presentation.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

internal val LocalShimmerConfig = compositionLocalOf { ShimmerConfig() }

/** Gives every `Modifier.shimmer()` below [config]. */
@Composable
fun ShimmerLayout(
    config: ShimmerConfig,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        value = LocalShimmerConfig provides config,
        content = content,
    )
}

/** Turns the placeholders below on or off, keeping the rest of the config around it. */
@Composable
fun ShimmerLayout(
    isEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    ShimmerLayout(
        config = LocalShimmerConfig.current.copy(isEnabled = isEnabled),
        content = content,
    )
}
