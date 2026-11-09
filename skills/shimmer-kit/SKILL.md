---
name: shimmer-kit
description: Writes, reviews and debugs loading placeholders in Compose Multiplatform apps that use shimmer-kit (packages io.thernal.shimmerkit.shimmer.*; Modifier.shimmer, ShimmerLayout, ShimmerConfig, ShimmerHost, ShimmerStyle, ShimmerEffect, SweepShimmerEffect, GlowShimmerEffect, PlainShimmerEffect). Use it for any loading state, skeleton screen, placeholder or shimmer in such a project, even when the kit is not named — a list or profile that loads, an icon that pulses while waiting, placeholders in the app's colours, a placeholder that is square around a round avatar, one that never shows, never animates or never goes away, previews and UI tests of loading screens. Not for projects without shimmer-kit, or progress indicators and spinners.
---

# shimmer-kit

The screen's own elements are the placeholders: `Modifier.shimmer()` on each, a `ShimmerLayout` saying
whether the screen is loading, one `ShimmerHost` at the root. Guide: https://github.com/Thernal/shimmer-kit —
`shimmer/api/README.md` (use), `shimmer/README.md` (why).

## 1. Orient first

```sh
grep -rn --include=*.kt "ShimmerHost(" .                                # the root: effect and style
grep -rn --include=*.kt -e "providedValues" -e "LocalShimmerRenderer" . # the root: the renderers installed
grep -rn --include=*.kt -e "ShimmerStyle(" -e "ShimmerLayout(" . | head  # token mapping, loading screens to copy
grep -n -A3 '^\[shimmer-kit\]' kits.lock; grep -n '^features' kits.lock  # taken as a kit? which effects?
```

No host or no renderers → add them at the root (§4). A `kits.lock` naming `shimmer-kit` means the modules were copied renamed
with skill-manager — this skill too. `features =` says which effects the app has: **an effect not listed is
not in the app** — `skillctl.sh kit select shimmer-kit --with glow` takes it (ask first); never copy it in
by hand.

## 2. The model

- `ShimmerLayout(isEnabled = isLoading) { … }` turns placeholders on for everything below; each element
  that should become one carries `Modifier.shimmer()`. While loading the screen shows **filler data of the
  right shape** (empty or sample items) — the placeholders take the elements' sizes. When loading ends,
  each fades into its content.
- `Modifier.shimmer { config -> config.copy(…) }` changes one element: `isOverlay = true` (shimmer inside an
  icon's or text's own pixels, content visible), `style`, `isEnabled`.
- The placeholder is clipped to **`style.shape`**, not the element's clip.
- `ShimmerHost(effect, style, isAnimated)` runs the one cycle and holds the one effect; every placeholder follows
  the nearest host. There are no per-element clocks or effects.
- Effects are values: `SweepShimmerEffect()` (band), `GlowShimmerEffect()` (breathing), `PlainShimmerEffect`
  (still fill, the fallback). An app's own: a `data class` implementing `ShimmerEffect` — costly work in
  `prepare`, only arithmetic in `ShimmerPaint.brushAt(progress)`.
- Look: one `ShimmerStyle` built from the app's tokens in its design-system module, given to the host.

## 3. Rules

- Never build a second skeleton composable beside the screen — put `Modifier.shimmer()` on the real one.
- `Modifier.shimmer()` comes **before** the element's `background`/`clip`/`border` in the chain; after them it
  covers nothing they draw.
- Never restyle by editing kit code or hard-coding colours at call sites: change the app's `ShimmerStyle`, or
  pass `style = it.style?.copy(…)` for one element.
- Read nothing from the shimmer in composition. One host at the root, never one per screen: a second `ShimmerHost`
  around a part of a screen is for what the design says differs in effect or speed, and it is the exception.
- A loading container gets one announcement in the app's own strings
  (`Modifier.semantics { contentDescription = … }`) — placeholders hide their filler from screen readers.

## 4. Setup

```kotlin
// root — the graph's locals hold the renderers shimmer/wiring contributes
CompositionLocalProvider(values = graph.providedValues.toTypedArray()) {
    ShimmerHost(effect = SweepShimmerEffect(), style = appShimmerStyle()) { App() }
}
// without Metro, the two by hand, above the host:
// LocalShimmerHostRenderer provides ShimmerHostRendererImpl(), LocalShimmerRenderer provides ShimmerRendererImpl()

// design system — the one mapping file
fun appShimmerStyle(): ShimmerStyle = ShimmerStyle(
    baseColor = AppTheme.colors.surfaceMuted, highlightColor = AppTheme.colors.surface,
    shape = AppTheme.shapes.medium, padding = 1.dp,
)
```

Build: a screen's module declares `…:shimmer:api` and the effect it names; the root's declares `…:shimmer:api`,
`…:shimmer:impl` and `…:shimmer:wiring` as well (no `api(...)` re-exports). An effect is a directory in `impl`,
present only if taken (`…impl.presentation.effect.sweep`, `….effect.glow`).

## 5. When it misbehaves

| Symptom | Cause |
|---|---|
| no placeholder at all, the filler shows | the renderers are not installed above (§4) — a preview hides this: it fills in the base colour regardless |
| placeholders are a flat colour that never moves | no `ShimmerHost` above (or `isAnimated = false`, or the device's reduce motion / "Remove animations") |
| square placeholder around a round avatar | the style's shape is used — `style = it.style?.copy(shape = CircleShape)` |
| the background shows through the placeholder / only part is covered | `Modifier.shimmer()` placed after `background`/`clip` — move it first |
| placeholders never go away | the `ShimmerLayout`'s `isEnabled` never turns false, or an element sets `isEnabled = true` itself |
| `SweepShimmerEffect`/`GlowShimmerEffect` unresolved | that effect is not taken — `features =` in `kits.lock`; offer `kit select shimmer-kit --with …` |
| a UI test hangs waiting for idle | the host animates forever — `ShimmerHost(effect = …, isAnimated = false)` in tests |
| TalkBack/VoiceOver reads nothing while loading | intended — announce loading on the container |
