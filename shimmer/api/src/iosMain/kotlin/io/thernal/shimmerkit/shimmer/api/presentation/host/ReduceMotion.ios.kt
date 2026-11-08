package io.thernal.shimmerkit.shimmer.api.presentation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
internal actual fun isReduceMotionEnabled(): Boolean {
    return remember { UIAccessibilityIsReduceMotionEnabled() }
}
