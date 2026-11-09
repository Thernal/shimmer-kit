plugins {
    alias(libs.plugins.shimmerkit.compose)
    alias(libs.plugins.shimmerkit.injection)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // The contracts are not re-exported: an app that names them depends on `api` itself.
                implementation(projects.shimmer.api)
                implementation(projects.shimmer.impl)
            }
        }
    }
}
