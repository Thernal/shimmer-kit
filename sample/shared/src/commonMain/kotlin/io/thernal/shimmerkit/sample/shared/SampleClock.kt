package io.thernal.shimmerkit.sample.shared

/** How the rows below take their cycle. */
internal enum class SampleClock(
    val label: String,
) {
    Synced(label = "Synced"),
    PerItem(label = "Per item"),
    Staggered(label = "Staggered"),
}
