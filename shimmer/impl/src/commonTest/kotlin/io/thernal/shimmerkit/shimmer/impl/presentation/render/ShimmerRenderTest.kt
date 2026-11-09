package io.thernal.shimmerkit.shimmer.impl.presentation.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class ShimmerRenderTest {
    @Test
    fun staggeredProgressMovesOnByIndexAndWraps() {
        assertEquals(
            expected = 0.7f,
            actual = staggeredProgress(progress = 0.5f, index = 2, stepFraction = 0.1f),
            absoluteTolerance = TOLERANCE,
        )
        assertEquals(
            expected = 0.1f,
            actual = staggeredProgress(progress = 0.9f, index = 2, stepFraction = 0.1f),
            absoluteTolerance = TOLERANCE,
        )
    }

    @Test
    fun staggeredProgressWrapsNegativeIndexesIntoTheCycle() {
        assertEquals(
            expected = 0.9f,
            actual = staggeredProgress(progress = 0.1f, index = -2, stepFraction = 0.1f),
            absoluteTolerance = TOLERANCE,
        )
    }

    @Test
    fun insetAreaKeepsThePaddingOnEverySide() {
        val area = insetArea(size = Size(width = 100f, height = 40f), paddingPx = 4f)

        assertEquals(expected = Offset(x = 4f, y = 4f), actual = area.topLeft)
        assertEquals(expected = Size(width = 92f, height = 32f), actual = area.size)
    }

    @Test
    fun insetAreaNeverGoesNegative() {
        val area = insetArea(size = Size(width = 6f, height = 6f), paddingPx = 4f)

        assertEquals(expected = Size.Zero, actual = area.size)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
