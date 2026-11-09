package io.thernal.shimmerkit.sample.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.thernal.shimmerkit.shimmer.api.presentation.config.ShimmerConfig
import io.thernal.shimmerkit.shimmer.api.presentation.config.ShimmerLayout
import io.thernal.shimmerkit.shimmer.api.presentation.effect.PlainShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.effect.ShimmerEffect
import io.thernal.shimmerkit.shimmer.api.presentation.host.ShimmerHost
import io.thernal.shimmerkit.shimmer.impl.presentation.effect.glow.GlowShimmerEffect
import io.thernal.shimmerkit.shimmer.impl.presentation.effect.sweep.SweepShimmerEffect

/**
 * The sample's root, as an application's root would be: the graph's composition locals installed in one spread
 * — without them no placeholder is drawn — then one [ShimmerHost] with the app's effect and style, then the
 * screen. Material is the sample's own UI kit; the placeholders do not use it.
 */
@Composable
fun SampleApp() {
    val graph = remember { createSampleGraph() }
    var effect by remember { mutableStateOf(SampleEffect.Sweep) }

    CompositionLocalProvider(values = graph.providedValues.toTypedArray()) {
        MaterialTheme {
            ShimmerHost(
                effect = effect.value,
                style = sampleShimmerStyle(),
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SampleScreen(
                        effect = effect,
                        onEffect = { effect = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleScreen(
    effect: SampleEffect,
    onEffect: (SampleEffect) -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(state = rememberScrollState())
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Loading",
                style = MaterialTheme.typography.titleMedium,
            )
            Switch(
                checked = isLoading,
                onCheckedChange = { isLoading = it },
            )
        }
        ChoiceRow(
            label = "Effect",
            choices = SampleEffect.entries,
            selected = effect,
            name = { it.label },
            onSelect = onEffect,
        )
        ShimmerLayout(config = ShimmerConfig(isEnabled = isLoading)) {
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                SampleSections()
            }
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    label: String,
    choices: List<T>,
    selected: T,
    name: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            choices.forEach { choice ->
                FilterChip(
                    selected = choice == selected,
                    onClick = { onSelect(choice) },
                    label = { Text(text = name(choice)) },
                )
            }
        }
    }
}

/** The effects the sample switches between — each a value an app passes to its host. */
internal enum class SampleEffect(
    val label: String,
    val value: ShimmerEffect,
) {
    Sweep(
        label = "Sweep",
        value = SweepShimmerEffect(),
    ),
    SharpSweep(
        label = "Sharp",
        value = SweepShimmerEffect(sharpness = SHARP),
    ),
    Glow(
        label = "Glow",
        value = GlowShimmerEffect(),
    ),
    Plain(
        label = "Plain",
        value = PlainShimmerEffect,
    ),
}

private const val SHARP = 0.6f
