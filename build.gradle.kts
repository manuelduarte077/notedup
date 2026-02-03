plugins {
    alias(libs.plugins.androidApplication) apply false  // Android application plugin
    alias(libs.plugins.androidLibrary) apply false      // Android library plugin
    alias(libs.plugins.composeMultiplatform) apply false  // Compose Multiplatform UI
    alias(libs.plugins.composeCompiler) apply false     // Compose compiler plugin
    alias(libs.plugins.kotlinMultiplatform) apply false  // Kotlin Multiplatform plugin
}