package io.thernal.shimmerkit.shimmer.api.presentation.render

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.unit.LayoutDirection
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerArea
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerPaint
import io.thernal.shimmerkit.shimmer.api.presentation.style.ShimmerStyle

internal data class ShimmerElement(
    val state: ShimmerAnimState,
    val isOverlay: Boolean,
    val style: ShimmerStyle,
    val effect: ShimmerEffect,
) : ModifierNodeElement<ShimmerNode>() {
    override fun create(): ShimmerNode {
        return ShimmerNode(
            state = state,
            isOverlay = isOverlay,
            style = style,
            effect = effect,
        )
    }

    override fun update(node: ShimmerNode) {
        node.update(
            state = state,
            isOverlay = isOverlay,
            style = style,
            effect = effect,
        )
    }
}

/**
 * Draws one placeholder. The area, its clip and the effect's prepared paint are kept until the size,
 * density, layout direction, style or effect changes; each frame only asks the paint for a brush.
 */
internal class ShimmerNode(
    private var state: ShimmerAnimState,
    private var isOverlay: Boolean,
    private var style: ShimmerStyle,
    private var effect: ShimmerEffect,
) : Modifier.Node(), DrawModifierNode, ObserverModifierNode {

    private val layerPaint = Paint()
    private var cache: Cache? = null

    override fun onAttach() {
        observeAnimationReads()
    }

    override fun onObservedReadsChanged() {
        invalidateDraw()
        observeAnimationReads()
    }

    fun update(
        state: ShimmerAnimState,
        isOverlay: Boolean,
        style: ShimmerStyle,
        effect: ShimmerEffect,
    ) {
        if (this.style != style || this.effect != effect) {
            cache = null
        }
        this.state = state
        this.isOverlay = isOverlay
        this.style = style
        this.effect = effect
        if (isAttached) {
            observeAnimationReads()
        }
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        val current = currentCache()
        val isVisible = state.shimmerAlpha > 0f &&
            current.area.size.width > 0f &&
            current.area.size.height > 0f

        if (isOverlay) {
            drawOverContent(
                cache = current,
                isVisible = isVisible,
            )
        } else {
            drawInsteadOfContent(
                cache = current,
                isVisible = isVisible,
            )
        }
    }

    // The content stays; the effect paints only where the content has pixels.
    private fun ContentDrawScope.drawOverContent(
        cache: Cache,
        isVisible: Boolean,
    ) {
        if (!isVisible) {
            drawContent()
            return
        }
        drawContext.canvas.saveLayer(
            bounds = Rect(
                offset = Offset.Zero,
                size = size,
            ),
            paint = layerPaint.apply { alpha = 1f },
        )
        drawContent()
        drawRect(
            brush = cache.paint.brushAt(progress = state.progress.value),
            topLeft = cache.area.topLeft,
            size = cache.area.size,
            alpha = state.shimmerAlpha,
            blendMode = BlendMode.SrcAtop,
        )
        drawContext.canvas.restore()
    }

    // The content fades in as the placeholder fades out.
    private fun ContentDrawScope.drawInsteadOfContent(
        cache: Cache,
        isVisible: Boolean,
    ) {
        val contentAlpha = state.contentAlpha
        if (contentAlpha >= 1f) {
            drawContent()
        } else if (contentAlpha > 0f) {
            drawContext.canvas.saveLayer(
                bounds = Rect(
                    offset = Offset.Zero,
                    size = size,
                ),
                paint = layerPaint.apply { alpha = contentAlpha },
            )
            drawContent()
            drawContext.canvas.restore()
        }
        if (isVisible) {
            clipPath(path = cache.clipPath) {
                drawRect(
                    brush = cache.paint.brushAt(progress = state.progress.value),
                    topLeft = cache.area.topLeft,
                    size = cache.area.size,
                    alpha = state.shimmerAlpha,
                )
            }
        }
    }

    private fun observeAnimationReads() {
        observeReads {
            state.progress.value
        }
    }

    private fun ContentDrawScope.currentCache(): Cache {
        val existing = cache
        if (existing != null &&
            existing.size == size &&
            existing.density == density &&
            existing.fontScale == fontScale &&
            existing.layoutDirection == layoutDirection
        ) {
            return existing
        }
        val area = insetArea(
            size = size,
            paddingPx = style.padding.toPx(),
        )
        val fresh = Cache(
            size = size,
            density = density,
            fontScale = fontScale,
            layoutDirection = layoutDirection,
            area = area,
            clipPath = clipPathOf(
                shape = style.shape,
                area = area,
                layoutDirection = layoutDirection,
                density = this,
            ),
            paint = effect.prepare(
                area = area,
                style = style,
                density = this,
                layoutDirection = layoutDirection,
            ),
        )
        cache = fresh
        return fresh
    }

    private class Cache(
        val size: Size,
        val density: Float,
        val fontScale: Float,
        val layoutDirection: LayoutDirection,
        val area: ShimmerArea,
        val clipPath: Path,
        val paint: ShimmerPaint,
    )
}
