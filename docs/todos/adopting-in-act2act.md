# Act2Act onto the kit

The kit was ported from Act2Act's `uikit/components/…/core/shimmer`; Act2Act still has its own copy. Moving it
onto the kit (`kit install shimmer-kit --adopt` after aligning names, or a fresh install and a call-site pass):

- `ShimmerProgressProvider` in the root's provider list → `ShimmerHost(effect = SweepShimmerEffect(), style = …)`.
- `ShimmerDefaults` (`DsTheme.colors.background04/02`, `rounding.r16`, `dimens.dp1`) → one `ShimmerStyle` in the
  design system.
- `asOverlay` → `isOverlay`; custom `durationMillis`/`delayMillis` on a config (the splash) → `clock =
  ShimmerClock.PerItem` with a style of its own duration.
- `grep -rn -e 'asOverlay' -e 'ShimmerProgressProvider' -e 'shimmer.config.ShimmerDefaults' .` finds the call sites.
