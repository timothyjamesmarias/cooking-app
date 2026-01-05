package com.timothymarias.cookingapp.shared.data.local

actual object BuildConfig {
    // iOS doesn't have a built-in BuildConfig like Android.
    // Options to set this properly:
    // 1. Pass via compilation flag: -Xopt-in=kotlin.experimental.ExperimentalNativeApi
    // 2. Set via build.gradle.kts based on iOS build configuration
    // 3. Use runtime detection (complex and unreliable)
    //
    // For now, defaulting to true (enables dev seeding).
    // TODO: Configure this properly in iOS build settings
    actual val isDebug: Boolean = true
}
