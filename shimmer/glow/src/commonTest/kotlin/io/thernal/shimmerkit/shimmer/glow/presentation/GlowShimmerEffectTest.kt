package io.thernal.shimmerkit.shimmer.glow.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

class GlowShimmerEffectTest {
    @Test
    fun theGlowIsDarkAtBothEndsOfACycleAndBrightestHalfWay() {
        assertEquals(expected = 0f, actual = glowIntensity(progress = 0f), absoluteTolerance = TOLERANCE)
        assertEquals(expected = 1f, actual = glowIntensity(progress = 0.5f), absoluteTolerance = TOLERANCE)
        assertEquals(expected = 0f, actual = glowIntensity(progress = 1f), absoluteTolerance = TOLERANCE)
    }

    @Test
    fun theGlowRisesAndFallsSymmetrically() {
        assertEquals(
            expected = glowIntensity(progress = 0.25f),
            actual = glowIntensity(progress = 0.75f),
            absoluteTolerance = TOLERANCE,
        )
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
