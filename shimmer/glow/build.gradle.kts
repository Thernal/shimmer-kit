plugins {
    alias(libs.plugins.shimmerkit.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.shimmer.api)
            }
        }
    }
}
