# An effect the app did not take is offered, not copied

Skill: shimmer-kit
Fixture: an app with shimmer-kit installed by skill-manager with `features = sweep` in `kits.lock` (so no
`shimmer/glow` module), and a `ProfileHeader` whose cover image is a placeholder while loading.

## Prompt
Make the profile cover's loading placeholder glow instead of sweeping.

## Passes when
- no file under the app's `shimmer/glow` path was created by hand, and no `GlowShimmerEffect` class was written
  in the app (`grep -rn "class GlowShimmerEffect"` finds nothing)
- the reply names `skillctl.sh kit select shimmer-kit --with glow` (or asks to run it) before using
  `GlowShimmerEffect`
- `kits.lock` is unchanged unless the user agreed in the run

## Runs
| Date | Model | Without the skill | With it |
|---|---|---|---|
