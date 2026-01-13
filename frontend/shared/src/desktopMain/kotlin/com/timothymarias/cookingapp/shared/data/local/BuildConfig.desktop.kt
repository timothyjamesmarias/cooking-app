package com.timothymarias.cookingapp.shared.data.local

actual object BuildConfig {
    // Desktop builds can set environment via system property:
    // -Dapp.environment=production  (or just omit for dev)
    //
    // Or via environment variable: APP_ENVIRONMENT=production
    actual val isDebug: Boolean = run {
        val sysProp = System.getProperty("app.environment")
        val envVar = System.getenv("APP_ENVIRONMENT")

        // If explicitly set to "production" or "release", disable debug
        val isProd = sysProp?.lowercase() in listOf("production", "release") ||
                     envVar?.lowercase() in listOf("production", "release")

        !isProd  // Default to debug (true) unless explicitly set to production
    }
}
