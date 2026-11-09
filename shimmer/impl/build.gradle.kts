plugins {
    alias(libs.plugins.shimmerkit.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.animation.core)
                implementation(projects.shimmer.api)
            }
        }
    }
}
