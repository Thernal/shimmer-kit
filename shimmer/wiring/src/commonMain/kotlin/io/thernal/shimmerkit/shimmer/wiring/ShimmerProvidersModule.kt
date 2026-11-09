package io.thernal.shimmerkit.shimmer.wiring

import androidx.compose.runtime.ProvidedValue
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.thernal.shimmerkit.shimmer.api.presentation.host.LocalShimmerHostRenderer
import io.thernal.shimmerkit.shimmer.api.presentation.host.ShimmerHostRenderer
import io.thernal.shimmerkit.shimmer.api.presentation.modifier.LocalShimmerRenderer
import io.thernal.shimmerkit.shimmer.api.presentation.modifier.ShimmerRenderer
import io.thernal.shimmerkit.shimmer.impl.presentation.host.ShimmerHostRendererImpl
import io.thernal.shimmerkit.shimmer.impl.presentation.modifier.ShimmerRendererImpl

/**
 * Binds the shimmer renderers into an application graph — a worked example rather than a fixed part of the
 * kit, since `api` and `impl` name no injection framework. An effect is not bound: it is a value the root
 * passes to `ShimmerHost`.
 */
@BindingContainer
@ContributesTo(AppScope::class)
interface ShimmerProvidersModule {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideShimmerHostRenderer(): ShimmerHostRenderer {
            return ShimmerHostRendererImpl()
        }

        @Provides
        @SingleIn(AppScope::class)
        fun provideShimmerRenderer(): ShimmerRenderer {
            return ShimmerRendererImpl()
        }

        /**
         * Contributed into the graph's `Set<ProvidedValue<*>>`, so a composition root installs every
         * feature's composition locals in one `CompositionLocalProvider` without naming any of them.
         */
        @Provides
        @IntoSet
        fun provideShimmerHostRendererValue(renderer: ShimmerHostRenderer): ProvidedValue<*> {
            return LocalShimmerHostRenderer provides renderer
        }

        @Provides
        @IntoSet
        fun provideShimmerRendererValue(renderer: ShimmerRenderer): ProvidedValue<*> {
            return LocalShimmerRenderer provides renderer
        }
    }
}
