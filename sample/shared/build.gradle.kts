plugins {
    alias(libs.plugins.shimmerkit.compose)
    alias(libs.plugins.shimmerkit.injection)
}

kotlin {
    // The framework the iOS sample embeds (sample/ios, built by Xcode through
    // `embedAndSignAppleFrameworkForXcode`). `./gradlew build` links it, so it cannot rot unnoticed.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SampleShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.shimmer.api)
                implementation(projects.shimmer.impl)
                implementation(projects.shimmer.wiring)
                implementation(projects.shimmer.sweep)
                implementation(projects.shimmer.glow)
                implementation(libs.compose.material3)
            }
        }
    }
}
