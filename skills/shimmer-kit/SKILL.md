---
name: shimmer-kit
description: Writes, reviews and debugs loading placeholders in Compose Multiplatform apps that use shimmer-kit (packages io.thernal.shimmerkit.shimmer.*; Modifier.shimmer, ShimmerLayout, ShimmerConfig, ShimmerHost, ShimmerClock, ShimmerStyle, ShimmerEffect, SweepShimmerEffect, GlowShimmerEffect, PlainShimmerEffect). Use it for any loading state, skeleton screen, placeholder or shimmer in such a project, even when the kit is not named — a list or profile that loads, an icon that pulses while waiting, a wave down a list, placeholders in the app's colours, a placeholder that is square around a round avatar, one that never animates or never goes away, previews and UI tests of loading screens. Not for projects without shimmer-kit, or progress indicators and spinners.
---

# shimmer-kit

The screen's own elements are the placeholders: `Modifier.shimmer()` on each, a `ShimmerLayout` saying
whether the screen is loading, one `ShimmerHost` at the root. Guide: https://github.com/Thernal/shimmer-kit —
`shimmer/api/README.md` (use), `shimmer/README.md` (why).

## 1. Orient first

```sh
grep -rn --include=*.kt "ShimmerHost(" .                                # the root: effect and style
grep -rn --include=*.kt -e "ShimmerStyle(" -e "ShimmerLayout(" . | head  # token mapping, loading screens to copy
grep -n -A3 '^\[shimmer-kit\]' kits.lock; grep -n '^features' kits.lock  # taken as a kit? which effects?
```

No host → add one at the root (§4). A `kits.lock` naming `shimmer-kit` means the modules were copied renamed
with skill-manager — this skill too. `features =` says which effects the app has: **an effect not listed is
not in the app** — `skillctl.sh kit select shimmer-kit --with glow` takes it (ask first); never copy it in
by hand.

## 2. The model

- `ShimmerLayout(isEnabled = isLoading) { … }` turns placeholders on for everything below; each element
  that should become one carries `Modifier.shimmer()`. While loading the screen shows **filler data of the
  right shape** (empty or sample items) — the placeholders take the elements' sizes. When loading ends,
  each fades into its content.
- `Modifier.shimmer { config -> config.copy(…) }` changes one element: `isOverlay = true` (shimmer inside an
  icon's or text's own pixels, content visible), `clock`, `style`, `effect`, `isEnabled`.
- The placeholder is clipped to **`style.shape`**, not the element's clip.
- `ShimmerHost(effect, style, isAnimated)` runs one cycle; `ShimmerClock.Synced` (default) follows it,
  `Staggered(index)` follows it shifted (a wave, still one animation), `PerItem` runs its own.
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
- Read nothing from the shimmer in composition; one host, never one per screen (a screen changes effect with
  `ShimmerLayout(config = …copy(effect = …))`).
- A loading container gets one announcement in the app's own strings
  (`Modifier.semantics { contentDescription = … }`) — placeholders hide their filler from screen readers.
- Do not chase per-item phases with `PerItem` across a long list: `Staggered(index)` gives the wave with one
  animation.

## 4. Setup

```kotlin
// root
ShimmerHost(effect = SweepShimmerEffect(), style = appShimmerStyle()) { App() }

// design system — the one mapping file
fun appShimmerStyle(): ShimmerStyle = ShimmerStyle(
    baseColor = AppTheme.colors.surfaceMuted, highlightColor = AppTheme.colors.surface,
    shape = AppTheme.shapes.medium, padding = 1.dp,
)
```

Build: modules `…:shimmer:api` and the effect modules the app took (`…:shimmer:sweep`, `…:shimmer:glow`),
each declared by the module that uses it (no `api(...)` re-exports).

## 5. When it misbehaves

| Symptom | Cause |
|---|---|
| placeholders are a flat colour that never moves | no `ShimmerHost` above (or `isAnimated = false`, or the device's reduce motion / "Remove animations") |
| square placeholder around a round avatar | the style's shape is used — `style = it.style?.copy(shape = CircleShape)` |
| the background shows through the placeholder / only part is covered | `Modifier.shimmer()` placed after `background`/`clip` — move it first |
| placeholders never go away | the `ShimmerLayout`'s `isEnabled` never turns false, or an element sets `isEnabled = true` itself |
| `SweepShimmerEffect`/`GlowShimmerEffect` unresolved | that effect is not taken — `features =` in `kits.lock`; offer `kit select shimmer-kit --with …` |
| rows in a list out of step | `PerItem` clocks — use `Synced` or `Staggered(index)` |
| a UI test hangs waiting for idle | the host animates forever — `ShimmerHost(effect = …, isAnimated = false)` in tests |
| TalkBack/VoiceOver reads nothing while loading | intended — announce loading on the container |
