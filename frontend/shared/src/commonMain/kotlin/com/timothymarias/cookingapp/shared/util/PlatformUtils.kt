package com.timothymarias.cookingapp.shared.util

/**
 * Platform-independent utility functions
 */

/**
 * Get current timestamp in milliseconds
 */
expect fun currentTimeMillis(): Long

/**
 * Generate a random UUID string
 */
expect fun randomUUID(): String