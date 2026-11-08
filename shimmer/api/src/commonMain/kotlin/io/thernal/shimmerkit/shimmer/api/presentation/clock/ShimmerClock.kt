package io.thernal.shimmerkit.shimmer.api.presentation.clock

import androidx.compose.runtime.Immutable

/** Where a placeholder's progress through the cycle comes from. */
@Immutable
sealed interface ShimmerClock {
    /** The host's one cycle: every placeholder on screen moves together. The default. */
    data object Synced : ShimmerClock

    /** A cycle of its own, from the moment the placeholder appears: each item its own wave. */
    data object PerItem : ShimmerClock

    /**
     * The host's cycle, shifted by [index] × [stepFraction] of a cycle — a wave rolling down a list.
     * Still one animation for the whole screen.
     */
    data class Staggered(
        val index: Int,
        val stepFraction: Float = DEFAULT_STEP_FRACTION,
    ) : ShimmerClock
}

private const val DEFAULT_STEP_FRACTION = 0.08f
