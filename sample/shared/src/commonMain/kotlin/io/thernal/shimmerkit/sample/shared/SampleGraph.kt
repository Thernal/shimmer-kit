package io.thernal.shimmerkit.sample.shared

import androidx.compose.runtime.ProvidedValue
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

/**
 * The application graph. Composition locals arrive in one set — the two renderers `ShimmerProvidersModule`
 * contributes — so nothing here names the kit.
 */
@DependencyGraph(AppScope::class)
interface SampleGraph {
    val providedValues: Set<ProvidedValue<*>>
}

fun createSampleGraph(): SampleGraph {
    return createGraph<SampleGraph>()
}
