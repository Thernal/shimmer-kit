plugins {
    alias(libs.plugins.shimmerkit.android.application)
}

dependencies {
    implementation(projects.sample.shared)
    implementation(libs.androidx.activity.compose)
}
