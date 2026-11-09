# Act2Act onto the kit

The kit was ported from Act2Act's `uikit/components/…/core/shimmer`; Act2Act still has its own copy. Moving it
onto the kit (`kit install shimmer-kit --adopt` after aligning names, or a fresh install and a call-site pass):

- `ShimmerProgressProvider` in the root's provider list → `ShimmerHost(effect = SweepShimmerEffect(), style = …)`, with
  the renderers `shimmer/wiring` contributes joining the app's `Set<ProvidedValue<*>>` above it.
- `ShimmerDefaults` (`DsTheme.colors.background04/02`, `rounding.r16`, `dimens.dp1`) → one `ShimmerStyle` in the
  design system.
- `asOverlay` → `isOverlay`; custom `durationMillis` on a config (the splash) → a `ShimmerHost` of its own with a style
  of that duration. `delayMillis` has no replacement.
- `grep -rn -e 'asOverlay' -e 'ShimmerProgressProvider' -e 'shimmer.config.ShimmerDefaults' .` finds the call sites.
