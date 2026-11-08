# `Modifier.shimmer` is built with `composed`

`Modifier.shimmer` reads CompositionLocals and remembers its fade, so it is a `composed` modifier, as
Act2Act's was. `composed` allocates per call site and is discouraged in favour of a `Modifier.Node` that reads
locals through `CompositionLocalConsumerModifierNode`. The fade (`animateFloatAsState`) and the per-item
transition would then move into the node as `Animatable`s driven from `coroutineScope`.

Open: whether the gain is worth moving the animations out of composition — measure a list of a hundred
placeholders first.
