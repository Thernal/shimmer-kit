# shimmer-kit

Loading placeholders for Compose Multiplatform (Android, iOS) — `Modifier.shimmer()` on the real layout,
turned on and off by a `ShimmerLayout` around it, drawn by one host at the app's root. Foundation only, no
Material.

```kotlin
// once, at the root
ShimmerHost(effect = SweepShimmerEffect(), style = appShimmerStyle()) { App() }

// a screen
ShimmerLayout(isEnabled = state.isLoading) {
    Row {
        Avatar(modifier = Modifier.shimmer())
        Text(text = user.name, modifier = Modifier.shimmer())
    }
}
```

- **The real layout is the placeholder.** No second skeleton composable to keep in step with the screen: the
  elements themselves are hidden behind the effect while loading and fade in when it ends.
- **One cycle for the screen.** Every placeholder follows the host's single animation by default, so a list
  of rows moves as one. Per element, a `ShimmerClock` chooses otherwise: `PerItem` (each its own wave, from
  when it appears) or `Staggered(index)` (the host's cycle shifted along a list — still one animation).
- **The effect is a value.** `SweepShimmerEffect` (a tilted band travelling across) and `GlowShimmerEffect`
  (a highlight breathing from the centre) are the kit's; an app may write its own `ShimmerEffect`. Without
  any, placeholders are a still fill in the base colour — previews and tests need nothing installed.
- **The look is the app's.** `ShimmerStyle` — colours, shape, padding, durations — has neutral defaults; the
  app maps its design tokens onto it in one file of its own design system.
- **Accessible and calm.** A placeholder is hidden from screen readers (the text under it is filler); the
  host stands still when the platform asks to reduce motion.
- **Cheap per frame.** Geometry, clip and the effect's paint are prepared once per size, style and effect;
  a frame only asks for a brush, read in draw — no recomposition.

Ported from Act2Act's design-system shimmer (`shimmer/README.md` says what changed and why).

## Documentation

| Read | For |
|---|---|
| [`shimmer/api/README.md`](shimmer/api/README.md) | using it, task by task: the host, layouts, overlays on icons, clocks, styles, an effect of your own, previews |
| [`shimmer/README.md`](shimmer/README.md) | why each part has its shape, and what changed from Act2Act |
| [`docs/todos/`](docs/todos) | open questions |
| [`skills/shimmer-kit`](skills/shimmer-kit/SKILL.md) | the same for an agent in an app that took the kit |

## Installing

An application takes the kit **by copy**, not as a dependency: the code is copied into the app, renamed to
the app's own package, and belongs to the app from then on. Nothing is published to a Maven repository.

### With skill-manager

If you have access to the author's knowledge repository (`github.com/Thernal/knowledge`), its
**skill-manager** skill does all of it — copy, rename, the skill, and later updates:

```sh
skillctl.sh kit install shimmer-kit --package com.example.app.core.designsystem \
    --module :core:designsystem:shimmer --alias app            # the api and the sweep effect
skillctl.sh kit install shimmer-kit … --with sweep,glow        # both effects
skillctl.sh kit install shimmer-kit … --with none              # the api alone, an effect of the app's own
```

The effects are **features** in [`kit.yml`](kit.yml): `sweep` is taken by default, `glow` when asked for. An
effect not taken is never copied, and stays out on every update; `kit select shimmer-kit --with glow` takes
it later. `kit status` shows what changed upstream and what the app edited; `kit update` merges the kit's
changes three ways, keeping the app's edits. The install prints what the app must provide (`requires`).

### Without it

The same by hand, from a clone of this repository.

1. **Copy** `shimmer/api` and the effects you want (`shimmer/sweep`, `shimmer/glow`) into the app, under the
   module path the app gives them: `shimmer/…` → `core/designsystem/shimmer/…`. Note the commit you copied
   (`git rev-parse HEAD`) — updates start from it.
2. **Rename** in everything copied:

   | In the kit | Becomes | Where |
   |---|---|---|
   | `io.thernal.shimmerkit` | the app's package for `core/designsystem`, e.g. `com.example.app.core.designsystem` | sources, build files; and the directories `io/thernal/shimmerkit` |
   | `:shimmer:` and `":shimmer"`, `projects.shimmer.` | the module path, e.g. `:core:designsystem:shimmer:`, `projects.core.designsystem.shimmer.` | build files |
   | `libs.plugins.shimmerkit.` | the app's catalog alias, e.g. `libs.plugins.app.` | build files |

   ```sh
   # in the app, after copying — perl, so it runs the same on macOS and Linux
   grep -rlI -e io.thernal.shimmerkit -e io/thernal/shimmerkit -e :shimmer -e plugins.shimmerkit. core/designsystem/shimmer \
     | xargs perl -pi -e 's/\Qio.thernal.shimmerkit\E/com.example.app.core.designsystem/g; s{\Qio/thernal/shimmerkit\E}{com/example/app/core/designsystem}g; s/\Q:shimmer:\E/:core:designsystem:shimmer:/g; s/"\Q:shimmer\E"/":core:designsystem:shimmer"/g; s/projects\.\Qshimmer\E\./projects.core.designsystem.shimmer./g; s/libs\.plugins\.\Qshimmerkit\E\./libs.plugins.app./g'
   find core/designsystem/shimmer -depth -type d -path '*/io/thernal/shimmerkit' | while read -r d; do
     mkdir -p "${d%/io/thernal/shimmerkit}/com/example/app/core" && mv "$d" "${d%/io/thernal/shimmerkit}/com/example/app/core/designsystem"
   done
   find core/designsystem/shimmer -depth -type d -empty -delete
   ```

3. **Provide** what the copy expects — the `requires` list in [`kit.yml`](kit.yml): convention plugins
   (build-kit's, or the ones in this repository's `build-logic/convention`), catalog entries, settings.
4. **The skill** (optional): copy [`skills/shimmer-kit`](skills/shimmer-kit) into the app's skills directory
   (`.claude/skills/` for Claude Code), with the same renames.
5. **Updates** are yours to carry: `git diff <the commit you copied> <a newer one> -- <the code paths>`.

## Layout

| Module | Holds | Depends on |
|---|---|---|
| `shimmer/api` | `Modifier.shimmer`, `ShimmerLayout`, `ShimmerConfig`, `ShimmerHost`, `ShimmerClock`, `ShimmerStyle`, the `ShimmerEffect` contract and `PlainShimmerEffect`; the draw node | Compose |
| `shimmer/sweep` | `SweepShimmerEffect` — feature `sweep`, taken by default | api |
| `shimmer/glow` | `GlowShimmerEffect` — feature `glow` | api |
| `sample/shared`, `sample/android`, `sample/ios` | one screen: every effect and clock, a loading switch, and `sampleShimmerStyle` — the mapping an app writes in its own design system | the kit — never copied |

## Building

```sh
./gradlew build                  # every target, tests on the JVM host and the iOS simulator, Detekt
scripts/check-features.sh        # the build again with each feature left out, as an app that does not take it
```

Detekt fails on any finding (`-PdetektAutoCorrect=true` fixes formatting first). Building for iOS needs
Xcode, not only its command-line tools. `-Pkit.without=sweep,glow` leaves those modules (and the sample)
out of the build; `scripts/check-features.sh` runs it for each feature in `kit.yml`.

The iOS sample is an Xcode project (`sample/ios/Sample.xcodeproj`, generated from `project.yml` by
XcodeGen); its build phase compiles the framework with Gradle:

```sh
xcodebuild -project sample/ios/Sample.xcodeproj -scheme Sample -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' build
```

## License

MIT — see [LICENSE](LICENSE).
