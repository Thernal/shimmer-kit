package io.thernal.shimmerkit.sample.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.thernal.shimmerkit.shimmer.api.presentation.clock.ShimmerClock
import io.thernal.shimmerkit.shimmer.api.presentation.modifier.shimmer

/** A card's image, a list of rows and a line of icons — every one a placeholder while the layout loads. */
@Composable
internal fun SampleSections(clock: SampleClock) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio = IMAGE_RATIO)
            .shimmer()
            .background(color = MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "An image")
    }
    repeat(times = ROWS) { index ->
        SampleRow(
            index = index,
            clock = clock,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
        repeat(times = ICONS) {
            // isOverlay keeps the content's own shape: the highlight passes through the dot, not a box around it.
            Box(
                modifier = Modifier
                    .size(size = 28.dp)
                    .shimmer { config -> config.copy(isOverlay = true) }
                    .clip(shape = CircleShape)
                    .background(color = Color.DarkGray),
            )
        }
    }
}

@Composable
private fun SampleRow(
    index: Int,
    clock: SampleClock,
) {
    val rowClock = when (clock) {
        SampleClock.Synced -> ShimmerClock.Synced
        SampleClock.PerItem -> ShimmerClock.PerItem
        SampleClock.Staggered -> ShimmerClock.Staggered(index = index)
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The placeholder takes the style's shape, not the content's clip: a round avatar asks for a round one.
        Box(
            modifier = Modifier
                .size(size = 44.dp)
                .shimmer { config ->
                    config.copy(
                        clock = rowClock,
                        style = config.style?.copy(shape = CircleShape),
                    )
                }
                .clip(shape = CircleShape)
                .background(color = MaterialTheme.colorScheme.secondary),
        )
        Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(
                text = "Person number ${index + 1}",
                modifier = Modifier.shimmer { config -> config.copy(clock = rowClock) },
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Joined a while ago",
                modifier = Modifier.shimmer { config -> config.copy(clock = rowClock) },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private const val IMAGE_RATIO = 16f / 9f
private const val ROWS = 6
private const val ICONS = 5
