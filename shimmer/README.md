# Why shimmer-kit has the shape it has

## Contracts in `api`, work in `impl`

A screen names `api` alone: `ShimmerHost`, `ShimmerLayout`, `ShimmerConfig`, the style, clock and effect values,
and `Modifier.shimmer`. The two composable entries are thin calls to a renderer from a CompositionLocal —
`ShimmerHostRenderer`, `ShimmerRenderer` — the shape overlay-kit, paging-kit and nav-kit have. `impl` holds the
work: the transition, reduce motion, the draw node and the fade. `wiring` contributes the renderers
to the graph's `Set<ProvidedValue<*>>`; `api` and `impl` name no injection framework, so an app without Metro
provides the two locals by hand. An effect lives in `impl` and uses `api` only: it is a brush, and knows nothing
of the node that paints it.

A renderer's default previews it. In a preview a placeholder is a still fill in the base colour and the host
just shows its content. Outside a preview, a missing installation changes nothing about the element: the filler
showing through gives it away, where a still fill would pass for an installation that only forgot to animate.
The host never hides its content, since the whole screen sits under it.

## The real layout is the placeholder

A skeleton screen drawn separately — grey boxes laid out to look like the screen — is a second layout to
keep in step with the first, and it drifts the moment a row gains a line. Here the screen's own elements carry
`Modifier.shimmer()`: while loading, each draws the effect in its own bounds instead of its content, and the
content fades in when loading ends. The data the elements show while loading is filler (an empty or sample
model); its sizes are what the placeholder takes.

So the placeholder hides its content's semantics too: a screen reader would otherwise read the filler.
Announcing "loading" once is the app's job, in its own words — the kit carries no string.

## One cycle, the nearest host's

Placeholders that each animate on their own drift apart and look restless; a list should move as one. The
host runs a single infinite transition and every placeholder reads it, so a screen of fifty placeholders is
one animation.

There are no per-element clocks. Act2Act chose between "global" and "own" implicitly: a placeholder whose
duration or delay differed from the default silently got a transition of its own. An early version of this
kit made that explicit, with synced, per-item and staggered clocks, and removed it: a design system wants one
loading motion, and a clock on every element is a choice every call site can make differently. A part of a
screen that really needs another speed or effect sits under a `ShimmerHost` of its own and follows that one.

## The effect is a value behind a contract

Act2Act's renderer was one effect — the sweep band — with its parameters (band width, tilt, sharpness) on
the config every call site carries. Now `ShimmerEffect` is the seam:

- `prepare(area, style, density, layoutDirection)` runs when the size, style, effect or density changes and
  returns a `ShimmerPaint`; `brushAt(progress)` runs every frame and is arithmetic. The band's geometry and
  colour stops are computed once, not per frame.
- The draw node owns everything effect-independent — the inset area, the clip to the style's shape, the
  fade between placeholder and content, the overlay mode — so an effect is only a brush.
- Effects are `data class`es: equal effects are not re-prepared, and an effect's parameters live on the effect
  (`SweepShimmerEffect(widthFraction, tiltDegrees, sharpness)`), not on every placeholder's config.

An effect is chosen on the host, not per element, and each is a directory inside `impl` and a **feature** in
`kit.yml`: an app takes `sweep` (the default), `glow`, both, or neither. A feature does not have to be a module:
these are two small files with no dependency of their own, and skill-manager leaves a directory out as readily as
a module. Nothing else in the kit names an effect, so leaving one out cannot break the build, and
`scripts/check-features.sh` proves it. Without any effect, `PlainShimmerEffect` — the base colour, still — is
what placeholders draw.

## The look is the app's tokens

`ShimmerStyle` holds what every effect shares — base and highlight colour, shape, padding, cycle and fade
durations — with neutral defaults. Act2Act read its design system (`DsTheme`) straight from the defaults; a
kit cannot, so the app maps its tokens onto `ShimmerStyle` in one file of its own and passes it to the host.

## Calm by default

`ShimmerHost(isAnimated = false)` stands every placeholder still — the style's colours at mid-cycle — and so does
the platform, whatever `isAnimated` says: iOS's Reduce Motion, Android's "Remove animations" (animator scale 0).
The platform's setting is read in `impl`, and the app has no way to overrule it: a user who asked for less motion
is not an app's to argue with.

## What changed from Act2Act

| Act2Act | shimmer-kit |
|---|---|
| `ShimmerProgressProvider`, a `ComposableProvider` in the app's root list | `ShimmerHost(effect, style)` — a plain composable; the app's root list gets the renderers |
| `ShimmerDefaults` read `DsTheme` | `ShimmerStyle`, mapped by the app |
| band width, tilt, sharpness, colours, timings on `ShimmerConfig` | effect parameters on `SweepShimmerEffect`; colours and timings on `ShimmerStyle` |
| `asOverlay` | `isOverlay` |
| own transition when duration or delay differed; per-element `delayMillis` | gone: every placeholder follows the nearest host, and a differing part has a host of its own |
| the sweep only | the `ShimmerEffect` contract; `sweep` and `glow` as features, `PlainShimmerEffect` without either |
| the band always left to right | the band follows the layout direction |
| semantics of the filler kept | hidden while loading |
| always animated | still under reduce motion |
| one module with the draw code beside the modifier | `api` / `impl` / `wiring`, as the other kits |

The draw node, its cache, the fade between placeholder and content, the overlay mode (`SrcAtop` over the
content's own pixels) and the first-frame trick (the host shows a third of the cycle until its transition
runs, so the first frame is never an empty base colour) are Act2Act's, moved behind the contract.
