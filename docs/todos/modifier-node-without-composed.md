# `Modifier.shimmer` is built with `composed`

`Modifier.shimmer` reads CompositionLocals and remembers its fade, so it is a `composed` modifier, as
Act2Act's was. `composed` allocates per call site and is discouraged in favour of a `Modifier.Node` that reads
locals through `CompositionLocalConsumerModifierNode`. The fade (`animateFloatAsState`) would then
move into the node as an `Animatable` driven from `coroutineScope`. The renderer contract
would change with it: `ShimmerRenderer.rememberModifier` is `@Composable`, so a node-based `Modifier.shimmer` needs
a renderer that hands back a plain element instead.

Open: whether the gain is worth moving the animations out of composition — measure a list of a hundred
placeholders first.
