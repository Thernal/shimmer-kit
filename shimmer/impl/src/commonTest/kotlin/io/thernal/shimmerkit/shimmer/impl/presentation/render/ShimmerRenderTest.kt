package io.thernal.shimmerkit.shimmer.impl.presentation.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class ShimmerRenderTest {
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
}
