package com.timothymarias.cookingapp.shared.data.local

/**
 * Cross-platform build configuration.
 * Provides environment flags (debug vs release) for conditional logic like seeding.
 */
expect object BuildConfig {
    /**
     * True if this is a debug/development build, false for production/release.
     * Used for:
     * - Seeding test data
     * - Enabling verbose logging
     * - Development-only features
     */
    val isDebug: Boolean
}
