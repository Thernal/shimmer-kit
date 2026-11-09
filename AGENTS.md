# shimmer-kit — rules for agents

shimmer-kit is a **kit**: reusable Compose Multiplatform code that applications copy — renamed into their own
package — with `skillctl.sh kit install shimmer-kit` (the skill-manager skill, `Thernal/knowledge`), and later
merge changes from with `kit update`. What they copy is listed in `kit.yml`: the modules `shimmer/api`, `shimmer/impl`, `shimmer/wiring`. The effects `sweep` and `glow` are directories inside
`impl` and **features** an app takes or leaves by name.
Everything committed to `main` reaches every app that takes the next update, so this file's first rule is
about delivery.

## Delivering a change

- **Nothing reaches `main` without a green `./gradlew build` and `scripts/check-features.sh`** — every
  target, the tests on the JVM host and the iOS simulator, Detekt; then the build again with each feature
  left out. Check Gradle's own exit code (`./gradlew build && git commit …`), never
  through a pipe: `./gradlew … | tail && git commit` checks `tail`. A failing test is fixed, never skipped.
- Git conventions, attribution (off here) and branches: `.agents/workspace.md`. A breaking change to what
  apps use is `feat!:`/`refactor!:` with a `Migration:` paragraph — apps merge the kit's files, never their
  own call sites.
- Detekt fails the build on any finding. `./gradlew build -PdetektAutoCorrect=true` fixes formatting;
  the rest is fixed by hand (named arguments, braces on every branch, `is`-prefixed booleans, block bodies).
  A `@Suppress` names the rule and says why, on the narrowest element.

## Writing code here

- **No `api(...)`**: a module declares everything it uses; nothing is re-exported.
- **Rename-safe.** An app's copy is renamed textually (`kit.yml`: package, module, alias). Write the
  package prefix whole, never split or as a regex fragment; `libs.plugins.<alias>.` literally; and keep the
  kit's name out of any string an install does not rename.
- **No Material.** The kit's modules use Compose runtime, foundation, ui and animation-core; only the
  sample may use Material.
- **Contracts in `api`, work in `impl`.** `ShimmerHost` and `Modifier.shimmer` are thin calls to a renderer from
  a CompositionLocal whose default previews them; `impl` implements `ShimmerHostRenderer` and `ShimmerRenderer`
  (the transition, reduce motion, the draw node, the fade); `wiring` contributes them
  (Metro `Set<ProvidedValue<*>>`) and holds binding containers only. Nothing in `api` imports `impl`.
- **Features are referenced by nothing outside them.** Nothing in `api`, `wiring` or the rest of `impl` imports an
  effect, an effect uses `api` alone, and no effect imports another: an app that leaves one out must still build.
  The api's own fallback (`PlainShimmerEffect`) is what a placeholder draws without any. A new effect is a new directory
  `presentation/effect/<name>` in `impl`'s `commonMain` and `commonTest`, a `feature:` line in `kit.yml` (not `default` — apps that never asked for it keep not having it),
  and `scripts/check-features.sh` proves it can be left out.
- **An effect is a value** (`data class`/`data object`): the draw node re-prepares only when it changes. What
  depends on area, style and density is computed in `prepare`; `brushAt` runs every frame and stays
  arithmetic.
- **Progress is read in draw, never in composition.** The node observes the progress `State` and invalidates
  draw; reading it in a composable would recompose the screen every frame.
- **One cycle and one effect, the nearest host's.** A placeholder has no clock and no effect of its own: a design
  system wants one loading motion. A part of a screen that really differs sits under a `ShimmerHost` of its own.
- **The kit carries no copy.** No string reaches the user; announcing "loading" is the app's.
- `api`/`impl` code lives in `presentation` packages (the kit's own Detekt rules); below it, packages are named
  by part (`host`, `config`, `render`…). Effects live in `impl`, under `presentation/effect/<name>`.
- Kotlin nests block comments: never write `/*` inside KDoc.
- Every module that changes what apps copy updates `kit.yml` in the same change: `code`, `feature`,
  `surface`, `requires`.

## Documentation

| File | Holds | Update when |
|---|---|---|
| `README.md` | what the kit does, its layout, how it is built | anything a user of the kit sees changes |
| `shimmer/README.md` | why each part has its shape, what changed from Act2Act | a decision or trade-off changes |
| `shimmer/api/README.md` | how to use it, task by task | the contract changes |
| `docs/todos/` | open questions, one file each | a question opens or is decided (then delete it) |
| `skills/shimmer-kit` | the same for an agent in an app that took the kit | the public surface changes — `kit status` flags a skill older than the surface (LAG) |
| `kit.yml` | what an app copies and what its build must provide | a module, file part or requirement changes |

The skill's frontmatter has to load in Claude Code and in Codex alike (knowledge `docs/SKILLS.md`): a
`description` of at most 1024 characters — 600–900 in practice, since an app's renamed package can
lengthen it and every installed skill shares one context budget — valid YAML (no `": "` or `" #"` in a
plain one-line description; write `—`), and only the keys `name` and `description`. skill-manager warns
in an app whose copy breaks this; the fix is made here.

Docs are read in place by apps (`knowledgectl.sh kit shimmer-kit read <path>`), never copied. When `kit.yml`
or `README.md` changes, the knowledge card `kits/shimmer-kit.md` in `Thernal/knowledge` needs its `card_sha`
bumped (`scripts/check-corpus.py --kits` says so).

## The sample

`sample/` shows every effect on Android and iOS and is never copied into apps.
`sampleShimmerStyle` is how an app's design system feeds the placeholders — keep every look the kit draws
reachable from `ShimmerStyle` or an effect's parameters; a value hard-coded in a module is a look no app can
change. A sample that builds is not one that starts: after changing it, build the iOS app with `xcodebuild`
and launch it on a simulator (`xcrun simctl launch --console-pty …`) — Compose Multiplatform refuses to
start without `CADisableMinimumFrameDurationOnPhone` in Info.plist, and a crash there shows nowhere else.
