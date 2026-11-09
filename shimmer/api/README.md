# Using shimmer-kit

Packages: `io.thernal.shimmerkit.shimmer.api.presentation.*` (renamed in an app that took the kit), the effects
in `….shimmer.sweep.presentation` and `….shimmer.glow.presentation`. A screen depends on `api` and names nothing
else; `impl` and `wiring` are the root's.

## Which tool do I need

| I want | Use |
|---|---|
| placeholders anywhere in the app | the renderers installed at the root, then `ShimmerHost(effect, style)` once |
| a screen's elements to be placeholders while it loads | `ShimmerLayout(isEnabled = isLoading) { … }` + `Modifier.shimmer()` on each element |
| an icon or text to shimmer inside its own shape, still visible | `Modifier.shimmer { it.copy(isOverlay = true) }` |
| a round placeholder for a round element | `Modifier.shimmer { it.copy(style = it.style?.copy(shape = CircleShape)) }` |
| a wave down a list | `Modifier.shimmer { it.copy(clock = ShimmerClock.Staggered(index)) }` |
| items that start shimmering as they appear | `clock = ShimmerClock.PerItem` |
| another effect for one screen | `ShimmerLayout(config = ShimmerConfig(isEnabled = …, effect = GlowShimmerEffect()))` |
| the app's colours and corners | a `ShimmerStyle` built from the app's tokens, given to `ShimmerHost` |
| an effect of my own | a `data class` implementing `ShimmerEffect` |

## The root

```kotlin
CompositionLocalProvider(values = graph.providedValues.toTypedArray()) {  // shimmer/wiring contributes the renderers
    ShimmerHost(
        effect = SweepShimmerEffect(),
        style = appShimmerStyle(),
    ) {
        App()
    }
}
```

Without Metro, provide the two renderers by hand, above the host:

```kotlin
CompositionLocalProvider(
    LocalShimmerHostRenderer provides ShimmerHostRendererImpl(),
    LocalShimmerRenderer provides ShimmerRendererImpl(),
) { … }
```

Without the renderers no placeholder is drawn outside a preview, and the elements show their filler.

Compose the host once, above everything that shimmers. It runs the one cycle synced placeholders follow and gives
every placeholder the effect and style its config does not name. With the renderers but no host, placeholders
still draw — `PlainShimmerEffect`, the default style, standing still.

`isAnimated = false` stands every placeholder still; so does the platform's reduce-motion setting, whatever
`isAnimated` says.

## A loading screen

```kotlin
ShimmerLayout(isEnabled = state.isLoading) {
    LazyColumn {
        items(state.people) { person ->
            Row {
                Avatar(person, modifier = Modifier.shimmer())
                Text(person.name, modifier = Modifier.shimmer())
            }
        }
    }
}
```

While loading, give the screen filler data of the right shape (`state.people` = a few empty persons): the
placeholders take the elements' sizes. When `isEnabled` turns false, each placeholder fades into its content
over `style.fadeOutMillis`.

`ShimmerLayout(isEnabled)` keeps the rest of the config around it; `ShimmerLayout(config)` replaces it.

**Order matters.** `Modifier.shimmer()` draws the modifiers after it and the children: put it before the
element's own `background`, `clip`, `border`, or the placeholder only covers what comes before.

**Screen readers.** A placeholder hides its content's semantics (the filler). Give the loading container one
announcement in the app's words: `Modifier.semantics { contentDescription = strings.loading }`.

## One element, differently

`Modifier.shimmer { config -> … }` gets the ambient config with the host's style and effect filled in, and
returns the one this element uses:

```kotlin
Icon(modifier = Modifier.shimmer { it.copy(isOverlay = true) })                        // inside the icon's pixels
Avatar(modifier = Modifier.shimmer { it.copy(style = it.style?.copy(shape = CircleShape)) })
Text(modifier = Modifier.shimmer { it.copy(isEnabled = true) })                      // regardless of the layout
```

The placeholder is clipped to the **style's** shape, not the element's own clip — a round element asks for a
round shape.

## Clocks

| `ShimmerClock` | Progress | Animations |
|---|---|---|
| `Synced` (default) | the host's | one for the screen |
| `Staggered(index, stepFraction = 0.08f)` | the host's, shifted by `index × stepFraction` of a cycle | one for the screen |
| `PerItem` | its own, from when the element appears | one per element |

## The look

```kotlin
// in the app's design-system module — the one file that maps tokens onto the kit
fun appShimmerStyle(): ShimmerStyle = ShimmerStyle(
    baseColor = AppTheme.colors.surfaceMuted,
    highlightColor = AppTheme.colors.surface,
    shape = AppTheme.shapes.medium,
    padding = 1.dp,
)
```

`durationMillis` is the cycle (1600 by default) and `fadeOutMillis` the fade into the content (300).

## Effects

| Effect | Draws | Parameters |
|---|---|---|
| `SweepShimmerEffect` (feature `sweep`) | a tilted band travelling across, in the layout direction | `widthFraction` 0.6, `tiltDegrees` 15, `sharpness` 0 (smooth) – 1 (hard edges) |
| `GlowShimmerEffect` (feature `glow`) | a highlight breathing from the centre | `radiusFraction` 0.75 |
| `PlainShimmerEffect` (api) | the base colour, still | — |

### An effect of your own

```kotlin
data class StripesEffect(val stripes: Int = 4) : ShimmerEffect {
    override fun prepare(area: ShimmerArea, style: ShimmerStyle, density: Density, layoutDirection: LayoutDirection): ShimmerPaint {
        val step = area.size.width / stripes                 // once per size, style, effect
        return ShimmerPaint { progress ->                     // every frame: arithmetic only
            Brush.horizontalGradient(
                colors = listOf(style.baseColor, style.highlightColor, style.baseColor),
                startX = area.topLeft.x + step * progress * stripes,
                endX = area.topLeft.x + step * (progress * stripes + 1),
                tileMode = TileMode.Repeated,
            )
        }
    }
}
```

Make it a `data class` (or `data object`): placeholders re-prepare only when the effect is no longer equal.

## Previews and tests

In a preview (`LocalInspectionMode`), placeholders stand still with nothing installed: a fill in the base colour,
or mid-cycle in the host's effect when the renderers are there. In UI tests, an animating host keeps the clock
busy: wrap the screen in `ShimmerHost(effect = …, isAnimated = false)`. A test with no renderers sees the real
elements and their filler.
