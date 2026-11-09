package io.thernal.shimmerkit.shimmer.impl.presentation.effect.sweep

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerArea
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SweepShimmerEffectTest {
    private val area = ShimmerArea(topLeft = Offset.Zero, size = Size(width = 200f, height = 40f))

    @Test
    fun theBandStartsAndEndsOutsideTheArea() {
        val band = assertNotNull(sweepBand(area = area, widthFraction = 0.6f, tiltDegrees = 15f, isRightToLeft = false))

        assertTrue(band.centerAt(progress = 0f) + band.sideOffset <= 0f + TOLERANCE)
        assertTrue(band.centerAt(progress = 1f) - band.sideOffset >= area.size.width - TOLERANCE)
    }

    @Test
    fun rightToLeftSweepsFromTheRightEdge() {
        val band = assertNotNull(sweepBand(area = area, widthFraction = 0.6f, tiltDegrees = 15f, isRightToLeft = true))

        assertTrue(band.centerAt(progress = 0f) > area.size.width)
        assertTrue(band.centerAt(progress = 1f) < 0f)
    }

    @Test
    fun noWidthMeansNoBand() {
        assertNull(sweepBand(area = area, widthFraction = 0f, tiltDegrees = 15f, isRightToLeft = false))
        assertNull(
            sweepBand(
                area = ShimmerArea(topLeft = Offset.Zero, size = Size.Zero),
                widthFraction = 0.6f,
                tiltDegrees = 15f,
                isRightToLeft = false,
            ),
        )
    }

    @Test
    fun aSmoothBandPeaksAtItsCentre() {
        val stops = sweepColorStops(baseColor = Color.Black, highlightColor = Color.White, sharpness = 0f)

        assertEquals(expected = listOf(0f, 0.5f, 1f), actual = stops.map { it.first })
        assertEquals(expected = Color.White, actual = stops[1].second)
    }

    @Test
    fun aSharpBandHoldsItsHighlightBetweenItsEdges() {
        val stops = sweepColorStops(baseColor = Color.Black, highlightColor = Color.White, sharpness = 0.5f)

        assertEquals(expected = listOf(0f, 0.25f, 0.75f, 1f), actual = stops.map { it.first })
    }

    private companion object {
        const val TOLERANCE = 0.001f
    }
}
